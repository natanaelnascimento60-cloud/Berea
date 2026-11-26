package com.example.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.model.ImportedBibleVersion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.Normalizer
import java.util.Locale
import java.util.UUID

data class PendingSqliteBible(
    val id: String,
    val localFilePath: String,
    val suggestedCode: String,
    val suggestedName: String,
    val detectedLanguage: String,
    val schemaType: String,
    val versesCount: Int
)

class SqliteBibleRepository(
    private val context: Context? = null
) {
    private data class SqliteTableLayout(
        val tableName: String,
        val bookCol: String,
        val chapterCol: String,
        val verseCol: String,
        val textCol: String,
        val schemaType: String
    )

    suspend fun stageAndInspectUri(uri: Uri): Result<PendingSqliteBible> = withContext(Dispatchers.IO) {
        val appContext = context ?: return@withContext Result.failure(
            IllegalStateException("Contexto do aplicativo indisponível para leitura do arquivo.")
        )
        val id = "sqlite_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val dir = File(appContext.filesDir, "imported_bibles").apply { mkdirs() }
        val destFile = File(dir, "$id.sqlite")
        val rawTempFile = File(dir, "$id.raw_upload")

        try {
            val displayName = resolveDisplayName(appContext, uri)
            val input = appContext.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(
                    IllegalArgumentException("Não foi possível abrir o arquivo selecionado.")
                )

            input.use { stream ->
                FileOutputStream(rawTempFile).use { out ->
                    stream.copyTo(out)
                }
            }

            val inspected = inspectOrConvertLocalFile(
                id = id,
                sourceFile = rawTempFile,
                destSqliteFile = destFile,
                originalFileName = displayName
            )
            rawTempFile.delete()
            if (inspected.isFailure) {
                destFile.delete()
            }
            inspected
        } catch (e: CancellationException) {
            rawTempFile.delete()
            destFile.delete()
            throw e
        } catch (e: Exception) {
            rawTempFile.delete()
            destFile.delete()
            Log.e(TAG, "Failed staging Bible file URI", e)
            Result.failure(
                Exception("Erro ao ler arquivo: ${e.localizedMessage ?: "formato inválido"}")
            )
        }
    }

    fun discardStagedFile(localFilePath: String) {
        runCatching {
            val file = File(localFilePath)
            if (file.exists()) file.delete()
        }
    }

    fun deleteLocalFile(localFilePath: String) {
        discardStagedFile(localFilePath)
    }

    suspend fun readChapterForVersions(
        versions: List<ImportedBibleVersion>,
        bookId: String,
        chapter: Int
    ): Map<String, Map<Int, String>> = withContext(Dispatchers.IO) {
        if (versions.isEmpty()) return@withContext emptyMap()
        val canonicalNum = OpenBibleRepository.canonicalBookNumber(bookId) ?: return@withContext emptyMap()

        buildMap {
            versions.forEach { version ->
                val verses = readChapterFromFile(
                    file = File(version.localFilePath),
                    canonicalBookNumber = canonicalNum,
                    chapter = chapter,
                    preferredSchemaType = version.schemaType
                )
                if (verses.isNotEmpty()) {
                    put(version.id, verses)
                }
            }
        }
    }

    private fun resolveDisplayName(appContext: Context, uri: Uri): String? {
        runCatching {
            appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) return cursor.getString(idx)
                    }
                }
        }
        return uri.lastPathSegment
    }

    companion object {
        private const val TAG = "SqliteBibleRepository"

        const val SCHEMA_VERSES_CANONICAL = "VERSES_CANONICAL"
        const val SCHEMA_VERSES_MYBIBLE = "VERSES_MYBIBLE"
        const val SCHEMA_BIBLE_MYSWORD = "BIBLE_MYSWORD"

        private val MYWORD_FOOTNOTE_REGEX by lazy { Regex("<RF>.*?(?:</RF>|</Rf>|<Rf>)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)) }
        private val MYWORD_TITLE_REGEX by lazy { Regex("<TS[0-9]*>.*?(?:</TS>|<Ts>)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)) }
        private val MYBIBLE_NOTE_REGEX by lazy { Regex("<[nfs][^>]*>.*?</[nfs]>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)) }

        // Canonical 1..66 -> MyBible .SQLite3 book_number (10..730)
        private val canonicalToMyBibleMap: IntArray = intArrayOf(
            0, // unused 0
            10, 20, 30, 40, 50, 60, 70, 80, 90, 100,       // 1..10: Gen..2Sam
            110, 120, 130, 140, 150, 160, 190, 220, 230, 240, // 11..20: 1Kgs..Prov
            250, 260, 290, 300, 310, 330, 340, 350, 360, 370, // 21..30: Eccl..Amos
            380, 390, 400, 410, 420, 430, 440, 450, 460, 470, // 31..40: Obad..Matt
            480, 490, 500, 510, 520, 530, 540, 550, 560, 570, // 41..50: Mark..Phil
            580, 590, 600, 610, 620, 630, 640, 650, 660, 670, // 51..60: Col..1Pet
            680, 690, 700, 710, 720, 730                      // 61..66: 2Pet..Rev
        )

        fun canonicalToMyBibleNumber(canonicalBookNumber: Int): Int {
            return if (canonicalBookNumber in 1..66) canonicalToMyBibleMap[canonicalBookNumber] else canonicalBookNumber
        }

        internal fun cleanSqliteVerseText(raw: String): String {
            if (raw.isBlank()) return ""
            val preCleaned = raw
                .replace(MYWORD_FOOTNOTE_REGEX, "")
                .replace(MYWORD_TITLE_REGEX, "")
                .replace(MYBIBLE_NOTE_REGEX, "")
                .replace("<CM>", " ", ignoreCase = true)
                .replace("<CL>", " ", ignoreCase = true)
                .replace("<PI>", " ", ignoreCase = true)
            return OpenBibleRepository.cleanVerseHtml(preCleaned)
        }

        internal fun inspectLocalSqliteFile(
            id: String,
            file: File,
            originalFileName: String? = null
        ): Result<PendingSqliteBible> {
            if (!file.exists() || file.length() < 100L) {
                return Result.failure(IllegalArgumentException("Arquivo SQLite vazio ou corrompido."))
            }

            val headerBytes = ByteArray(16)
            file.inputStream().use { it.read(headerBytes) }
            val headerStr = String(headerBytes, Charsets.US_ASCII)
            if (!headerStr.startsWith("SQLite format 3")) {
                return Result.failure(
                    IllegalArgumentException("O arquivo selecionado não é um banco SQLite válido.")
                )
            }

            return try {
                SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                    val layout = detectTableLayout(db)
                        ?: return Result.failure(
                            IllegalArgumentException(
                                "Estrutura de Bíblia não reconhecida neste SQLite. Esperado tabela 'verses' ou 'Bible' com livro, capítulo, versículo e texto."
                            )
                        )

                    val versesCount = db.rawQuery(
                        "SELECT COUNT(*) FROM \"${layout.tableName}\" WHERE \"${layout.textCol}\" IS NOT NULL AND length(trim(\"${layout.textCol}\")) > 0",
                        null
                    ).use { cursor ->
                        if (cursor.moveToFirst()) cursor.getInt(0) else 0
                    }

                    if (versesCount <= 0) {
                        return Result.failure(
                            IllegalArgumentException("Nenhum versículo encontrado neste arquivo SQLite.")
                        )
                    }

                    val meta = readMetadata(db)
                    val baseFileName = originalFileName
                        ?.substringBeforeLast('.')
                        ?.replace(".bbl", "", ignoreCase = true)
                        ?.trim()
                        .orEmpty()

                    val rawName = meta["description"]
                        ?: meta["title"]
                        ?: baseFileName.takeIf { it.isNotBlank() }
                        ?: "Bíblia SQLite"

                    val rawCode = meta["abbreviation"]
                        ?: meta["abbr"]
                        ?: extractShortCode(baseFileName.ifBlank { rawName })

                    val rawLang = meta["language"] ?: "pt"
                    val langDisplay = when {
                        rawLang.lowercase(Locale.ROOT).startsWith("pt") -> "Português · SQLite Local"
                        rawLang.isNotBlank() -> "${rawLang.uppercase(Locale.ROOT)} · SQLite Local"
                        else -> "SQLite Local"
                    }

                    Result.success(
                        PendingSqliteBible(
                            id = id,
                            localFilePath = file.absolutePath,
                            suggestedCode = sanitizeCode(rawCode),
                            suggestedName = rawName.take(60),
                            detectedLanguage = langDisplay,
                            schemaType = layout.schemaType,
                            versesCount = versesCount
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed inspecting SQLite file", e)
                Result.failure(
                    Exception("Não foi possível ler o SQLite: ${e.localizedMessage ?: "erro interno"}")
                )
            }
        }

        internal fun readChapterFromFile(
            file: File,
            canonicalBookNumber: Int,
            chapter: Int,
            preferredSchemaType: String? = null
        ): Map<Int, String> {
            if (!file.exists()) return emptyMap()
            return try {
                SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                    val layout = detectTableLayout(db) ?: return emptyMap()
                    val useMyBibleNum = (preferredSchemaType == SCHEMA_VERSES_MYBIBLE) ||
                        (layout.schemaType == SCHEMA_VERSES_MYBIBLE)

                    val primaryBookVal = if (useMyBibleNum) {
                        canonicalToMyBibleNumber(canonicalBookNumber)
                    } else {
                        canonicalBookNumber
                    }

                    val firstPass = queryVerses(db, layout, primaryBookVal, chapter)
                    if (firstPass.isNotEmpty()) return firstPass

                    // Fallback in case the file uses canonical 1..66 or MyBible 10..730 inversely
                    val altBookVal = if (useMyBibleNum) {
                        canonicalBookNumber
                    } else {
                        canonicalToMyBibleNumber(canonicalBookNumber)
                    }
                    if (altBookVal != primaryBookVal) {
                        queryVerses(db, layout, altBookVal, chapter)
                    } else {
                        emptyMap()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading chapter $canonicalBookNumber:$chapter from ${file.name}", e)
                emptyMap()
            }
        }

        private fun queryVerses(
            db: SQLiteDatabase,
            layout: SqliteTableLayout,
            bookNumber: Int,
            chapter: Int
        ): Map<Int, String> {
            val sql = "SELECT \"${layout.verseCol}\", \"${layout.textCol}\" FROM \"${layout.tableName}\" " +
                "WHERE \"${layout.bookCol}\" = ? AND \"${layout.chapterCol}\" = ? " +
                "ORDER BY \"${layout.verseCol}\" ASC"
            val result = linkedMapOf<Int, String>()
            db.rawQuery(sql, arrayOf(bookNumber.toString(), chapter.toString())).use { cursor ->
                while (cursor.moveToNext()) {
                    val verseNum = cursor.getInt(0)
                    val rawText = cursor.getString(1).orEmpty()
                    val cleaned = cleanSqliteVerseText(rawText)
                    if (verseNum > 0 && cleaned.isNotBlank()) {
                        result[verseNum] = cleaned
                    }
                }
            }
            return result
        }

        private fun detectTableLayout(db: SQLiteDatabase): SqliteTableLayout? {
            val tableNames = mutableListOf<String>()
            db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'",
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getString(0)
                    if (!name.isNullOrBlank()) tableNames.add(name)
                }
            }

            // Prioritize standard Bible table names first
            val orderedTables = tableNames.sortedBy { name ->
                when (name.lowercase(Locale.ROOT)) {
                    "verses" -> 0
                    "bible" -> 1
                    "scripture", "verse", "texts" -> 2
                    else -> 3
                }
            }

            for (tableName in orderedTables) {
                val cols = mutableListOf<String>()
                db.rawQuery("PRAGMA table_info(\"$tableName\")", null).use { cursor ->
                    val nameIdx = cursor.getColumnIndex("name")
                    while (cursor.moveToNext()) {
                        if (nameIdx >= 0) {
                            cols.add(cursor.getString(nameIdx))
                        }
                    }
                }

                val bookCol = findMatchingColumn(cols, listOf("book_number", "book_id", "book", "b", "bk"))
                val chapterCol = findMatchingColumn(cols, listOf("chapter", "chap", "ch", "c"))
                val verseCol = findMatchingColumn(cols, listOf("verse", "ver", "vs", "v"))
                val textCol = findMatchingColumn(cols, listOf("text", "scripture", "content", "verse_text", "txt", "t"))

                if (bookCol != null && chapterCol != null && verseCol != null && textCol != null) {
                    val schemaType = when {
                        tableName.equals("bible", ignoreCase = true) && textCol.equals("scripture", ignoreCase = true) -> {
                            SCHEMA_BIBLE_MYSWORD
                        }
                        detectMyBibleNumbering(db, tableName, bookCol) -> {
                            SCHEMA_VERSES_MYBIBLE
                        }
                        else -> SCHEMA_VERSES_CANONICAL
                    }
                    return SqliteTableLayout(
                        tableName = tableName,
                        bookCol = bookCol,
                        chapterCol = chapterCol,
                        verseCol = verseCol,
                        textCol = textCol,
                        schemaType = schemaType
                    )
                }
            }
            return null
        }

        private fun detectMyBibleNumbering(
            db: SQLiteDatabase,
            tableName: String,
            bookCol: String
        ): Boolean {
            return runCatching {
                db.rawQuery(
                    "SELECT MIN(\"$bookCol\"), MAX(\"$bookCol\") FROM \"$tableName\"",
                    null
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        val minVal = cursor.getInt(0)
                        val maxVal = cursor.getInt(1)
                        maxVal >= 100 || (minVal == 10 && maxVal >= 470)
                    } else false
                }
            }.getOrDefault(false)
        }

        private fun findMatchingColumn(cols: List<String>, candidates: List<String>): String? {
            for (candidate in candidates) {
                val found = cols.firstOrNull { it.equals(candidate, ignoreCase = true) }
                if (found != null) return found
            }
            return null
        }

        private fun readMetadata(db: SQLiteDatabase): Map<String, String> {
            val meta = mutableMapOf<String, String>()
            val tableNames = mutableSetOf<String>()
            db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null).use { cursor ->
                while (cursor.moveToNext()) {
                    cursor.getString(0)?.let { tableNames.add(it.lowercase(Locale.ROOT)) }
                }
            }

            // MyBible 'info' table (name, value)
            if (tableNames.contains("info")) {
                runCatching {
                    db.rawQuery("SELECT name, value FROM info", null).use { cursor ->
                        while (cursor.moveToNext()) {
                            val k = cursor.getString(0)?.trim()?.lowercase(Locale.ROOT).orEmpty()
                            val v = cursor.getString(1)?.trim().orEmpty()
                            if (k.isNotBlank() && v.isNotBlank()) meta[k] = v
                        }
                    }
                }
            }

            // MySword 'Details' / 'details' table (Title, Abbreviation, Description, Language)
            if (tableNames.contains("details")) {
                runCatching {
                    db.rawQuery("SELECT * FROM Details LIMIT 1", null).use { cursor ->
                        if (cursor.moveToFirst()) {
                            for (i in 0 until cursor.columnCount) {
                                val col = cursor.getColumnName(i)?.trim()?.lowercase(Locale.ROOT).orEmpty()
                                val valStr = cursor.getString(i)?.trim().orEmpty()
                                if (col.isNotBlank() && valStr.isNotBlank()) {
                                    meta.putIfAbsent(col, valStr)
                                }
                            }
                        }
                    }
                }
            }
            return meta
        }

        internal fun sanitizeCode(raw: String): String {
            val cleaned = raw.uppercase(Locale.ROOT)
                .replace(Regex("[^A-Z0-9]"), "")
                .take(8)
            return cleaned.ifBlank { "SQLITE" }
        }

        private fun extractShortCode(source: String): String {
            val firstToken = source
                .split(Regex("[\\s_\\-.]+"))
                .firstOrNull { it.any(Char::isLetterOrDigit) }
                .orEmpty()
            return sanitizeCode(firstToken)
        }

        private val txtVerseLineRegex by lazy { Regex("^(.+?)\\s+(\\d+)[:.](\\d+)\\s*[-–:]?\\s+(.+)$") }
        private val txtNumericTsvRegex by lazy { Regex("^(\\d+)\\t(\\d+)\\t(\\d+)\\t(.+)$") }
        private val combiningMarksRegex by lazy { Regex("[\\p{M}]+") }
        private val cleanCharsRegex by lazy { Regex("[.\\s_-]+") }

        private val bookNameToCanonicalNumber: Map<String, Int> by lazy {
            val map = hashMapOf<String, Int>()
            fun norm(s: String): String {
                val nfd = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                return cleanCharsRegex.replace(
                    combiningMarksRegex.replace(nfd, "").lowercase(Locale.ROOT),
                    ""
                )
            }

            BibleCatalogData.allBooks.forEachIndexed { idx, book ->
                val num = idx + 1
                map[book.id.lowercase(Locale.ROOT)] = num
                map[norm(book.name)] = num
                map[norm(book.shortName)] = num
            }

            // Additional Portuguese, English, and common Bible JSON abbreviations
            val aliases = mapOf(
                "gn" to 1, "gen" to 1, "genesis" to 1,
                "ex" to 2, "exo" to 2, "exod" to 2, "exodo" to 2,
                "lv" to 3, "lev" to 3, "levitico" to 3,
                "nm" to 4, "num" to 4, "numeros" to 4,
                "dt" to 5, "deu" to 5, "deut" to 5, "deuteronomio" to 5,
                "js" to 6, "jos" to 6, "josh" to 6, "josue" to 6,
                "jz" to 7, "jdg" to 7, "judg" to 7, "juizes" to 7,
                "rt" to 8, "rut" to 8, "ruth" to 8, "rute" to 8,
                "1sm" to 9, "1sa" to 9, "1sam" to 9, "1samuel" to 9,
                "2sm" to 10, "2sa" to 10, "2sam" to 10, "2samuel" to 10,
                "1rs" to 11, "1ki" to 11, "1kgs" to 11, "1reis" to 11,
                "2rs" to 12, "2ki" to 12, "2kgs" to 12, "2reis" to 12,
                "1cr" to 13, "1ch" to 13, "1chr" to 13, "1cronicas" to 13,
                "2cr" to 14, "2ch" to 14, "2chr" to 14, "2cronicas" to 14,
                "ed" to 15, "ezr" to 15, "ezra" to 15, "esdras" to 15,
                "ne" to 16, "neh" to 16, "neemias" to 16,
                "et" to 17, "est" to 17, "esth" to 17, "ester" to 17,
                "job" to 18,
                "sl" to 19, "slm" to 19, "psa" to 19, "ps" to 19, "salmos" to 19, "salmo" to 19,
                "pv" to 20, "pro" to 20, "prov" to 20, "proverbios" to 20,
                "ec" to 21, "ecc" to 21, "eccl" to 21, "eclesiastes" to 21,
                "ct" to 22, "sng" to 22, "song" to 22, "sol" to 22, "canticos" to 22, "cantares" to 22,
                "is" to 23, "isa" to 23, "isaias" to 23,
                "jr" to 24, "jer" to 24, "jeremias" to 24,
                "lm" to 25, "lam" to 25, "lamentacoes" to 25,
                "ez" to 26, "ezk" to 26, "eze" to 26, "ezequiel" to 26,
                "dn" to 27, "dan" to 27, "daniel" to 27,
                "os" to 28, "hos" to 28, "oseias" to 28,
                "jl" to 29, "jol" to 29, "joe" to 29, "joel" to 29,
                "am" to 30, "amo" to 30, "amos" to 30,
                "ob" to 31, "oba" to 31, "obad" to 31, "obadias" to 31,
                "jn" to 32, "jon" to 32, "jonas" to 32,
                "mq" to 33, "mic" to 33, "miqueias" to 33,
                "na" to 34, "nah" to 34, "nam" to 34, "naum" to 34,
                "hc" to 35, "hab" to 35, "habacuque" to 35,
                "sf" to 36, "zep" to 36, "sofonias" to 36,
                "ag" to 37, "hag" to 37, "ageu" to 37,
                "zc" to 38, "zec" to 38, "zacarias" to 38,
                "ml" to 39, "mal" to 39, "malaquias" to 39,
                "mt" to 40, "mat" to 40, "matt" to 40, "mateus" to 40,
                "mc" to 41, "mrk" to 41, "mar" to 41, "mark" to 41, "marcos" to 41,
                "lc" to 42, "luk" to 42, "luke" to 42, "lucas" to 42,
                "jo" to 43, "jhn" to 43, "joh" to 43, "john" to 43, "joao" to 43,
                "at" to 44, "act" to 44, "acts" to 44, "atos" to 44,
                "rm" to 45, "rom" to 45, "romanos" to 45,
                "1co" to 46, "1cor" to 46, "1corintios" to 46,
                "2co" to 47, "2cor" to 47, "2corintios" to 47,
                "gl" to 48, "gal" to 48, "galatas" to 48,
                "ef" to 49, "eph" to 49, "efesios" to 49,
                "fp" to 50, "php" to 50, "phi" to 50, "phil" to 50, "filipenses" to 50,
                "cl" to 51, "col" to 51, "colossenses" to 51,
                "1ts" to 52, "1th" to 52, "1tessalonicenses" to 52,
                "2ts" to 53, "2th" to 53, "2tessalonicenses" to 53,
                "1tm" to 54, "1ti" to 54, "1tim" to 54, "1timoteo" to 54,
                "2tm" to 55, "2ti" to 55, "2tim" to 55, "2timoteo" to 55,
                "tt" to 56, "tit" to 56, "tito" to 56,
                "fm" to 57, "phm" to 57, "filemom" to 57,
                "hb" to 58, "heb" to 58, "hebreus" to 58,
                "tg" to 59, "jas" to 59, "jam" to 59, "tiago" to 59,
                "1pe" to 60, "1pet" to 60, "1pedro" to 60,
                "2pe" to 61, "2pet" to 61, "2pedro" to 61,
                "1jo" to 62, "1jn" to 62, "1joao" to 62,
                "2jo" to 63, "2jn" to 63, "2joao" to 63,
                "3jo" to 64, "3jn" to 64, "3joao" to 64,
                "jd" to 65, "jud" to 65, "judas" to 65,
                "ap" to 66, "rev" to 66, "apocalipse" to 66
            )
            aliases.forEach { (k, v) -> map[k] = v }
            map
        }

        internal fun resolveCanonicalBookNumber(rawBook: Any?, indexHintOneBased: Int? = null): Int? {
            if (rawBook is Number) {
                val n = rawBook.toInt()
                return if (n in 1..66) n else null
            }
            val rawStr = rawBook?.toString()?.trim().orEmpty()
            rawStr.toIntOrNull()?.let { n ->
                if (n in 1..66) return n
            }
            if (rawStr.equals("Jó", ignoreCase = true) || rawStr.equals("Jô", ignoreCase = true)) {
                return 18
            }
            if (rawStr.equals("jo", ignoreCase = true) && indexHintOneBased == 18) {
                return 18
            }
            val normalized = Normalizer.normalize(rawStr, Normalizer.Form.NFD)
                .let { combiningMarksRegex.replace(it, "") }
                .lowercase(Locale.ROOT)
                .replace(Regex("[.\\s_-]+"), "")
            return bookNameToCanonicalNumber[normalized] ?: indexHintOneBased?.takeIf { it in 1..66 }
        }

        internal fun inspectOrConvertLocalFile(
            id: String,
            sourceFile: File,
            destSqliteFile: File,
            originalFileName: String? = null
        ): Result<PendingSqliteBible> {
            if (!sourceFile.exists() || sourceFile.length() == 0L) {
                return Result.failure(IllegalArgumentException("Arquivo vazio ou ilegível."))
            }

            val headerBytes = ByteArray(16)
            val bytesRead = FileInputStream(sourceFile).use { it.read(headerBytes) }
            val headerStr = if (bytesRead > 0) String(headerBytes, 0, bytesRead, Charsets.US_ASCII) else ""

            if (headerStr.startsWith("SQLite format 3")) {
                if (sourceFile.absolutePath != destSqliteFile.absolutePath) {
                    sourceFile.copyTo(destSqliteFile, overwrite = true)
                }
                return inspectLocalSqliteFile(
                    id = id,
                    file = destSqliteFile,
                    originalFileName = originalFileName
                )
            }

            val trimmedHead = BufferedReader(InputStreamReader(FileInputStream(sourceFile), Charsets.UTF_8)).use { r ->
                val buf = CharArray(256)
                val n = r.read(buf)
                if (n > 0) String(buf, 0, n).removePrefix("\uFEFF").trimStart() else ""
            }

            val lowerName = originalFileName?.lowercase(Locale.ROOT).orEmpty()
            return if (trimmedHead.startsWith("{") || trimmedHead.startsWith("[") || lowerName.endsWith(".json")) {
                convertJsonBibleToSqlite(
                    id = id,
                    jsonFile = sourceFile,
                    destSqliteFile = destSqliteFile,
                    originalFileName = originalFileName
                )
            } else {
                convertPlainTextBibleToSqlite(
                    id = id,
                    textFile = sourceFile,
                    destSqliteFile = destSqliteFile,
                    originalFileName = originalFileName
                )
            }
        }

        internal fun convertJsonBibleToSqlite(
            id: String,
            jsonFile: File,
            destSqliteFile: File,
            originalFileName: String? = null
        ): Result<PendingSqliteBible> {
            return try {
                val rawText = jsonFile.readText(Charsets.UTF_8).removePrefix("\uFEFF").trim()
                if (rawText.isBlank()) {
                    return Result.failure(IllegalArgumentException("Arquivo JSON vazio."))
                }

                var detectedTitle: String? = null
                var detectedCode: String? = null
                var detectedLang: String? = null
                val collected = mutableListOf<Triple<Int, Int, Pair<Int, String>>>() // (book, chap) -> (verse, text)

                fun addVerse(bookNum: Int?, chap: Int, verse: Int, text: String) {
                    if (bookNum != null && bookNum in 1..66 && chap > 0 && verse > 0) {
                        val cleaned = cleanSqliteVerseText(text)
                        if (cleaned.isNotBlank()) {
                            collected.add(Triple(bookNum, chap, verse to cleaned))
                        }
                    }
                }

                fun parseBooksArray(booksArr: JSONArray) {
                    val useIndexFallback = booksArr.length() == 66
                    for (i in 0 until booksArr.length()) {
                        val obj = booksArr.optJSONObject(i) ?: continue
                        val rawAbbrev = obj.opt("abbrev")
                            ?: obj.opt("id")
                            ?: obj.opt("book")
                            ?: obj.opt("book_number")
                            ?: obj.opt("name")
                        val bookNum = resolveCanonicalBookNumber(
                            rawBook = rawAbbrev,
                            indexHintOneBased = if (useIndexFallback) i + 1 else null
                        ) ?: continue

                        val chaptersArr = obj.optJSONArray("chapters")
                        if (chaptersArr != null) {
                            for (cIdx in 0 until chaptersArr.length()) {
                                val chapItem = chaptersArr.opt(cIdx)
                                when (chapItem) {
                                    is JSONArray -> {
                                        val chapNum = cIdx + 1
                                        for (vIdx in 0 until chapItem.length()) {
                                            val vText = chapItem.optString(vIdx, "")
                                            addVerse(bookNum, chapNum, vIdx + 1, vText)
                                        }
                                    }
                                    is JSONObject -> {
                                        val chapNum = chapItem.optInt("chapter", cIdx + 1)
                                        val versesArr = chapItem.optJSONArray("verses")
                                        if (versesArr != null) {
                                            for (vIdx in 0 until versesArr.length()) {
                                                val vEntry = versesArr.opt(vIdx)
                                                when (vEntry) {
                                                    is String -> addVerse(bookNum, chapNum, vIdx + 1, vEntry)
                                                    is JSONObject -> {
                                                        val vNum = vEntry.optInt("verse", vIdx + 1)
                                                        val vText = vEntry.optString("text", "")
                                                        addVerse(bookNum, chapNum, vNum, vText)
                                                    }
                                                }
                                            }
                                        } else {
                                            val keys = chapItem.keys()
                                            while (keys.hasNext()) {
                                                val k = keys.next()
                                                k.toIntOrNull()?.let { vNum ->
                                                    addVerse(bookNum, chapNum, vNum, chapItem.optString(k, ""))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                fun parseFlatVersesArray(arr: JSONArray) {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val rawBook = obj.opt("book_number")
                            ?: obj.opt("book_id")
                            ?: obj.opt("book")
                            ?: obj.opt("abbrev")
                            ?: obj.opt("book_name")
                        val bookNum = resolveCanonicalBookNumber(rawBook)
                        val chap = obj.optInt("chapter", 0)
                        val verse = obj.optInt("verse", 0)
                        val text = obj.optString("text", "").ifBlank {
                            obj.optString("scripture", "")
                        }
                        addVerse(bookNum, chap, verse, text)
                    }
                }

                val root = JSONTokener(rawText).nextValue()
                when (root) {
                    is JSONArray -> {
                        val first = root.optJSONObject(0)
                        if (first != null && first.has("chapters")) {
                            parseBooksArray(root)
                        } else {
                            parseFlatVersesArray(root)
                        }
                    }
                    is JSONObject -> {
                        detectedTitle = root.optString("name", "").ifBlank {
                            root.optString("title", "").ifBlank { root.optString("description", "") }
                        }.takeIf { it.isNotBlank() }
                        detectedCode = root.optString("abbrev", "").ifBlank {
                            root.optString("abbreviation", "").ifBlank { root.optString("version", "") }
                        }.takeIf { it.isNotBlank() }
                        detectedLang = root.optString("language", "").ifBlank {
                            root.optString("lang", "")
                        }.takeIf { it.isNotBlank() }

                        val booksArr = root.optJSONArray("books")
                        val versesArr = root.optJSONArray("verses")
                        when {
                            booksArr != null -> parseBooksArray(booksArr)
                            versesArr != null -> parseFlatVersesArray(versesArr)
                            else -> {
                                // Map of bookKey -> { chapterKey -> { verseKey -> text } }
                                val bKeys = root.keys()
                                while (bKeys.hasNext()) {
                                    val bKey = bKeys.next()
                                    val bookNum = resolveCanonicalBookNumber(bKey) ?: continue
                                    val chapObj = root.optJSONObject(bKey) ?: continue
                                    val cKeys = chapObj.keys()
                                    while (cKeys.hasNext()) {
                                        val cKey = cKeys.next()
                                        val chapNum = cKey.toIntOrNull() ?: continue
                                        when (val vContainer = chapObj.opt(cKey)) {
                                            is JSONObject -> {
                                                val vKeys = vContainer.keys()
                                                while (vKeys.hasNext()) {
                                                    val vKey = vKeys.next()
                                                    val vNum = vKey.toIntOrNull() ?: continue
                                                    addVerse(bookNum, chapNum, vNum, vContainer.optString(vKey, ""))
                                                }
                                            }
                                            is JSONArray -> {
                                                for (vIdx in 0 until vContainer.length()) {
                                                    addVerse(bookNum, chapNum, vIdx + 1, vContainer.optString(vIdx, ""))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (collected.isEmpty()) {
                    return Result.failure(
                        IllegalArgumentException("Nenhum versículo bíblico reconhecido neste arquivo JSON.")
                    )
                }

                val baseFileName = originalFileName?.substringBeforeLast('.')?.trim().orEmpty()
                val title = detectedTitle ?: baseFileName.ifBlank { "Bíblia JSON" }
                val code = sanitizeCode(detectedCode ?: extractShortCode(title))
                val lang = detectedLang ?: "pt"
                val langDisplay = if (lang.lowercase(Locale.ROOT).startsWith("pt")) {
                    "Português · JSON Local"
                } else {
                    "${lang.uppercase(Locale.ROOT)} · JSON Local"
                }

                writeNormalizedSqlite(destSqliteFile, code, title, lang, collected)
                Result.success(
                    PendingSqliteBible(
                        id = id,
                        localFilePath = destSqliteFile.absolutePath,
                        suggestedCode = code,
                        suggestedName = title.take(60),
                        detectedLanguage = langDisplay,
                        schemaType = SCHEMA_VERSES_CANONICAL,
                        versesCount = collected.size
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed converting JSON Bible to SQLite", e)
                Result.failure(
                    Exception("Não foi possível interpretar o JSON bíblico: ${e.localizedMessage ?: "formato inválido"}")
                )
            }
        }

        internal fun convertPlainTextBibleToSqlite(
            id: String,
            textFile: File,
            destSqliteFile: File,
            originalFileName: String? = null
        ): Result<PendingSqliteBible> {
            return try {
                val collected = mutableListOf<Triple<Int, Int, Pair<Int, String>>>()
                BufferedReader(InputStreamReader(FileInputStream(textFile), Charsets.UTF_8), 32768).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        val trimmed = line.removePrefix("\uFEFF").trim()
                        if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                            val numMatch = txtNumericTsvRegex.matchEntire(trimmed)
                            if (numMatch != null) {
                                val b = numMatch.groupValues[1].toIntOrNull()
                                val c = numMatch.groupValues[2].toIntOrNull()
                                val v = numMatch.groupValues[3].toIntOrNull()
                                val t = cleanSqliteVerseText(numMatch.groupValues[4])
                                if (b in 1..66 && c != null && c > 0 && v != null && v > 0 && t.isNotBlank()) {
                                    collected.add(Triple(b!!, c, v to t))
                                }
                            } else {
                                val refMatch = txtVerseLineRegex.matchEntire(trimmed)
                                if (refMatch != null) {
                                    val b = resolveCanonicalBookNumber(refMatch.groupValues[1])
                                    val c = refMatch.groupValues[2].toIntOrNull()
                                    val v = refMatch.groupValues[3].toIntOrNull()
                                    val t = cleanSqliteVerseText(refMatch.groupValues[4])
                                    if (b != null && c != null && c > 0 && v != null && v > 0 && t.isNotBlank()) {
                                        collected.add(Triple(b, c, v to t))
                                    }
                                }
                            }
                        }
                        line = reader.readLine()
                    }
                }

                if (collected.isEmpty()) {
                    return Result.failure(
                        IllegalArgumentException(
                            "Formato não reconhecido. Para Bíblias locais use .sqlite/.db, .json ou .txt no formato 'Livro Cap:Ver Texto'."
                        )
                    )
                }

                val baseFileName = originalFileName?.substringBeforeLast('.')?.trim().orEmpty()
                val title = baseFileName.ifBlank { "Bíblia TXT" }
                val code = sanitizeCode(extractShortCode(title))
                writeNormalizedSqlite(destSqliteFile, code, title, "pt", collected)

                Result.success(
                    PendingSqliteBible(
                        id = id,
                        localFilePath = destSqliteFile.absolutePath,
                        suggestedCode = code,
                        suggestedName = title.take(60),
                        detectedLanguage = "Português · TXT Local",
                        schemaType = SCHEMA_VERSES_CANONICAL,
                        versesCount = collected.size
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed converting TXT Bible to SQLite", e)
                Result.failure(
                    Exception("Não foi possível importar o arquivo TXT: ${e.localizedMessage ?: "formato inválido"}")
                )
            }
        }

        private fun writeNormalizedSqlite(
            destSqliteFile: File,
            code: String,
            title: String,
            language: String,
            verses: List<Triple<Int, Int, Pair<Int, String>>>
        ) {
            if (destSqliteFile.exists()) destSqliteFile.delete()
            SQLiteDatabase.openOrCreateDatabase(destSqliteFile, null).use { db ->
                db.beginTransactionNonExclusive()
                try {
                    db.execSQL("CREATE TABLE info (name TEXT, value TEXT)")
                    db.execSQL("CREATE TABLE verses (book_number INTEGER, chapter INTEGER, verse INTEGER, text TEXT)")
                    db.execSQL("INSERT INTO info VALUES (?, ?)", arrayOf("abbreviation", code))
                    db.execSQL("INSERT INTO info VALUES (?, ?)", arrayOf("description", title))
                    db.execSQL("INSERT INTO info VALUES (?, ?)", arrayOf("language", language))

                    val stmt = db.compileStatement(
                        "INSERT INTO verses (book_number, chapter, verse, text) VALUES (?, ?, ?, ?)"
                    )
                    stmt.use { s ->
                        verses.forEach { (bookNum, chap, vPair) ->
                            s.clearBindings()
                            s.bindLong(1, bookNum.toLong())
                            s.bindLong(2, chap.toLong())
                            s.bindLong(3, vPair.first.toLong())
                            s.bindString(4, vPair.second)
                            s.executeInsert()
                        }
                    }
                    db.execSQL("CREATE INDEX idx_verses_bcv ON verses (book_number, chapter, verse)")
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }
}
