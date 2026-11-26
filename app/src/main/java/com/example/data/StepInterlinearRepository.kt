package com.example.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.model.ImportedInterlinearPack
import com.example.model.InterlinearToken
import com.example.model.StepInterlinearImportProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.UUID

enum class DetectedImportKind {
    BIBLE_TRANSLATION,
    STEP_INTERLINEAR,
    UNKNOWN
}

data class StepInterlinearImportSummary(
    val importedPacks: List<ImportedInterlinearPack>,
    val failedFiles: List<Pair<String, String>>
)

object StepInterlinearParser {

    private val refRegex by lazy { Regex("^([1-3]?[A-Za-z]{2,5})\\.(\\d+)\\.(\\d+)#(\\d+)(?:=([A-Za-z0-9_+.-]+))?$") }
    private val parenTranslitRegex by lazy { Regex("^(.*?)\\s*\\(([^()]+)\\)\\s*$") }
    private val expandedLemmaBracesRegex by lazy { Regex("\\{[^=]+=([^=\\}]+)=") }
    private val expandedLemmaFallbackRegex by lazy { Regex("^[^=]+=([^=]+)=") }
    private val multiSpaceRegex by lazy { Regex("\\s+") }
    private val strongLikeRegex by lazy { Regex("[HG]\\d+") }

    // Maps STEPBible 3-char (and variant) book codes (lowercase) -> BibleCatalogData canonical book.id
    private val stepBookMap: Map<String, String> = mapOf(
        "gen" to "gen",
        "exo" to "exo", "exod" to "exo",
        "lev" to "lev",
        "num" to "num",
        "deu" to "deu", "deut" to "deu",
        "jos" to "jos", "josh" to "jos",
        "jdg" to "jdg", "judg" to "jdg",
        "rut" to "rut", "ruth" to "rut",
        "1sa" to "1sa", "1sam" to "1sa",
        "2sa" to "2sa", "2sam" to "2sa",
        "1ki" to "1ki", "1kgs" to "1ki",
        "2ki" to "2ki", "2kgs" to "2ki",
        "1ch" to "1ch", "1chr" to "1ch",
        "2ch" to "2ch", "2chr" to "2ch",
        "ezr" to "ezr", "ezra" to "ezr",
        "neh" to "neh",
        "est" to "est", "esth" to "est",
        "job" to "job",
        "psa" to "psa", "ps" to "psa",
        "pro" to "pro", "prov" to "pro",
        "ecc" to "ecc", "eccl" to "ecc", "qoh" to "ecc",
        "sng" to "sng", "song" to "sng", "sol" to "sng", "son" to "sng",
        "isa" to "isa",
        "jer" to "jer",
        "lam" to "lam",
        "ezk" to "ezk", "eze" to "ezk", "ezek" to "ezk",
        "dan" to "dan",
        "hos" to "hos",
        "jol" to "jol", "joe" to "jol", "joel" to "jol",
        "amo" to "amo", "amos" to "amo",
        "oba" to "oba", "obad" to "oba",
        "jon" to "jon", "jnh" to "jon",
        "mic" to "mic",
        "nah" to "nah", "nam" to "nah",
        "hab" to "hab",
        "zep" to "zep", "zeph" to "zep",
        "hag" to "hag",
        "zec" to "zec", "zech" to "zec",
        "mal" to "mal",
        // New Testament
        "mat" to "mat", "matt" to "mat",
        "mrk" to "mrk", "mar" to "mrk", "mark" to "mrk",
        "luk" to "luk", "luke" to "luk",
        "jhn" to "jhn", "joh" to "jhn", "john" to "jhn",
        "act" to "act", "acts" to "act",
        "rom" to "rom",
        "1co" to "1co", "1cor" to "1co",
        "2co" to "2co", "2cor" to "2co",
        "gal" to "gal",
        "eph" to "eph",
        "php" to "php", "phi" to "php", "phil" to "php",
        "col" to "col",
        "1th" to "1th", "1thess" to "1th",
        "2th" to "2th", "2thess" to "2th",
        "1ti" to "1ti", "1tim" to "1ti",
        "2ti" to "2ti", "2tim" to "2ti",
        "tit" to "tit", "titus" to "tit",
        "phm" to "phm", "phlm" to "phm",
        "heb" to "heb",
        "jas" to "jas", "jam" to "jas", "jas." to "jas",
        "1pe" to "1pe", "1pet" to "1pe",
        "2pe" to "2pe", "2pet" to "2pe",
        "1jn" to "1jn", "1jo" to "1jn", "1john" to "1jn",
        "2jn" to "2jn", "2jo" to "2jn", "2john" to "2jn",
        "3jn" to "3jn", "3jo" to "3jn", "3john" to "3jn",
        "jud" to "jud", "jude" to "jud",
        "rev" to "rev"
    )

    private val oldTestamentBookIds: Set<String> = BibleCatalogData.allBooks
        .filter { it.testament == com.example.model.Testament.OLD }
        .map { it.id }
        .toSet()

    data class ColumnLayout(
        val refIdx: Int = 0,
        val originalIdx: Int = 1,
        val translitIdx: Int = 2,
        val translationIdx: Int = 3,
        val strongsIdx: Int = 4,
        val grammarIdx: Int = 5,
        val combinedStrongsGrammar: Boolean = false,
        val dictGlossIdx: Int = -1,
        val expandedIdx: Int = 11
    )

    data class ParsedRow(
        val bookId: String,
        val chapter: Int,
        val verse: Int,
        val wordOrder: Int,
        val variantType: String,
        val isHebrew: Boolean,
        val token: InterlinearToken
    )

    data class ParsedVerseBatch(
        val bookId: String,
        val chapter: Int,
        val verse: Int,
        val isHebrew: Boolean,
        val originalLine: String,
        val tokens: List<InterlinearToken>
    )

    data class ParseStats(
        val corpusType: String,
        val booksFound: Set<String>,
        val versesCount: Int,
        val tokensCount: Int
    )

    fun mapStepBookId(rawBookCode: String): String? {
        return stepBookMap[rawBookCode.trim().lowercase(Locale.ROOT)]
    }

    fun cleanOriginalWord(raw: String): String {
        return raw.replace("/", "")
            .replace("\\", "")
            .trim()
    }

    fun cleanTransliteration(raw: String): String {
        return raw.replace("/", "")
            .replace("\\", "")
            .trim()
    }

    fun cleanTranslation(raw: String): String {
        return raw.replace("/", " ")
            .replace("\\", " ")
            .replace(multiSpaceRegex, " ")
            .trim()
    }

    fun cleanStrongsCode(raw: String): String {
        return raw.replace("{", "")
            .replace("}", "")
            .trim()
    }

    fun variantPriority(variantType: String): Int {
        val v = variantType.uppercase(Locale.ROOT)
        return when {
            v == "L" -> 100
            v.contains("N") -> 95
            v == "Q" -> 80
            v == "K" -> 70
            v == "R" -> 60
            v.isBlank() -> 50
            else -> 40
        }
    }

    fun isHeaderLine(trimmedLine: String): Boolean {
        if (trimmedLine.isBlank()) return false
        val cols = trimmedLine.split('\t')
        if (cols.size < 3) return false
        val c0 = cols[0].trim().lowercase(Locale.ROOT)
        if (c0.startsWith("eng (heb) ref") ||
            c0.startsWith("word & type") ||
            c0 == "word" ||
            c0 == "ref" ||
            c0 == "eng ref"
        ) {
            return true
        }
        val lowerLine = trimmedLine.lowercase(Locale.ROOT)
        return lowerLine.contains("\tdstrongs") &&
            (lowerLine.contains("\thebrew") || lowerLine.contains("\tgreek") || lowerLine.contains("\tgrammar"))
    }

    fun detectLayoutFromHeader(headerLine: String): ColumnLayout {
        val cols = headerLine.split('\t').map { it.trim().lowercase(Locale.ROOT) }
        var refIdx = 0
        var originalIdx = -1
        var translitIdx = -1
        var translationIdx = -1
        var strongsIdx = -1
        var grammarIdx = -1
        var combinedStrongsGrammar = false
        var dictGlossIdx = -1
        var expandedIdx = -1

        cols.forEachIndexed { idx, col ->
            when {
                idx == 0 && (col.contains("ref") || col.contains("word")) -> refIdx = 0
                originalIdx < 0 && (col == "hebrew" || col == "greek" || col.startsWith("hebrew") || col.startsWith("greek")) -> {
                    originalIdx = idx
                }
                translitIdx < 0 && (col.contains("translit")) -> {
                    translitIdx = idx
                }
                translationIdx < 0 && (col == "translation" || col == "english" || col == "gloss" || col.contains("translation")) -> {
                    translationIdx = idx
                }
                col.contains("dstrong") && col.contains("grammar") -> {
                    strongsIdx = idx
                    combinedStrongsGrammar = true
                }
                strongsIdx < 0 && (col.contains("dstrong") || col == "strongs" || col == "strong") -> {
                    strongsIdx = idx
                }
                grammarIdx < 0 && (col == "grammar" || col.startsWith("morph")) -> {
                    grammarIdx = idx
                }
                dictGlossIdx < 0 && (col.contains("dictionary form") || col.startsWith("lemma")) -> {
                    dictGlossIdx = idx
                }
                expandedIdx < 0 && col.contains("expanded strong") -> {
                    expandedIdx = idx
                }
            }
        }

        // If header was TAGNT style ("Word & Type \t Greek \t English \t dStrongs = Grammar")
        return ColumnLayout(
            refIdx = refIdx,
            originalIdx = if (originalIdx >= 0) originalIdx else 1,
            translitIdx = translitIdx,
            translationIdx = if (translationIdx >= 0) translationIdx else if (translitIdx >= 0) 3 else 2,
            strongsIdx = if (strongsIdx >= 0) strongsIdx else if (translitIdx >= 0) 4 else 3,
            grammarIdx = if (combinedStrongsGrammar) -1 else if (grammarIdx >= 0) grammarIdx else 5,
            combinedStrongsGrammar = combinedStrongsGrammar,
            dictGlossIdx = dictGlossIdx,
            expandedIdx = expandedIdx
        )
    }

    private fun inferLayoutFromDataColumns(cols: List<String>): ColumnLayout {
        val col3 = cols.getOrNull(3).orEmpty().trim()
        val col4 = cols.getOrNull(4).orEmpty().trim()
        // TAGNT compact style: col1="word (translit)", col2="English", col3="G0976=N-NSF"
        if (col3.contains("=") && strongLikeRegex.containsMatchIn(col3) && !strongLikeRegex.containsMatchIn(col4)) {
            return ColumnLayout(
                refIdx = 0,
                originalIdx = 1,
                translitIdx = -1,
                translationIdx = 2,
                strongsIdx = 3,
                grammarIdx = -1,
                combinedStrongsGrammar = true,
                dictGlossIdx = 4,
                expandedIdx = -1
            )
        }
        // Standard 12-column TAHOT / STEP TSV
        return ColumnLayout(
            refIdx = 0,
            originalIdx = 1,
            translitIdx = 2,
            translationIdx = 3,
            strongsIdx = 4,
            grammarIdx = 5,
            combinedStrongsGrammar = false,
            dictGlossIdx = -1,
            expandedIdx = 11
        )
    }

    fun parseDataLine(line: String, layout: ColumnLayout?): Pair<ParsedRow?, ColumnLayout?> {
        if (line.isBlank()) return null to layout
        val cleanLine = line.removePrefix("\uFEFF")
        val trimmedStart = cleanLine.trimStart()
        if (trimmedStart.startsWith("#")) return null to layout
        if (isHeaderLine(trimmedStart)) {
            return null to detectLayoutFromHeader(cleanLine)
        }

        val cols = when {
            cleanLine.contains('\t') -> cleanLine.split('\t')
            cleanLine.contains(';') -> cleanLine.split(';')
            else -> cleanLine.split(',')
        }
        if (cols.size < 4) return null to layout

        val resolvedLayout = layout ?: inferLayoutFromDataColumns(cols)
        val rawRef = cols.getOrNull(resolvedLayout.refIdx)?.trim().orEmpty()
        val refMatch = refRegex.matchEntire(rawRef) ?: return null to resolvedLayout

        val bookId = mapStepBookId(refMatch.groupValues[1]) ?: return null to resolvedLayout
        val chapter = refMatch.groupValues[2].toIntOrNull() ?: return null to resolvedLayout
        val verse = refMatch.groupValues[3].toIntOrNull() ?: return null to resolvedLayout
        val wordOrder = refMatch.groupValues[4].toIntOrNull() ?: return null to resolvedLayout
        val variantType = refMatch.groupValues[5]
        if (chapter <= 0 || verse <= 0) return null to resolvedLayout

        val rawOriginal = cols.getOrNull(resolvedLayout.originalIdx).orEmpty()
        var originalWord = cleanOriginalWord(rawOriginal)
        var transliteration = if (resolvedLayout.translitIdx >= 0) {
            cleanTransliteration(cols.getOrNull(resolvedLayout.translitIdx).orEmpty())
        } else {
            ""
        }

        if (transliteration.isBlank()) {
            val parenMatch = parenTranslitRegex.matchEntire(originalWord)
            if (parenMatch != null) {
                originalWord = cleanOriginalWord(parenMatch.groupValues[1])
                transliteration = cleanTransliteration(parenMatch.groupValues[2])
            }
        }
        if (originalWord.isBlank()) return null to resolvedLayout

        val translation = cleanTranslation(cols.getOrNull(resolvedLayout.translationIdx).orEmpty())

        val rawStrongsCol = cols.getOrNull(resolvedLayout.strongsIdx).orEmpty().trim()
        val strongId: String
        val morphology: String
        if (resolvedLayout.combinedStrongsGrammar || (resolvedLayout.grammarIdx < 0 && rawStrongsCol.contains("="))) {
            val eqIdx = rawStrongsCol.indexOf('=')
            if (eqIdx >= 0) {
                strongId = cleanStrongsCode(rawStrongsCol.substring(0, eqIdx))
                morphology = rawStrongsCol.substring(eqIdx + 1).trim()
            } else {
                strongId = cleanStrongsCode(rawStrongsCol)
                morphology = cols.getOrNull(resolvedLayout.grammarIdx)?.trim().orEmpty()
            }
        } else {
            strongId = cleanStrongsCode(rawStrongsCol)
            morphology = if (resolvedLayout.grammarIdx >= 0) {
                cols.getOrNull(resolvedLayout.grammarIdx)?.trim().orEmpty()
            } else {
                ""
            }
        }

        val lemma = extractLemma(
            expandedCol = if (resolvedLayout.expandedIdx >= 0) cols.getOrNull(resolvedLayout.expandedIdx).orEmpty() else "",
            dictGlossCol = if (resolvedLayout.dictGlossIdx >= 0) cols.getOrNull(resolvedLayout.dictGlossIdx).orEmpty() else "",
            fallbackOriginal = originalWord
        )

        val isHebrew = bookId in oldTestamentBookIds
        return ParsedRow(
            bookId = bookId,
            chapter = chapter,
            verse = verse,
            wordOrder = wordOrder,
            variantType = variantType,
            isHebrew = isHebrew,
            token = InterlinearToken(
                originalWord = originalWord,
                transliteration = transliteration,
                portugueseGloss = translation,
                strongId = strongId,
                morphology = morphology,
                lemma = lemma
            )
        ) to resolvedLayout
    }

    private fun extractLemma(
        expandedCol: String,
        dictGlossCol: String,
        fallbackOriginal: String
    ): String {
        if (expandedCol.isNotBlank()) {
            expandedLemmaBracesRegex.find(expandedCol)?.groupValues?.getOrNull(1)?.trim()
                ?.takeIf { it.isNotBlank() }?.let { return cleanOriginalWord(it) }
            expandedLemmaFallbackRegex.find(expandedCol)?.groupValues?.getOrNull(1)?.trim()
                ?.takeIf { it.isNotBlank() }?.let { return cleanOriginalWord(it) }
        }
        if (dictGlossCol.isNotBlank()) {
            val beforeEq = dictGlossCol.substringBefore('=').trim()
            if (beforeEq.isNotBlank()) return cleanOriginalWord(beforeEq)
        }
        return fallbackOriginal
    }

    fun parseStream(
        inputStream: InputStream,
        onVerseCompleted: (ParsedVerseBatch) -> Unit
    ): ParseStats {
        var layout: ColumnLayout? = null
        var currentBookId: String? = null
        var currentChapter = -1
        var currentVerse = -1
        var currentIsHebrew = false
        val currentWords = linkedMapOf<Int, Pair<Int, InterlinearToken>>()

        val booksFound = linkedSetOf<String>()
        var versesCount = 0
        var tokensCount = 0
        var hebrewVerses = 0
        var greekVerses = 0

        fun flushCurrentVerse() {
            val bId = currentBookId ?: return
            if (currentWords.isEmpty()) return
            val orderedTokens = currentWords.entries
                .sortedBy { it.key }
                .map { it.value.second }
            val originalLine = orderedTokens.joinToString(" ") { it.originalWord }
            onVerseCompleted(
                ParsedVerseBatch(
                    bookId = bId,
                    chapter = currentChapter,
                    verse = currentVerse,
                    isHebrew = currentIsHebrew,
                    originalLine = originalLine,
                    tokens = orderedTokens
                )
            )
            booksFound.add(bId)
            versesCount++
            tokensCount += orderedTokens.size
            if (currentIsHebrew) hebrewVerses++ else greekVerses++
            currentWords.clear()
        }

        val buffered = BufferedInputStream(inputStream, 65536)
        buffered.mark(4096)
        val probe = ByteArray(1024)
        val probeLen = buffered.read(probe)
        buffered.reset()
        val head = if (probeLen > 0) {
            String(probe, 0, probeLen, Charsets.UTF_8).removePrefix("\uFEFF").trimStart()
        } else {
            ""
        }

        if (head.startsWith("{") || head.startsWith("[")) {
            return parseJsonStream(
                inputStream = buffered,
                onRow = { parsed ->
                    if (currentBookId != null &&
                        (parsed.bookId != currentBookId ||
                            parsed.chapter != currentChapter ||
                            parsed.verse != currentVerse)
                    ) {
                        flushCurrentVerse()
                    }
                    currentBookId = parsed.bookId
                    currentChapter = parsed.chapter
                    currentVerse = parsed.verse
                    currentIsHebrew = parsed.isHebrew

                    val prio = variantPriority(parsed.variantType)
                    val existing = currentWords[parsed.wordOrder]
                    if (existing == null || prio > existing.first) {
                        currentWords[parsed.wordOrder] = prio to parsed.token
                    }
                },
                onFinish = {
                    flushCurrentVerse()
                    val corpusType = when {
                        hebrewVerses > 0 && greekVerses == 0 -> "TAHOT · JSON (AT)"
                        greekVerses > 0 && hebrewVerses == 0 -> "TAGNT · JSON (NT)"
                        else -> "Interlinear JSON"
                    }
                    ParseStats(
                        corpusType = corpusType,
                        booksFound = booksFound,
                        versesCount = versesCount,
                        tokensCount = tokensCount
                    )
                }
            )
        }

        BufferedReader(InputStreamReader(buffered, Charsets.UTF_8), 65536).use { reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                val (parsed, updatedLayout) = parseDataLine(line, layout)
                layout = updatedLayout
                if (parsed != null) {
                    if (currentBookId != null &&
                        (parsed.bookId != currentBookId ||
                            parsed.chapter != currentChapter ||
                            parsed.verse != currentVerse)
                    ) {
                        flushCurrentVerse()
                    }
                    currentBookId = parsed.bookId
                    currentChapter = parsed.chapter
                    currentVerse = parsed.verse
                    currentIsHebrew = parsed.isHebrew

                    val prio = variantPriority(parsed.variantType)
                    val existing = currentWords[parsed.wordOrder]
                    if (existing == null || prio > existing.first) {
                        currentWords[parsed.wordOrder] = prio to parsed.token
                    }
                }
                line = reader.readLine()
            }
            flushCurrentVerse()
        }

        val corpusType = when {
            hebrewVerses > 0 && greekVerses == 0 -> "TAHOT (Hebraico AT)"
            greekVerses > 0 && hebrewVerses == 0 -> "TAGNT (Grego NT)"
            else -> "STEP Interlinear"
        }

        return ParseStats(
            corpusType = corpusType,
            booksFound = booksFound,
            versesCount = versesCount,
            tokensCount = tokensCount
        )
    }

    private fun parseJsonStream(
        inputStream: InputStream,
        onRow: (ParsedRow) -> Unit,
        onFinish: () -> ParseStats
    ): ParseStats {
        val raw = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8), 65536)
            .use { it.readText() }
            .removePrefix("\uFEFF")
            .trim()
        if (raw.isBlank()) return onFinish()

        val root = runCatching { JSONTokener(raw).nextValue() }.getOrNull() ?: return onFinish()
        val verseRefRegex = Regex("^([1-3]?[A-Za-z]{2,5})[.\\s]+(\\d+)[:.](\\d+)$")

        fun emitTokenObject(
            obj: JSONObject,
            fallbackBook: String? = null,
            fallbackChap: Int = 0,
            fallbackVerse: Int = 0,
            fallbackOrder: Int = 1
        ) {
            val rawRef = obj.optString("ref", "").ifBlank {
                obj.optString("wordRef", "").ifBlank {
                    obj.optString("Eng (Heb) Ref & Type", "").ifBlank {
                        obj.optString("Word & Type", "")
                    }
                }
            }.trim()

            var bookId: String? = null
            var chap = fallbackChap
            var verse = fallbackVerse
            var wordOrder = fallbackOrder
            var variant = ""

            val refMatch = refRegex.matchEntire(rawRef)
            if (refMatch != null) {
                bookId = mapStepBookId(refMatch.groupValues[1])
                chap = refMatch.groupValues[2].toIntOrNull() ?: 0
                verse = refMatch.groupValues[3].toIntOrNull() ?: 0
                wordOrder = refMatch.groupValues[4].toIntOrNull() ?: fallbackOrder
                variant = refMatch.groupValues[5]
            } else {
                val rawBook = obj.optString("bookId", "").ifBlank {
                    obj.optString("book", "").ifBlank { fallbackBook.orEmpty() }
                }
                bookId = mapStepBookId(rawBook)
                    ?: SqliteBibleRepository.resolveCanonicalBookNumber(rawBook)?.let { idx ->
                        BibleCatalogData.allBooks.getOrNull(idx - 1)?.id
                    }
                if (chap <= 0) chap = obj.optInt("chapter", 0)
                if (verse <= 0) verse = obj.optInt("verse", 0)
                wordOrder = obj.optInt("order", obj.optInt("wordOrder", fallbackOrder))
            }

            if (bookId == null || chap <= 0 || verse <= 0) return

            val rawOrig = obj.optString("hebrew", "").ifBlank {
                obj.optString("greek", "").ifBlank {
                    obj.optString("originalWord", "").ifBlank {
                        obj.optString("original", "").ifBlank {
                            obj.optString("word", "").ifBlank { obj.optString("Hebrew", obj.optString("Greek", "")) }
                        }
                    }
                }
            }
            var orig = cleanOriginalWord(rawOrig)
            var translit = cleanTransliteration(
                obj.optString("transliteration", "").ifBlank {
                    obj.optString("translit", "").ifBlank { obj.optString("Transliteration", "") }
                }
            )
            if (translit.isBlank()) {
                val parenMatch = parenTranslitRegex.matchEntire(orig)
                if (parenMatch != null) {
                    orig = cleanOriginalWord(parenMatch.groupValues[1])
                    translit = cleanTransliteration(parenMatch.groupValues[2])
                }
            }
            if (orig.isBlank()) return

            val gloss = cleanTranslation(
                obj.optString("translation", "").ifBlank {
                    obj.optString("portugueseGloss", "").ifBlank {
                        obj.optString("gloss", "").ifBlank {
                            obj.optString("english", "").ifBlank { obj.optString("Translation", obj.optString("English", "")) }
                        }
                    }
                }
            )

            val rawStrong = obj.optString("dStrongs", "").ifBlank {
                obj.optString("strongId", "").ifBlank {
                    obj.optString("strongs", "").ifBlank { obj.optString("strong", "") }
                }
            }.trim()
            val rawGrammar = obj.optString("grammar", "").ifBlank {
                obj.optString("morphology", "").ifBlank {
                    obj.optString("morph", "").ifBlank { obj.optString("Grammar", "") }
                }
            }.trim()

            val (strongId, morph) = if (rawGrammar.isBlank() && rawStrong.contains("=")) {
                val eq = rawStrong.indexOf('=')
                cleanStrongsCode(rawStrong.substring(0, eq)) to rawStrong.substring(eq + 1).trim()
            } else {
                cleanStrongsCode(rawStrong) to rawGrammar
            }

            val lemma = cleanOriginalWord(
                obj.optString("lemma", "").ifBlank {
                    obj.optString("root", "").ifBlank { orig }
                }
            )

            onRow(
                ParsedRow(
                    bookId = bookId,
                    chapter = chap,
                    verse = verse,
                    wordOrder = wordOrder,
                    variantType = variant,
                    isHebrew = bookId in oldTestamentBookIds,
                    token = InterlinearToken(
                        originalWord = orig,
                        transliteration = translit,
                        portugueseGloss = gloss,
                        strongId = strongId,
                        morphology = morph,
                        lemma = lemma
                    )
                )
            )
        }

        fun processTokenOrVerseArray(arr: JSONArray) {
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val nestedTokens = item.optJSONArray("tokens")
                    ?: item.optJSONArray("interlinear")
                    ?: item.optJSONArray("words")
                if (nestedTokens != null) {
                    val b = item.optString("bookId", item.optString("book", ""))
                    val c = item.optInt("chapter", 0)
                    val v = item.optInt("verse", 0)
                    for (tIdx in 0 until nestedTokens.length()) {
                        val tObj = nestedTokens.optJSONObject(tIdx) ?: continue
                        emitTokenObject(tObj, b, c, v, tIdx + 1)
                    }
                } else {
                    emitTokenObject(item, fallbackOrder = i + 1)
                }
            }
        }

        when (root) {
            is JSONArray -> processTokenOrVerseArray(root)
            is JSONObject -> {
                val arr = root.optJSONArray("words")
                    ?: root.optJSONArray("tokens")
                    ?: root.optJSONArray("verses")
                    ?: root.optJSONArray("data")
                    ?: root.optJSONArray("rows")
                if (arr != null) {
                    processTokenOrVerseArray(arr)
                } else {
                    // Keyed by verse ref e.g. "Gen.1.1": [ {...} ] or TSV lines as keys
                    var keyFallbackLayout: ColumnLayout? = null
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val refMatch = verseRefRegex.matchEntire(key.trim())
                        val valArr = root.optJSONArray(key)
                        if (refMatch != null && valArr != null) {
                            val b = mapStepBookId(refMatch.groupValues[1])
                            val c = refMatch.groupValues[2].toIntOrNull() ?: 0
                            val v = refMatch.groupValues[3].toIntOrNull() ?: 0
                            for (tIdx in 0 until valArr.length()) {
                                val tObj = valArr.optJSONObject(tIdx) ?: continue
                                emitTokenObject(tObj, b, c, v, tIdx + 1)
                            }
                        } else if (key.contains('\t')) {
                            // Handle JSON where TSV lines were stored as keys/strings
                            key.lineSequence().forEach { line ->
                                val (parsed, updated) = parseDataLine(line, keyFallbackLayout)
                                keyFallbackLayout = updated
                                if (parsed != null) onRow(parsed)
                            }
                        }
                    }
                }
            }
        }
        return onFinish()
    }

    private val inlineStepRefRegex = Regex("(^|\\s|[\\{\"'])[1-3]?[A-Za-z]{2,5}\\.\\d+\\.\\d+#\\d+")

    fun classifyFileHead(headBytes: ByteArray, fileName: String? = null): DetectedImportKind {
        val lowerName = fileName?.trim()?.lowercase(Locale.ROOT).orEmpty()

        if (headBytes.size >= 15) {
            val ascii = String(headBytes, 0, minOf(headBytes.size, 16), Charsets.US_ASCII)
            if (ascii.startsWith("SQLite format 3")) {
                return DetectedImportKind.BIBLE_TRANSLATION
            }
        }

        val text = headBytes.toString(Charsets.UTF_8).removePrefix("\uFEFF").trimStart()
        if (text.isBlank()) {
            return when {
                lowerName.endsWith(".sqlite") ||
                    lowerName.endsWith(".sqlite3") ||
                    lowerName.endsWith(".db") ||
                    lowerName.endsWith(".mybible") -> DetectedImportKind.BIBLE_TRANSLATION
                lowerName.endsWith(".tsv") ||
                    lowerName.contains("tahot") ||
                    lowerName.contains("tagnt") -> DetectedImportKind.STEP_INTERLINEAR
                else -> DetectedImportKind.UNKNOWN
            }
        }

        val lowerText = text.lowercase(Locale.ROOT)
        if (text.startsWith("{") || text.startsWith("[")) {
            val looksInterlinearJson = inlineStepRefRegex.containsMatchIn(text) ||
                lowerText.contains("\"dstrongs\"") ||
                lowerText.contains("\"transliteration\"") ||
                lowerText.contains("\"translit\"") ||
                lowerText.contains("\"portuguesegloss\"") ||
                lowerText.contains("\"morphology\"") ||
                lowerText.contains("\"interlinear\"") ||
                lowerText.contains("\"tokens\"") ||
                lowerText.contains("eng (heb) ref") ||
                lowerText.contains("word & type") ||
                ((lowerText.contains("\"hebrew\"") || lowerText.contains("\"greek\"")) &&
                    (lowerText.contains("\"translation\"") || lowerText.contains("\"grammar\""))) ||
                lowerName.contains("tahot") ||
                lowerName.contains("tagnt")
            return if (looksInterlinearJson) {
                DetectedImportKind.STEP_INTERLINEAR
            } else {
                DetectedImportKind.BIBLE_TRANSLATION
            }
        }

        if (inlineStepRefRegex.containsMatchIn(text)) {
            return DetectedImportKind.STEP_INTERLINEAR
        }

        val lines = text.lineSequence().take(100)
        for (line in lines) {
            val trimmed = line.removePrefix("\uFEFF").trim()
            if (trimmed.isBlank()) continue
            if (isHeaderLine(trimmed)) return DetectedImportKind.STEP_INTERLINEAR
            val firstCol = trimmed.split('\t', ';', ',').firstOrNull()?.trim().orEmpty()
            if (refRegex.matches(firstCol)) return DetectedImportKind.STEP_INTERLINEAR
        }

        if (lowerName.endsWith(".tsv") || lowerName.contains("tahot") || lowerName.contains("tagnt")) {
            return DetectedImportKind.STEP_INTERLINEAR
        }

        return DetectedImportKind.BIBLE_TRANSLATION
    }
}

class StepInterlinearRepository(
    private val context: Context? = null,
    private val customDbFile: File? = null
) {

    private class InterlinearDbHelper(
        context: Context?,
        dbNameOrPath: String?
    ) : SQLiteOpenHelper(context, dbNameOrPath, null, DB_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS interlinear_packs (
                    id TEXT PRIMARY KEY,
                    file_name TEXT NOT NULL,
                    corpus_type TEXT NOT NULL,
                    books_summary TEXT NOT NULL,
                    verses_count INTEGER NOT NULL,
                    tokens_count INTEGER NOT NULL,
                    imported_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS interlinear_verses (
                    book_id TEXT NOT NULL,
                    chapter INTEGER NOT NULL,
                    verse INTEGER NOT NULL,
                    pack_id TEXT NOT NULL,
                    is_hebrew INTEGER NOT NULL,
                    original_line TEXT NOT NULL,
                    tokens_blob TEXT NOT NULL,
                    PRIMARY KEY (book_id, chapter, verse)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_interlinear_book_chap ON interlinear_verses (book_id, chapter)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_interlinear_pack ON interlinear_verses (pack_id)"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

        companion object {
            const val DB_VERSION = 1
        }
    }

    private val dbLock = Any()
    private val _packsFlow = MutableStateFlow<List<ImportedInterlinearPack>>(emptyList())
    private val _availableBookIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    private val helper: InterlinearDbHelper? by lazy {
        when {
            customDbFile != null -> InterlinearDbHelper(context, customDbFile.absolutePath)
            context != null -> {
                val dbFile = File(context.filesDir, "step_interlinear.db")
                InterlinearDbHelper(context, dbFile.absolutePath)
            }
            else -> null
        }
    }



    fun observePacks(): Flow<List<ImportedInterlinearPack>> = _packsFlow.asStateFlow()

    fun observeAvailableBookIds(): Flow<Set<String>> = _availableBookIdsFlow.asStateFlow()

    suspend fun refreshCoverage() = withContext(Dispatchers.IO) {
        refreshCachedCoverage()
    }

    private fun refreshCachedCoverage() {
        runCatching {
            val db = helper?.readableDatabase ?: return
            synchronized(dbLock) {
                _packsFlow.value = queryPacksLocked(db)
                _availableBookIdsFlow.value = queryBookIdsLocked(db)
            }
        }.onFailure { err ->
            Log.e(TAG, "Failed query packs or book ids", err)
        }
    }

    suspend fun getAvailableChapters(bookId: String): Set<Int> = withContext(Dispatchers.IO) {
        val db = helper?.readableDatabase ?: return@withContext emptySet()
        synchronized(dbLock) {
            val result = hashSetOf<Int>()
            db.rawQuery(
                "SELECT DISTINCT chapter FROM interlinear_verses WHERE book_id = ?",
                arrayOf(bookId)
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    result.add(cursor.getInt(0))
                }
            }
            result
        }
    }

    suspend fun readChapter(
        bookId: String,
        chapter: Int
    ): Map<Int, Pair<String, List<InterlinearToken>>> = withContext(Dispatchers.IO) {
        val db = helper?.readableDatabase ?: return@withContext emptyMap()
        synchronized(dbLock) {
            val map = linkedMapOf<Int, Pair<String, List<InterlinearToken>>>()
            db.rawQuery(
                "SELECT verse, original_line, tokens_blob FROM interlinear_verses " +
                    "WHERE book_id = ? AND chapter = ? ORDER BY verse ASC",
                arrayOf(bookId, chapter.toString())
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val verseNum = cursor.getInt(0)
                    val originalLine = cursor.getString(1).orEmpty()
                    val blob = cursor.getString(2).orEmpty()
                    val tokens = decodeTokensBlob(blob)
                    if (verseNum > 0 && (originalLine.isNotBlank() || tokens.isNotEmpty())) {
                        map[verseNum] = originalLine to tokens
                    }
                }
            }
            map
        }
    }

    suspend fun classifyUris(uris: List<Uri>): Map<DetectedImportKind, List<Uri>> = withContext(Dispatchers.IO) {
        val appContext = context ?: return@withContext emptyMap()
        val grouped = linkedMapOf<DetectedImportKind, MutableList<Uri>>()
        uris.forEach { uri ->
            val name = resolveDisplayName(appContext, uri)
            val headBytes = runCatching {
                appContext.contentResolver.openInputStream(uri)?.use { stream ->
                    val buf = ByteArray(16384)
                    var total = 0
                    while (total < buf.size) {
                        val read = stream.read(buf, total, buf.size - total)
                        if (read <= 0) break
                        total += read
                    }
                    buf.copyOf(total)
                }
            }.getOrNull() ?: ByteArray(0)
            val kind = StepInterlinearParser.classifyFileHead(headBytes, name)
            grouped.getOrPut(kind) { mutableListOf() }.add(uri)
        }
        grouped
    }

    suspend fun importUris(
        uris: List<Uri>,
        onProgress: (StepInterlinearImportProgress) -> Unit = {}
    ): StepInterlinearImportSummary = withContext(Dispatchers.IO) {
        val appContext = context ?: return@withContext StepInterlinearImportSummary(
            importedPacks = emptyList(),
            failedFiles = listOf("Arquivo" to "Contexto do aplicativo indisponível.")
        )
        val imported = mutableListOf<ImportedInterlinearPack>()
        val failed = mutableListOf<Pair<String, String>>()

        uris.forEachIndexed { index, uri ->
            val displayName = resolveDisplayName(appContext, uri) ?: "arquivo_${index + 1}.txt"
            onProgress(
                StepInterlinearImportProgress(
                    currentFileIndex = index + 1,
                    totalFiles = uris.size,
                    currentFileName = displayName,
                    parsedVersesCount = 0,
                    parsedTokensCount = 0
                )
            )
            try {
                val inputStream = appContext.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    failed.add(displayName to "Não foi possível abrir o arquivo.")
                } else {
                    inputStream.use { stream ->
                        val packResult = importStreamInternal(
                            fileName = displayName,
                            inputStream = stream,
                            onProgressVerses = { vCount, tCount ->
                                onProgress(
                                    StepInterlinearImportProgress(
                                        currentFileIndex = index + 1,
                                        totalFiles = uris.size,
                                        currentFileName = displayName,
                                        parsedVersesCount = vCount,
                                        parsedTokensCount = tCount
                                    )
                                )
                            }
                        )
                        packResult.onSuccess { pack ->
                            imported.add(pack)
                        }.onFailure { err ->
                            failed.add(displayName to (err.localizedMessage ?: "Formato inválido"))
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error importing STEPBible file $displayName", e)
                failed.add(displayName to (e.localizedMessage ?: "Erro de leitura"))
            }
        }

        refreshCachedCoverage()
        StepInterlinearImportSummary(importedPacks = imported, failedFiles = failed)
    }

    suspend fun importStream(
        fileName: String,
        inputStream: InputStream
    ): Result<ImportedInterlinearPack> = withContext(Dispatchers.IO) {
        val result = importStreamInternal(fileName = fileName, inputStream = inputStream)
        refreshCachedCoverage()
        result
    }

    private fun importStreamInternal(
        fileName: String,
        inputStream: InputStream,
        onProgressVerses: (Int, Int) -> Unit = { _, _ -> }
    ): Result<ImportedInterlinearPack> {
        val db = helper?.writableDatabase ?: return Result.failure(
            IllegalStateException("Banco local interlinear indisponível.")
        )
        val packId = "step_${UUID.randomUUID().toString().replace("-", "").take(10)}"

        synchronized(dbLock) {
            // Remove any prior pack with the exact same filename to avoid orphaned pack entries
            val priorPackIds = mutableListOf<String>()
            db.rawQuery(
                "SELECT id FROM interlinear_packs WHERE file_name = ?",
                arrayOf(fileName)
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    priorPackIds.add(cursor.getString(0))
                }
            }

            var insertedVerses = 0
            var insertedTokens = 0

            val insertSql =
                "INSERT OR REPLACE INTO interlinear_verses " +
                    "(book_id, chapter, verse, pack_id, is_hebrew, original_line, tokens_blob) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"

            db.beginTransactionNonExclusive()
            var stmt: SQLiteStatement? = null
            try {
                priorPackIds.forEach { oldId ->
                    db.delete("interlinear_verses", "pack_id = ?", arrayOf(oldId))
                    db.delete("interlinear_packs", "id = ?", arrayOf(oldId))
                }

                stmt = db.compileStatement(insertSql)
                val stats = StepInterlinearParser.parseStream(inputStream) { verseBatch ->
                    val s = stmt!!
                    s.clearBindings()
                    s.bindString(1, verseBatch.bookId)
                    s.bindLong(2, verseBatch.chapter.toLong())
                    s.bindLong(3, verseBatch.verse.toLong())
                    s.bindString(4, packId)
                    s.bindLong(5, if (verseBatch.isHebrew) 1L else 0L)
                    s.bindString(6, verseBatch.originalLine)
                    s.bindString(7, encodeTokensBlob(verseBatch.tokens))
                    s.executeInsert()

                    insertedVerses++
                    insertedTokens += verseBatch.tokens.size
                    if (insertedVerses % 250 == 0) {
                        onProgressVerses(insertedVerses, insertedTokens)
                    }
                    if (insertedVerses % 2000 == 0) {
                        db.setTransactionSuccessful()
                        db.endTransaction()
                        db.beginTransactionNonExclusive()
                        stmt?.close()
                        stmt = db.compileStatement(insertSql)
                    }
                }

                if (stats.versesCount <= 0) {
                    return Result.failure(
                        IllegalArgumentException(
                            "Nenhum dado interlinear STEPBible (TAHOT/TAGNT) encontrado em \"$fileName\"."
                        )
                    )
                }

                val booksSummary = formatBooksSummary(stats.booksFound)
                val now = System.currentTimeMillis()
                val packStmt = db.compileStatement(
                    "INSERT OR REPLACE INTO interlinear_packs " +
                        "(id, file_name, corpus_type, books_summary, verses_count, tokens_count, imported_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)"
                )
                packStmt.use { ps ->
                    ps.bindString(1, packId)
                    ps.bindString(2, fileName)
                    ps.bindString(3, stats.corpusType)
                    ps.bindString(4, booksSummary)
                    ps.bindLong(5, stats.versesCount.toLong())
                    ps.bindLong(6, stats.tokensCount.toLong())
                    ps.bindLong(7, now)
                    ps.executeInsert()
                }

                db.setTransactionSuccessful()
                onProgressVerses(stats.versesCount, stats.tokensCount)

                return Result.success(
                    ImportedInterlinearPack(
                        id = packId,
                        fileName = fileName,
                        corpusType = stats.corpusType,
                        booksSummary = booksSummary,
                        versesCount = stats.versesCount,
                        tokensCount = stats.tokensCount,
                        importedAtMillis = now
                    )
                )
            } finally {
                stmt?.close()
                if (db.inTransaction()) {
                    db.endTransaction()
                }
            }
        }
    }

    suspend fun deletePack(packId: String) = withContext(Dispatchers.IO) {
        val db = helper?.writableDatabase ?: return@withContext
        synchronized(dbLock) {
            db.beginTransactionNonExclusive()
            try {
                db.delete("interlinear_verses", "pack_id = ?", arrayOf(packId))
                db.delete("interlinear_packs", "id = ?", arrayOf(packId))
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            _packsFlow.value = queryPacksLocked(db)
            _availableBookIdsFlow.value = queryBookIdsLocked(db)
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        val db = helper?.writableDatabase ?: return@withContext
        synchronized(dbLock) {
            db.beginTransactionNonExclusive()
            try {
                db.delete("interlinear_verses", null, null)
                db.delete("interlinear_packs", null, null)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            _packsFlow.value = emptyList()
            _availableBookIdsFlow.value = emptySet()
        }
    }

    fun close() {
        runCatching { helper?.close() }
    }

    private fun queryPacksLocked(db: SQLiteDatabase): List<ImportedInterlinearPack> {
        val packs = mutableListOf<ImportedInterlinearPack>()
        db.rawQuery(
            "SELECT id, file_name, corpus_type, books_summary, verses_count, tokens_count, imported_at " +
                "FROM interlinear_packs ORDER BY imported_at DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                packs.add(
                    ImportedInterlinearPack(
                        id = cursor.getString(0),
                        fileName = cursor.getString(1),
                        corpusType = cursor.getString(2),
                        booksSummary = cursor.getString(3),
                        versesCount = cursor.getInt(4),
                        tokensCount = cursor.getInt(5),
                        importedAtMillis = cursor.getLong(6)
                    )
                )
            }
        }
        return packs
    }

    private fun queryBookIdsLocked(db: SQLiteDatabase): Set<String> {
        val ids = hashSetOf<String>()
        db.rawQuery("SELECT DISTINCT book_id FROM interlinear_verses", null).use { cursor ->
            while (cursor.moveToNext()) {
                ids.add(cursor.getString(0))
            }
        }
        return ids
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

    private fun formatBooksSummary(bookIds: Set<String>): String {
        if (bookIds.isEmpty()) return ""
        val names = bookIds.mapNotNull { id ->
            BibleCatalogData.allBooks.firstOrNull { it.id == id }?.shortName
        }
        return when {
            names.isEmpty() -> "${bookIds.size} livros"
            names.size == 1 -> names.first()
            else -> "${names.first()}–${names.last()} (${names.size} livros)"
        }
    }

    companion object {
        private const val TAG = "StepInterlinearRepo"
        private const val TOKEN_SEP = "\u001E"
        private const val FIELD_SEP = "\u001F"

        private fun sanitizeBlobField(value: String): String {
            return value.replace(TOKEN_SEP, " ").replace(FIELD_SEP, " ")
        }

        internal fun encodeTokensBlob(tokens: List<InterlinearToken>): String {
            return tokens.joinToString(TOKEN_SEP) { t ->
                listOf(
                    sanitizeBlobField(t.originalWord),
                    sanitizeBlobField(t.transliteration),
                    sanitizeBlobField(t.portugueseGloss),
                    sanitizeBlobField(t.strongId),
                    sanitizeBlobField(t.morphology),
                    sanitizeBlobField(t.lemma)
                ).joinToString(FIELD_SEP)
            }
        }

        internal fun decodeTokensBlob(blob: String): List<InterlinearToken> {
            if (blob.isBlank()) return emptyList()
            return blob.split(TOKEN_SEP).mapNotNull { entry ->
                if (entry.isBlank()) return@mapNotNull null
                val parts = entry.split(FIELD_SEP)
                if (parts.size < 5) return@mapNotNull null
                InterlinearToken(
                    originalWord = parts[0],
                    transliteration = parts[1],
                    portugueseGloss = parts[2],
                    strongId = parts[3],
                    morphology = parts[4],
                    lemma = parts.getOrElse(5) { parts[0] }
                )
            }
        }
    }
}
