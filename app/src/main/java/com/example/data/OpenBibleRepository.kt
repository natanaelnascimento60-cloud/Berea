package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.BibleTranslation
import com.example.model.BibleVerse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class OpenBibleRepository(
    context: Context? = null,
    private val callFactory: Call.Factory? = null
) {
    private val client: Call.Factory by lazy {
        callFactory ?: OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .apply {
                if (context != null) {
                    val cacheDir = File(context.cacheDir, "open_bible_http_cache")
                    cache(Cache(cacheDir, 16L * 1024L * 1024L))
                }
            }
            .build()
    }

    private val chapterCache = ConcurrentHashMap<String, List<BibleVerse>>()

    fun getCachedFullChapter(bookId: String, chapter: Int): List<BibleVerse>? {
        return chapterCache["$bookId:$chapter"]
    }

    suspend fun fetchFullChapter(
        bookId: String,
        bookName: String,
        chapter: Int,
        bundledScholarlyVerses: List<BibleVerse> = emptyList()
    ): Result<List<BibleVerse>> {
        val cacheKey = "$bookId:$chapter"
        chapterCache[cacheKey]?.let { cached ->
            return Result.success(cached)
        }

        val bookNumber = canonicalBookNumber(bookId)
            ?: return Result.failure(IllegalArgumentException("Livro desconhecido: $bookId"))

        return try {
            val merged = withContext(Dispatchers.IO) {
                coroutineScope {
                    val blivreDeferred = async {
                        fetchGetBiblePortuguese(
                            translationSlug = "livre",
                            bookNumber = bookNumber,
                            chapter = chapter
                        )
                    }
                    val bltrDeferred = async {
                        fetchGetBiblePortuguese(
                            translationSlug = "livretr",
                            bookNumber = bookNumber,
                            chapter = chapter
                        )
                    }
                    val almDeferred = async {
                        fetchGetBiblePortuguese(
                            translationSlug = "almeida",
                            bookNumber = bookNumber,
                            chapter = chapter
                        )
                    }

                    val blivreVerses = blivreDeferred.await()
                    val bltrVerses = bltrDeferred.await()
                    val almVerses = almDeferred.await()

                    mergeChapterSources(
                        bookId = bookId,
                        bookName = bookName,
                        chapter = chapter,
                        blivreVerses = blivreVerses,
                        bltrVerses = bltrVerses,
                        almVerses = almVerses,
                        bundledScholarlyVerses = bundledScholarlyVerses
                    )
                }
            }

            if (merged.isEmpty()) {
                Result.failure(
                    IllegalStateException(
                        "Não foi possível baixar o texto completo de $bookName $chapter. Verifique sua conexão ou importe uma Bíblia SQLite offline."
                    )
                )
            } else {
                chapterCache[cacheKey] = merged
                Result.success(merged)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch full chapter $bookId $chapter", e)
            Result.failure(
                Exception("Não foi possível carregar $bookName $chapter: ${e.localizedMessage ?: "erro de rede"}")
            )
        }
    }

    private fun fetchGetBiblePortuguese(
        translationSlug: String,
        bookNumber: Int,
        chapter: Int
    ): Map<Int, String> {
        val url = "https://api.getbible.net/v2/$translationSlug/$bookNumber/$chapter.json"
        return try {
            val body = executeGet(url) ?: return emptyMap()
            parseGetBibleChapterJson(body)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Failed fetching GetBible $translationSlug $bookNumber:$chapter", e)
            emptyMap()
        }
    }

    private fun executeGet(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "BereaExegeseApp/1.0 (Android)")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "HTTP ${response.code} for $url")
                return null
            }
            return response.body?.string()
        }
    }

    companion object {
        private const val TAG = "OpenBibleRepository"

        private val HTML_SUP_REGEX by lazy { Regex("<sup[^>]*>.*?</sup>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)) }
        private val HTML_STRONG_TAG_REGEX by lazy { Regex("<S>.*?</S>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)) }
        private val HTML_TAG_REGEX by lazy { Regex("<[^>]+>") }
        private val MULTI_SPACE_REGEX by lazy { Regex("\\s+") }

        private val bookNumberById: Map<String, Int> by lazy {
            BibleCatalogData.allBooks
                .mapIndexed { index, book -> book.id to (index + 1) }
                .toMap()
        }

        fun canonicalBookNumber(bookId: String): Int? = bookNumberById[bookId]

        internal fun cleanVerseHtml(raw: String): String {
            if (raw.isBlank()) return ""
            return raw
                .replace(HTML_SUP_REGEX, "")
                .replace(HTML_STRONG_TAG_REGEX, "")
                .replace("<br>", " ", ignoreCase = true)
                .replace("<br/>", " ", ignoreCase = true)
                .replace("<br />", " ", ignoreCase = true)
                .replace(HTML_TAG_REGEX, "")
                .replace("&nbsp;", " ")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace(MULTI_SPACE_REGEX, " ")
                .trim()
        }

        internal fun parseGetBibleChapterJson(json: String): Map<Int, String> {
            val trimmed = json.trim()
            if (!trimmed.startsWith("{")) return emptyMap()
            val root = JSONObject(trimmed)
            val lang = root.optString("lang").trim().lowercase()
            if (lang.isNotEmpty() && !lang.startsWith("pt")) {
                Log.w(TAG, "Rejecting non-Portuguese GetBible payload lang=$lang")
                return emptyMap()
            }
            val versesArr = root.optJSONArray("verses") ?: return emptyMap()
            val result = linkedMapOf<Int, String>()
            for (i in 0 until versesArr.length()) {
                val obj = versesArr.optJSONObject(i) ?: continue
                val verseNum = obj.optInt("verse", -1)
                val text = cleanVerseHtml(obj.optString("text"))
                if (verseNum > 0 && text.isNotBlank()) {
                    result[verseNum] = text
                }
            }
            return result
        }

        internal fun mergeChapterSources(
            bookId: String,
            bookName: String,
            chapter: Int,
            blivreVerses: Map<Int, String>,
            bltrVerses: Map<Int, String>,
            almVerses: Map<Int, String>,
            bundledScholarlyVerses: List<BibleVerse>
        ): List<BibleVerse> {
            val bundledByVerse = bundledScholarlyVerses.associateBy { it.verse }
            val allVerseNumbers = sortedSetOf<Int>().apply {
                addAll(blivreVerses.keys)
                addAll(bltrVerses.keys)
                addAll(almVerses.keys)
                addAll(bundledByVerse.keys)
            }

            return allVerseNumbers.mapNotNull { verseNum ->
                val bundled = bundledByVerse[verseNum]
                val translations = buildMap {
                    val blivreText = blivreVerses[verseNum] ?: bundled?.translations?.get(BibleTranslation.BLIVRE)
                    val bltrText = bltrVerses[verseNum] ?: bundled?.translations?.get(BibleTranslation.BLTR)
                    val almText = almVerses[verseNum] ?: bundled?.translations?.get(BibleTranslation.ALM)
                    if (!blivreText.isNullOrBlank()) put(BibleTranslation.BLIVRE, blivreText)
                    if (!bltrText.isNullOrBlank()) put(BibleTranslation.BLTR, bltrText)
                    if (!almText.isNullOrBlank()) put(BibleTranslation.ALM, almText)
                }

                if (translations.isEmpty() && bundled == null) return@mapNotNull null

                BibleVerse(
                    bookId = bookId,
                    bookName = bookName,
                    chapter = chapter,
                    verse = verseNum,
                    originalTextLine = bundled?.originalTextLine.orEmpty(),
                    translations = translations,
                    customTranslations = bundled?.customTranslations.orEmpty(),
                    interlinear = bundled?.interlinear.orEmpty(),
                    commentary = bundled?.commentary.orEmpty(),
                    textualNote = bundled?.textualNote.orEmpty(),
                    crossReferences = bundled?.crossReferences.orEmpty()
                )
            }
        }
    }
}
