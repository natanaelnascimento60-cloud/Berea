package com.example.data

import android.util.Log
import com.example.model.BibleTranslation
import com.example.model.BibleVerse
import com.example.model.InterlinearToken
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.RequestOptions
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

data class GeneratedReadingPlanPayload(
    val title: String,
    val description: String,
    val readings: List<String>
)

internal class FirstTokenTimeoutException(
    val modelLabel: String,
    val timeoutMs: Long
) : Exception("O modelo $modelLabel não iniciou a resposta em ${timeoutMs}ms.")

class TheoAiRepository {

    private val exegesisSystemInstruction by lazy {
        content {
            text(
                "Você é uma bancada de exegese bíblica acadêmica (estilo Logos/Accordance/Nestle-Aland) em português. " +
                    "Seja direto, claro e estruturado (cerca de 200 a 360 palavras), apresentando com rigor histórico-gramatical: " +
                    "texto original (Grego Koiné ou Hebraico Bíblico) com transliteração e código Strong, " +
                    "análise morfológica e sintática essencial, ocorrências principais e notas breves de crítica textual ou traduções."
            )
        }
    }

    private val exegesisModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = PRIMARY_EXEGESIS_MODEL,
            generationConfig = generationConfig {
                maxOutputTokens = 1400
                thinkingConfig = thinkingConfig {
                    thinkingLevel = PRIMARY_EXEGESIS_THINKING_LEVEL
                }
            },
            systemInstruction = exegesisSystemInstruction,
            requestOptions = RequestOptions(45_000L)
        )
    }

    private val fastExegesisModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = FAST_EXEGESIS_MODEL,
            generationConfig = generationConfig {
                maxOutputTokens = 1400
                thinkingConfig = thinkingConfig {
                    thinkingLevel = FAST_EXEGESIS_THINKING_LEVEL
                }
            },
            systemInstruction = exegesisSystemInstruction,
            requestOptions = RequestOptions(40_000L)
        )
    }

    private val readingPlanSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "title" to Schema.string(description = "Título curto e acadêmico do plano em português"),
                "description" to Schema.string(description = "Descrição concisa do foco exegético do plano em 1 frase"),
                "readings" to Schema.array(
                    items = Schema.string(description = "Leitura diária no formato 'Dia X: Livro Cap — Foco exegético e termo original'"),
                    description = "Lista ordenada de leituras diárias"
                )
            )
        )
    }

    private val readingPlanModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = FAST_EXEGESIS_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = readingPlanSchema
                thinkingConfig = thinkingConfig {
                    thinkingLevel = FAST_EXEGESIS_THINKING_LEVEL
                }
            },
            requestOptions = RequestOptions(50_000L)
        )
    }

    private val chapterSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "verses" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "verse" to Schema.integer(description = "Número do versículo"),
                            "originalTextLine" to Schema.string(description = "Texto original completo do versículo em Grego Koiné (NT) ou Hebraico Bíblico (AT)"),
                            "tb" to Schema.string(description = "Texto em português (estilo Bíblia Livre / Almeida)"),
                            "commentary" to Schema.string(description = "Comentário exegético conciso (2 frases) em português"),
                            "textualNote" to Schema.string(description = "Nota breve de crítica textual ou sintaxe"),
                            "interlinear" to Schema.array(
                                items = Schema.obj(
                                    properties = mapOf(
                                        "originalWord" to Schema.string(description = "Palavra no original grego ou hebraico"),
                                        "transliteration" to Schema.string(description = "Transliteração acadêmica"),
                                        "portugueseGloss" to Schema.string(description = "Glosa em português"),
                                        "strongId" to Schema.string(description = "Código Strong G... ou H..."),
                                        "morphology" to Schema.string(description = "Código morfológico ex: V-AAP-NMS ou V-Qal-Perf-3ms"),
                                        "lemma" to Schema.string(description = "Forma lexical/lema")
                                    )
                                ),
                                description = "4 a 5 termos originais principais do versículo"
                            )
                        ),
                        optionalProperties = listOf("tb", "textualNote")
                    ),
                    description = "Versículos-chave do capítulo com texto original, análise interlinear e comentário exegético"
                )
            )
        )
    }

    private val chapterStudyModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = FAST_EXEGESIS_MODEL,
            generationConfig = generationConfig {
                maxOutputTokens = 2600
                responseMimeType = "application/json"
                responseSchema = chapterSchema
                thinkingConfig = thinkingConfig {
                    thinkingLevel = FAST_EXEGESIS_THINKING_LEVEL
                }
            },
            requestOptions = RequestOptions(50_000L)
        )
    }

    fun streamTheologicalResponse(prompt: String): Flow<String> = buildResilientTextStream(
        primaryStream = { onUpdate ->
            collectModelStream(
                model = exegesisModel,
                modelLabel = PRIMARY_EXEGESIS_MODEL,
                prompt = prompt,
                firstTokenTimeoutMs = PRIMARY_FIRST_TOKEN_TIMEOUT_MS,
                totalTimeoutMs = PRIMARY_TOTAL_TIMEOUT_MS,
                onAccumulated = onUpdate
            )
        },
        fallbackStream = { onUpdate ->
            collectModelStream(
                model = fastExegesisModel,
                modelLabel = FAST_EXEGESIS_MODEL,
                prompt = prompt,
                firstTokenTimeoutMs = FALLBACK_FIRST_TOKEN_TIMEOUT_MS,
                totalTimeoutMs = FALLBACK_TOTAL_TIMEOUT_MS,
                onAccumulated = onUpdate
            )
        },
        unaryFallback = {
            withTimeout(UNARY_FALLBACK_TIMEOUT_MS) {
                val response = fastExegesisModel.generateContent(prompt)
                response.text.orEmpty()
            }
        }
    ).flowOn(Dispatchers.IO)

    private suspend fun collectModelStream(
        model: GenerativeModel,
        modelLabel: String,
        prompt: String,
        firstTokenTimeoutMs: Long,
        totalTimeoutMs: Long,
        onAccumulated: suspend (String) -> Unit
    ): String {
        val accumulated = StringBuilder()
        val gotFirstToken = AtomicBoolean(false)
        val timedOutFirstToken = AtomicBoolean(false)
        val startMs = System.currentTimeMillis()

        withTimeout(totalTimeoutMs) {
            coroutineScope {
                val streamJob = launch {
                    model.generateContentStream(prompt).collect { chunk ->
                        val text = chunk.text.orEmpty()
                        if (text.isNotEmpty()) {
                            if (gotFirstToken.compareAndSet(false, true)) {
                                Log.d(TAG, "$modelLabel first token in ${System.currentTimeMillis() - startMs}ms")
                            }
                            accumulated.append(text)
                            onAccumulated(accumulated.toString())
                        }
                    }
                }
                val watchdogJob = launch {
                    delay(firstTokenTimeoutMs)
                    if (!gotFirstToken.get() && streamJob.isActive) {
                        timedOutFirstToken.set(true)
                        Log.w(TAG, "$modelLabel produced no tokens in ${firstTokenTimeoutMs}ms")
                        streamJob.cancel(
                            CancellationException("Sem resposta inicial em ${firstTokenTimeoutMs}ms")
                        )
                    }
                }
                try {
                    streamJob.join()
                } finally {
                    watchdogJob.cancel()
                }
            }
        }

        if (timedOutFirstToken.get() && accumulated.isBlank()) {
            throw FirstTokenTimeoutException(modelLabel, firstTokenTimeoutMs)
        }
        return accumulated.toString()
    }

    fun streamWordOccurrencesAnalysis(query: String): Flow<String> {
        val prompt = """
            Faça uma análise completa de CONCORDÂNCIA E OCORRÊNCIAS BÍBLICAS para o termo/código: "$query".
            Estruture exatamente assim:
            1. Lema Original (Grego Koiné e/ou Hebraico Bíblico), transliteração e código Strong.
            2. Contagem aproximada de ocorrências no AT/LXX e no NT, com distribuição pelos principais blocos ou livros bíblicos.
            3. Variações de sentido (campo semântico) em diferentes autores bíblicos.
            4. Lista comentada de 5 a 7 ocorrências-chave (referência bíblica, forma morfológica na passagem e impacto exegético).
        """.trimIndent()
        return streamTheologicalResponse(prompt)
    }

    fun streamOriginalWordStrongAnalysis(
        word: String,
        verseRef: String,
        originalLine: String,
        isOldTestament: Boolean
    ): Flow<String> {
        val lang = if (isOldTestament) "Hebraico Bíblico" else "Grego Koiné"
        val prompt = """
            Analise a palavra "$word" ($lang) conforme aparece em $verseRef:
            Texto original do versículo: "$originalLine"

            Responda de forma objetiva e acadêmica em português com estas 4 seções:
            1. Análise Morfológica: classe gramatical, tempo/aspecto/tronco, voz, modo, caso/gênero/número e função sintática na oração.
            2. Raiz & Lema: forma de dicionário (lema), transliteração e raiz etimológica.
            3. Léxico Strong: código Strong (H... ou G...), glosa principal e definição léxica completa.
            4. Uso na Passagem: nuance teológica e frequência aproximada nas Escrituras.
        """.trimIndent()
        return streamTheologicalResponse(prompt)
    }

    suspend fun generateCustomReadingPlan(
        focusTopic: String,
        days: Int,
        pace: String
    ): Result<GeneratedReadingPlanPayload> {
        return try {
            val safeDays = days.coerceIn(3, 30)
            val prompt = """
                Crie um plano de leitura e exegese bíblica personalizado em português.
                Tema/Livro: $focusTopic
                Duração: $safeDays dias
                Ritmo: $pace
                Retorne exatamente $safeDays itens no array 'readings', numerados de Dia 1 até Dia $safeDays, incluindo referência bíblica e um termo grego ou hebraico relevante para análise exegética.
            """.trimIndent()

            val response = withContext(Dispatchers.IO) {
                withTimeout(55_000L) {
                    readingPlanModel.generateContent(prompt)
                }
            }
            val jsonStr = response.text ?: throw IllegalStateException("Resposta vazia.")
            val json = JSONObject(jsonStr)
            val title = json.getString("title")
            val description = json.getString("description")
            val readingsJson = json.getJSONArray("readings")
            val readings = (0 until readingsJson.length()).map { readingsJson.getString(it) }
            Result.success(GeneratedReadingPlanPayload(title, description, readings))
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Reading plan generation timed out", e)
            Result.failure(Exception("Tempo limite excedido ao gerar o plano. Tente novamente."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Reading plan generation failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchChapterStudyOnDemand(
        bookId: String,
        bookName: String,
        chapter: Int,
        isOldTestament: Boolean,
        targetVerseNumbers: List<Int> = listOf(1, 2, 3)
    ): Result<List<BibleVerse>> {
        return try {
            val langLabel = if (isOldTestament) "Hebraico Bíblico (TM, código Strong H...)" else "Grego Koiné (NA28, código Strong G...)"
            val verseListText = targetVerseNumbers.take(4).joinToString(", ")
            val prompt = """
                Forneça o estudo exegético dos versículos [$verseListText] de $bookName capítulo $chapter em formato compacto:
                - Texto original do versículo em $langLabel
                - Análise interlinear de 4 a 5 palavras-chave em $langLabel com código Strong e morfologia
                - Comentário exegético conciso (2 frases) e nota breve de crítica textual/sintaxe em português.
            """.trimIndent()

            val response = withContext(Dispatchers.IO) {
                withTimeout(55_000L) {
                    chapterStudyModel.generateContent(prompt)
                }
            }
            val jsonStr = response.text ?: throw IllegalStateException("Resposta vazia ao carregar capítulo.")
            val root = JSONObject(jsonStr)
            val versesArr = root.getJSONArray("verses")
            val parsed = (0 until versesArr.length()).map { idx ->
                val vObj = versesArr.getJSONObject(idx)
                val interArr = vObj.optJSONArray("interlinear")
                val tokens = if (interArr != null) {
                    (0 until interArr.length()).map { tIdx ->
                        val tObj = interArr.getJSONObject(tIdx)
                        InterlinearToken(
                            originalWord = tObj.optString("originalWord"),
                            transliteration = tObj.optString("transliteration"),
                            portugueseGloss = tObj.optString("portugueseGloss"),
                            strongId = tObj.optString("strongId"),
                            morphology = tObj.optString("morphology"),
                            lemma = tObj.optString("lemma")
                        )
                    }
                } else emptyList()

                val tbText = vObj.optString("tb")

                BibleVerse(
                    bookId = bookId,
                    bookName = bookName,
                    chapter = chapter,
                    verse = vObj.optInt("verse", idx + 1),
                    originalTextLine = vObj.optString("originalTextLine"),
                    translations = buildMap {
                        if (tbText.isNotBlank()) put(BibleTranslation.BLIVRE, tbText)
                    },
                    interlinear = tokens,
                    commentary = vObj.optString("commentary"),
                    textualNote = vObj.optString("textualNote")
                )
            }
            Result.success(parsed)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Chapter study timed out", e)
            Result.failure(Exception("Tempo limite excedido ao consultar exegese do capítulo."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Chapter study failed", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "TheoAiRepository"
        internal const val PRIMARY_EXEGESIS_MODEL = "gemini-3.8-flash"
        internal val PRIMARY_EXEGESIS_THINKING_LEVEL = ThinkingLevel.LOW
        internal const val FAST_EXEGESIS_MODEL = "gemini-3.5-flash-lite"
        internal val FAST_EXEGESIS_THINKING_LEVEL = ThinkingLevel.MINIMAL

        internal const val PRIMARY_FIRST_TOKEN_TIMEOUT_MS = 12_000L
        internal const val PRIMARY_TOTAL_TIMEOUT_MS = 40_000L
        internal const val FALLBACK_FIRST_TOKEN_TIMEOUT_MS = 14_000L
        internal const val FALLBACK_TOTAL_TIMEOUT_MS = 35_000L
        internal const val UNARY_FALLBACK_TIMEOUT_MS = 20_000L

        internal fun buildResilientTextStream(
            primaryStream: suspend (onUpdate: suspend (String) -> Unit) -> String,
            fallbackStream: suspend (onUpdate: suspend (String) -> Unit) -> String,
            unaryFallback: suspend () -> String
        ): Flow<String> = channelFlow {
            executeResilientTextStream(
                primaryStream = primaryStream,
                fallbackStream = fallbackStream,
                unaryFallback = unaryFallback,
                onChunk = { accumulated -> send(accumulated) }
            )
        }

        internal suspend fun executeResilientTextStream(
            primaryStream: suspend (onUpdate: suspend (String) -> Unit) -> String,
            fallbackStream: suspend (onUpdate: suspend (String) -> Unit) -> String,
            unaryFallback: suspend () -> String,
            onChunk: suspend (String) -> Unit
        ) {
            var emittedAny = false
            var primaryError: Throwable? = null

            try {
                val primaryResult = primaryStream { accumulated ->
                    if (accumulated.isNotBlank()) {
                        emittedAny = true
                        onChunk(accumulated)
                    }
                }
                if (primaryResult.isNotBlank()) return
            } catch (e: TimeoutCancellationException) {
                currentCoroutineContext().ensureActive()
                if (emittedAny) throw e
                primaryError = e
                Log.w(TAG, "Primary model timed out before emitting tokens; switching to fast fallback", e)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (emittedAny) throw e
                primaryError = e
                Log.w(TAG, "Primary model failed before emitting tokens; switching to fast fallback", e)
            }

            var fallbackError: Throwable? = null
            try {
                val fallbackResult = fallbackStream { accumulated ->
                    if (accumulated.isNotBlank()) {
                        emittedAny = true
                        onChunk(accumulated)
                    }
                }
                if (fallbackResult.isNotBlank()) return
            } catch (e: TimeoutCancellationException) {
                currentCoroutineContext().ensureActive()
                if (emittedAny) throw e
                fallbackError = e
                Log.w(TAG, "Fast fallback stream timed out before emitting tokens; trying unary fallback", e)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (emittedAny) throw e
                fallbackError = e
                Log.w(TAG, "Fast fallback stream failed; trying unary fallback", e)
            }

            try {
                val unaryResult = unaryFallback()
                if (unaryResult.isNotBlank()) {
                    onChunk(unaryResult)
                    return
                }
            } catch (e: TimeoutCancellationException) {
                currentCoroutineContext().ensureActive()
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unary fallback failed", e)
                throw e
            }

            throw (fallbackError ?: primaryError ?: IllegalStateException("O modelo retornou uma resposta vazia."))
        }
    }
}
