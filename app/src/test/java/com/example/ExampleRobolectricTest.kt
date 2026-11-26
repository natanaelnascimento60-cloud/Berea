package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.BibleCatalogData
import com.example.data.LocalStudyDatabase
import com.example.data.ScholarlyBibleVersesData
import com.example.data.ScholarlyLibraryData
import com.example.data.TheoAiRepository
import com.example.data.UserStudyRepository
import com.example.model.Testament
import com.google.firebase.ai.type.ThinkingLevel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Berea", appName)
    }

    @Test
    fun `catalog contains all 66 canonical books and daily study`() {
        assertEquals(66, BibleCatalogData.allBooks.size)
        assertEquals(39, BibleCatalogData.allBooks.count { it.testament == Testament.OLD })
        assertEquals(27, BibleCatalogData.allBooks.count { it.testament == Testament.NEW })
        val daily = BibleCatalogData.getDailyStudyForToday()
        assertTrue(daily.verseText.isNotBlank())
        assertTrue(daily.originalWord.isNotBlank())
        assertTrue(daily.originalExcerpt.isNotBlank())
        assertTrue(daily.occurrencesCount > 0)
    }

    @Test
    fun `scholarly datasets provide original lines interlinear lexicons and occurrences`() {
        val verses = ScholarlyBibleVersesData.getVerses("1pe", 1)
        assertTrue(verses.isNotEmpty())
        assertTrue(verses.first().originalTextLine.isNotBlank())
        assertTrue(verses.first().interlinear.isNotEmpty())
        val g313 = ScholarlyLibraryData.lexiconEntries.first { it.strongId == "G313" }
        assertTrue(g313.bookDistribution.isNotEmpty())
        assertTrue(g313.sampleOccurrences.isNotEmpty())
        assertTrue(ScholarlyLibraryData.commentaries.isNotEmpty())
        assertTrue(ScholarlyLibraryData.dictionaryEntries.isNotEmpty())
        assertTrue(ScholarlyLibraryData.findStrongEntry("G2128") != null)
    }

    @Test
    fun `morphology decoder and original word matcher parse Greek and Hebrew`() {
        val greekDecoded = com.example.model.MorphologyDecoder.decode(
            "V-AAP-NMS",
            com.example.model.OriginalLanguage.GREEK
        )
        assertTrue(greekDecoded.any { it.label == "Classe" && it.value == "Verbo" })
        assertTrue(greekDecoded.any { it.label == "Tempo/Aspecto" && it.value.contains("Aoristo") })
        assertTrue(greekDecoded.any { it.label == "Caso" && it.value.contains("Nominativo") })

        val hebrewDecoded = com.example.model.MorphologyDecoder.decode(
            "V-Qal-Perf-3ms",
            com.example.model.OriginalLanguage.HEBREW
        )
        assertTrue(hebrewDecoded.any { it.label == "Tronco (Binyan)" && it.value.contains("Qal") })

        val verses1Pe = ScholarlyBibleVersesData.getVerses("1pe", 1)
        val matched = com.example.model.MorphologyDecoder.findMatchingToken(
            "ἀναγεννήσας",
            verses1Pe.first().interlinear
        )
        assertEquals("G313", matched?.strongId)
    }

    @Test
    fun `fallback tokens, morphology inference, and all bundled passages resolve Strong entries`() {
        val kyriou = ScholarlyLibraryData.findFallbackToken("Κυρίου", isOldTestament = false)
        assertEquals("G2962", kyriou?.strongId)
        assertTrue(ScholarlyLibraryData.findStrongEntry("G2962") != null)

        val elohim = ScholarlyLibraryData.findFallbackToken("אֱלֹהִים", isOldTestament = true)
        assertEquals("H430", elohim?.strongId)

        assertEquals(
            "λόγος",
            com.example.model.MorphologyDecoder.stripPunctuation("λόγος·׃־,")
        )

        val g313 = ScholarlyLibraryData.findStrongEntry("G313")!!
        val inferredG313 = com.example.model.MorphologyDecoder.inferLexicalAttributes(g313)
        assertTrue(inferredG313.isNotEmpty())

        assertTrue(ScholarlyBibleVersesData.bundledPassages.size >= 15)
        ScholarlyBibleVersesData.bundledPassages.forEach { passage ->
            assertTrue(
                "Missing bundled chapter for ${passage.label}",
                ScholarlyBibleVersesData.hasBundledChapter(passage.bookId, passage.chapter)
            )
            val verses = ScholarlyBibleVersesData.getVerses(passage.bookId, passage.chapter)
            assertTrue("Empty verses for ${passage.label}", verses.isNotEmpty())
            verses.forEach { verse ->
                assertTrue(verse.originalTextLine.isNotBlank())
                assertTrue(verse.interlinear.isNotEmpty())
                verse.interlinear.forEach { token ->
                    assertTrue(
                        "Missing Strong entry ${token.strongId} in ${passage.label}",
                        ScholarlyLibraryData.findStrongEntry(token.strongId) != null
                    )
                }
            }
        }
    }

    @Test
    fun `local room study repository works without login`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, LocalStudyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = UserStudyRepository(db.studyDao())
            val userId = "local_scholar"
            assertTrue(repo.ensureUserProfileAndStarterPlan(userId, "Estudante").isSuccess)

            val profile = repo.observeUserProfile(userId).first()
            assertEquals("Estudante", profile?.displayName)

            val plans = repo.observeReadingPlans(userId).first()
            assertEquals(1, plans.size)
            assertEquals("Fundamentos da Esperança e do Verbo", plans.first().title)

            assertTrue(repo.addNote(userId, "1 Pedro 1:3", "Regeneração", "Particípio aoristo ἀναγεννήσας", "Morfologia").isSuccess)
            val notes = repo.observeNotes(userId).first()
            assertEquals(1, notes.size)
            assertEquals("Regeneração", notes.first().title)

            assertTrue(repo.togglePlanDayCompletion(userId, plans.first(), 1L, profile).isSuccess)
            val updatedPlans = repo.observeReadingPlans(userId).first()
            assertTrue(updatedPlans.first().completedDays.contains(1L))
        } finally {
            db.close()
        }
    }

    @Test
    fun `theo ai model configs and resilient channelFlow handle child emissions and fallback`() = runTest {
        // Verify supported Gemini 3.x thinking level pairings (gemini-3.8-flash rejects MINIMAL)
        assertEquals("gemini-3.8-flash", TheoAiRepository.PRIMARY_EXEGESIS_MODEL)
        assertTrue(TheoAiRepository.PRIMARY_EXEGESIS_THINKING_LEVEL != ThinkingLevel.MINIMAL)
        assertEquals("gemini-3.5-flash-lite", TheoAiRepository.FAST_EXEGESIS_MODEL)
        assertEquals(ThinkingLevel.MINIMAL, TheoAiRepository.FAST_EXEGESIS_THINKING_LEVEL)

        // Scenario 1: Primary stream emits from a child launch coroutine (channelFlow safety check)
        val primaryChunks = TheoAiRepository.buildResilientTextStream(
            primaryStream = { onUpdate ->
                coroutineScope {
                    launch {
                        onUpdate("ἐν ἀρχῇ ")
                        onUpdate("ἐν ἀρχῇ ἦν ὁ λόγος")
                    }.join()
                }
                "ἐν ἀρχῇ ἦν ὁ λόγος"
            },
            fallbackStream = { error("Should not fall back when primary succeeds") },
            unaryFallback = { error("Should not reach unary fallback") }
        ).toList()
        assertEquals(listOf("ἐν ἀρχῇ ", "ἐν ἀρχῇ ἦν ὁ λόγος"), primaryChunks)

        // Scenario 2: Primary stream times out before first token -> fallback child-coroutine stream succeeds
        val streamChunks = TheoAiRepository.buildResilientTextStream(
            primaryStream = {
                withTimeout(10L) {
                    delay(100L)
                    ""
                }
            },
            fallbackStream = { onUpdate ->
                coroutineScope {
                    launch {
                        onUpdate("Análise de ἀναγεννάω (G313)")
                    }.join()
                }
                "Análise de ἀναγεννάω (G313)"
            },
            unaryFallback = { error("Should not reach unary fallback") }
        ).toList()
        assertEquals(listOf("Análise de ἀναγεννάω (G313)"), streamChunks)

        // Scenario 3: Both streams stall before emitting -> unary fallback succeeds
        val unaryChunks = TheoAiRepository.buildResilientTextStream(
            primaryStream = {
                withTimeout(1L) {
                    delay(50L)
                    ""
                }
            },
            fallbackStream = { throw RuntimeException("SSE interrupted") },
            unaryFallback = { "Exegese recuperada via modo direto." }
        ).toList()
        assertEquals(listOf("Exegese recuperada via modo direto."), unaryChunks)
    }

    @Test
    fun `only Portuguese Bible translations are included, foreign payloads rejected, and full chapters merge`() {
        val codes = com.example.model.BibleTranslation.entries.map { it.code }
        assertEquals(listOf("BLIVRE", "BLTR", "ALM"), codes)
        assertEquals(
            com.example.model.BibleTranslation.BLIVRE,
            com.example.model.BibleTranslation.fromCodeOrDefault("NVI")
        )

        assertEquals(1, com.example.data.OpenBibleRepository.canonicalBookNumber("gen"))
        assertEquals(19, com.example.data.OpenBibleRepository.canonicalBookNumber("psa"))
        assertEquals(43, com.example.data.OpenBibleRepository.canonicalBookNumber("jhn"))
        assertEquals(60, com.example.data.OpenBibleRepository.canonicalBookNumber("1pe"))
        assertEquals(66, com.example.data.OpenBibleRepository.canonicalBookNumber("rev"))

        val nonPortugueseJson = """
            {
              "lang": "id",
              "verses": [
                {"verse": 1, "text": "Pada mulanya Allah menciptakan langit dan bumi."}
              ]
            }
        """.trimIndent()
        assertTrue(com.example.data.OpenBibleRepository.parseGetBibleChapterJson(nonPortugueseJson).isEmpty())

        val getBiblePortugueseSample = """
            {
              "book_name": "João",
              "lang": "pt",
              "chapter": 1,
              "verses": [
                {"verse": 1, "text": "No princípio era o <i>Verbo</i>.<sup>1</sup>"},
                {"verse": 2, "text": "Ele estava no princípio com Deus."}
              ]
            }
        """.trimIndent()
        val parsedAlm = com.example.data.OpenBibleRepository.parseGetBibleChapterJson(getBiblePortugueseSample)
        assertEquals(2, parsedAlm.size)
        assertEquals("No princípio era o Verbo.", parsedAlm[1])
        assertEquals("Ele estava no princípio com Deus.", parsedAlm[2])

        val bundledJhn1 = ScholarlyBibleVersesData.getVerses("jhn", 1)
        val merged = com.example.data.OpenBibleRepository.mergeChapterSources(
            bookId = "jhn",
            bookName = "João",
            chapter = 1,
            blivreVerses = mapOf(1 to "No princípio era a Palavra...", 2 to "Este estava no princípio com Deus."),
            bltrVerses = mapOf(1 to "No princípio era o Verbo...", 2 to "Este estava no princípio com Deus."),
            almVerses = parsedAlm,
            bundledScholarlyVerses = bundledJhn1
        )
        assertTrue(merged.any { it.verse == 1 && it.originalTextLine.isNotBlank() && it.interlinear.isNotEmpty() })
        assertTrue(merged.any { it.verse == 2 && it.translations.size == 3 })
        assertTrue(merged.any { it.verse == 14 && it.originalTextLine.isNotBlank() })
    }

    @Test
    fun `sqlite bible importer parses MyBible and MySword schemas offline and persists in Room`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cacheDir = context.cacheDir

        // 1. MyBible schema (.SQLite3 with book_number 10/500 and info table)
        val myBibleFile = java.io.File(cacheDir, "test_mybible_ara.sqlite")
        if (myBibleFile.exists()) myBibleFile.delete()
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(myBibleFile, null).use { sqlDb ->
            sqlDb.execSQL("CREATE TABLE info (name TEXT, value TEXT)")
            sqlDb.execSQL("INSERT INTO info VALUES ('description', 'Almeida Revista e Atualizada'), ('abbreviation', 'ARA'), ('language', 'pt')")
            sqlDb.execSQL("CREATE TABLE verses (book_number INT, chapter INT, verse INT, text TEXT)")
            sqlDb.execSQL("INSERT INTO verses VALUES (10, 1, 1, 'No princípio criou Deus os céus e a terra.<S>430</S>')")
            sqlDb.execSQL("INSERT INTO verses VALUES (500, 1, 1, '<n>nota</n>No princípio era o Verbo.')")
        }

        // 2. MySword schema (.bbl.mybible with Bible and Details tables, canonical 1..66)
        val mySwordFile = java.io.File(cacheDir, "test_mysword_nvi.bbl.mybible")
        if (mySwordFile.exists()) mySwordFile.delete()
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(mySwordFile, null).use { sqlDb ->
            sqlDb.execSQL("CREATE TABLE Details (Title TEXT, Abbreviation TEXT, Language TEXT)")
            sqlDb.execSQL("INSERT INTO Details VALUES ('Nova Versão Internacional', 'NVI', 'pt')")
            sqlDb.execSQL("CREATE TABLE Bible (Book INT, Chapter INT, Verse INT, Scripture TEXT)")
            sqlDb.execSQL("INSERT INTO Bible VALUES (60, 1, 3, 'Bendito seja o Deus<RF>nota rodape<Rf> e Pai.')")
        }

        val db = Room.inMemoryDatabaseBuilder(context, LocalStudyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val inspectedAra = com.example.data.SqliteBibleRepository.inspectLocalSqliteFile(
                id = "sqlite_ara",
                file = myBibleFile,
                originalFileName = "ARA.SQLite3"
            ).getOrThrow()
            assertEquals("ARA", inspectedAra.suggestedCode)
            assertEquals("Almeida Revista e Atualizada", inspectedAra.suggestedName)
            assertEquals(2, inspectedAra.versesCount)
            assertEquals(com.example.data.SqliteBibleRepository.SCHEMA_VERSES_MYBIBLE, inspectedAra.schemaType)

            val gen1 = com.example.data.SqliteBibleRepository.readChapterFromFile(
                file = myBibleFile,
                canonicalBookNumber = 1,
                chapter = 1,
                preferredSchemaType = inspectedAra.schemaType
            )
            assertEquals("No princípio criou Deus os céus e a terra.", gen1[1])

            val jhn1 = com.example.data.SqliteBibleRepository.readChapterFromFile(
                file = myBibleFile,
                canonicalBookNumber = 43,
                chapter = 1,
                preferredSchemaType = inspectedAra.schemaType
            )
            assertEquals("No princípio era o Verbo.", jhn1[1])

            val inspectedNvi = com.example.data.SqliteBibleRepository.inspectLocalSqliteFile(
                id = "sqlite_nvi",
                file = mySwordFile,
                originalFileName = "NVI.bbl.mybible"
            ).getOrThrow()
            assertEquals("NVI", inspectedNvi.suggestedCode)
            assertEquals(com.example.data.SqliteBibleRepository.SCHEMA_BIBLE_MYSWORD, inspectedNvi.schemaType)

            val pe1 = com.example.data.SqliteBibleRepository.readChapterFromFile(
                file = mySwordFile,
                canonicalBookNumber = 60,
                chapter = 1,
                preferredSchemaType = inspectedNvi.schemaType
            )
            assertEquals("Bendito seja o Deus e Pai.", pe1[3])

            val repo = UserStudyRepository(db.studyDao())
            val version = com.example.model.ImportedBibleVersion(
                id = inspectedAra.id,
                code = inspectedAra.suggestedCode,
                fullName = inspectedAra.suggestedName,
                languageLabel = inspectedAra.detectedLanguage,
                localFilePath = inspectedAra.localFilePath,
                schemaType = inspectedAra.schemaType,
                versesCount = inspectedAra.versesCount,
                createdAtMillis = 1234L
            )
            assertTrue(repo.saveImportedBible(version).isSuccess)
            val savedList = repo.observeImportedBibles().first()
            assertEquals(1, savedList.size)
            assertEquals("ARA", savedList.first().code)

            assertTrue(repo.deleteImportedBible(version.id).isSuccess)
            assertTrue(repo.observeImportedBibles().first().isEmpty())
        } finally {
            db.close()
            myBibleFile.delete()
            mySwordFile.delete()
        }
    }

    @Test
    fun `stepbible tahot and tagnt parser imports multiple files offline and decodes morphology`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = java.io.File(context.cacheDir, "test_step_interlinear.db")
        if (dbFile.exists()) dbFile.delete()

        val tahotSample = """
            # TAHOT - Translators Amalgamated Hebrew OT - STEPBible.org CC BY 4.0
            # License: Creative Commons Attribution 4.0
            Some introductory documentation line to ignore

            Eng (Heb) Ref & Type${'\t'}Hebrew${'\t'}Transliteration${'\t'}Translation${'\t'}dStrongs${'\t'}Grammar${'\t'}Meaning Variants${'\t'}Spelling Variants${'\t'}Root dStrong+Instance${'\t'}Alternative Strongs+Instance${'\t'}Conjoin word${'\t'}Expanded Strong tags
            Gen.1.1#01=L${'\t'}בְּ/רֵאשִׁ֖ית${'\t'}be./re.Shit${'\t'}in/ beginning${'\t'}H9003/{H7225G}${'\t'}HR/Ncfsa${'\t'}${'\t'}${'\t'}H7225G${'\t'}${'\t'}${'\t'}H9003=ב=in/{H7225G=רֵאשִׁית=: beginning»first:1_beginning}
            Gen.1.1#02=L${'\t'}בָּרָ֣א${'\t'}ba.Ra'${'\t'}he created${'\t'}{H1254A}${'\t'}HVqp3ms${'\t'}${'\t'}${'\t'}H1254A${'\t'}${'\t'}${'\t'}{H1254A=בָּרָא=to create}
            Gen.1.1#03=L${'\t'}אֱלֹהִ֑ים${'\t'}'E.lo.Him${'\t'}God${'\t'}{H0430G}${'\t'}HNcmpa${'\t'}${'\t'}${'\t'}H0430G${'\t'}${'\t'}${'\t'}{H0430G=אֱלֹהִים=God»LORD@Gen.1.1-Heb}
            Gen.1.1#03=Q${'\t'}אֱלֹהִ֑יםQ${'\t'}'E.lo.HimQ${'\t'}GodQ${'\t'}{H0430G}${'\t'}HNcmpa${'\t'}${'\t'}${'\t'}H0430G${'\t'}${'\t'}${'\t'}{H0430G=אֱלֹהִים=God}
        """.trimIndent()

        val tagntSample = """
            # TAGNT - Translators Amalgamated Greek NT
            Word & Type${'\t'}Greek${'\t'}English${'\t'}dStrongs = Grammar${'\t'}Dictionary form = Gloss
            Mat.1.1#01=NKO${'\t'}Βίβλος (Biblos)${'\t'}[The] book${'\t'}G0976=N-NSF${'\t'}βίβλος=book
            Mat.1.1#02=NKO${'\t'}γενέσεως (geneseōs)${'\t'}of [the] genealogy${'\t'}G1078=N-GSF${'\t'}γένεσις=genealogy
            Jhn.1.1#01=NKO${'\t'}Ἐν (En)${'\t'}In${'\t'}G1722=PREP${'\t'}ἐν=in
            Jhn.1.1#02=NKO${'\t'}ἀρχῇ (archē)${'\t'}[the] beginning${'\t'}G0746=N-DSF${'\t'}ἀρχή=beginning
        """.trimIndent()

        val repo = com.example.data.StepInterlinearRepository(context, customDbFile = dbFile)
        try {
            val pack1 = repo.importStream(
                fileName = "1-TAHOT_Gen-Deu.txt",
                inputStream = tahotSample.byteInputStream(Charsets.UTF_8)
            ).getOrThrow()
            val pack2 = repo.importStream(
                fileName = "5-TAGNT_Mat-Jhn.txt",
                inputStream = tagntSample.byteInputStream(Charsets.UTF_8)
            ).getOrThrow()

            assertEquals("TAHOT (Hebraico AT)", pack1.corpusType)
            assertEquals(1, pack1.versesCount)
            assertEquals(3, pack1.tokensCount)

            assertEquals("TAGNT (Grego NT)", pack2.corpusType)
            assertEquals(2, pack2.versesCount)
            assertEquals(4, pack2.tokensCount)

            val gen1 = repo.readChapter("gen", 1)
            val v1 = gen1[1]!!
            assertEquals("בְּרֵאשִׁ֖ית בָּרָ֣א אֱלֹהִ֑ים", v1.first)
            assertEquals(3, v1.second.size)

            val tok1 = v1.second[0]
            assertEquals("בְּרֵאשִׁ֖ית", tok1.originalWord)
            assertEquals("be.re.Shit", tok1.transliteration)
            assertEquals("in beginning", tok1.portugueseGloss)
            assertEquals("H9003/H7225G", tok1.strongId)
            assertEquals("HR/Ncfsa", tok1.morphology)
            assertEquals("רֵאשִׁית", tok1.lemma)

            val tok2 = v1.second[1]
            assertEquals("בָּרָ֣א", tok2.originalWord)
            assertEquals("ba.Ra'", tok2.transliteration)
            assertEquals("he created", tok2.portugueseGloss)
            assertEquals("H1254A", tok2.strongId)
            assertEquals("HVqp3ms", tok2.morphology)

            val tok3 = v1.second[2]
            assertEquals("אֱלֹהִ֑ים", tok3.originalWord)
            assertEquals("H0430G", tok3.strongId)
            assertEquals("HNcmpa", tok3.morphology)

            // Verify Strong catalog normalization for STEPBible dStrongs
            assertEquals("H7225", ScholarlyLibraryData.findStrongEntry(tok1.strongId)?.strongId)
            assertEquals("H1254", ScholarlyLibraryData.findStrongEntry(tok2.strongId)?.strongId)
            assertEquals("H430", ScholarlyLibraryData.findStrongEntry(tok3.strongId)?.strongId)

            // Verify STEPBible Hebrew morphology decoding
            val morph1 = com.example.model.MorphologyDecoder.decode("HR/Ncfsa", com.example.model.OriginalLanguage.HEBREW)
            assertTrue(morph1.any { it.label == "Prefixo" && it.value.contains("Preposição") })
            assertTrue(morph1.any { it.label == "Classe" && it.value == "Substantivo" })
            assertTrue(morph1.any { it.label == "Gênero" && it.value == "Feminino" })

            val morph2 = com.example.model.MorphologyDecoder.decode("HVqp3ms", com.example.model.OriginalLanguage.HEBREW)
            assertTrue(morph2.any { it.label == "Tronco (Binyan)" && it.value.contains("Qal") })
            assertTrue(morph2.any { it.label == "Conjugação" && it.value.contains("Perfeito") })

            // Verify TAGNT chapters
            val mat1 = repo.readChapter("mat", 1)
            assertEquals("Βίβλος γενέσεως", mat1[1]?.first)
            assertEquals("Biblos", mat1[1]?.second?.first()?.transliteration)
            assertEquals("G0976", mat1[1]?.second?.first()?.strongId)
            assertEquals("N-NSF", mat1[1]?.second?.first()?.morphology)

            val jhn1 = repo.readChapter("jhn", 1)
            assertEquals("G1722", jhn1[1]?.second?.first()?.strongId)
            assertEquals("G746", ScholarlyLibraryData.findStrongEntry("G0746")?.strongId)

            val books = repo.observeAvailableBookIds().first()
            assertTrue(books.containsAll(setOf("gen", "mat", "jhn")))
            assertTrue(repo.getAvailableChapters("gen").contains(1))

            repo.deletePack(pack1.id)
            assertTrue(repo.readChapter("gen", 1).isEmpty())
            assertTrue(repo.readChapter("jhn", 1).isNotEmpty())
        } finally {
            repo.close()
            dbFile.delete()
        }
    }

    @Test
    fun `json and plain txt bible importers convert to offline sqlite and serve chapters`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cacheDir = context.cacheDir

        val jsonBooksFile = java.io.File(cacheDir, "acf_sample.json")
        val jsonBooksDest = java.io.File(cacheDir, "acf_sample.sqlite")
        val jsonFlatFile = java.io.File(cacheDir, "nvi_flat.json")
        val jsonFlatDest = java.io.File(cacheDir, "nvi_flat.sqlite")
        val txtFile = java.io.File(cacheDir, "salmos_txt.txt")
        val txtDest = java.io.File(cacheDir, "salmos_txt.sqlite")

        try {
            jsonBooksFile.writeText(
                """
                [
                  {
                    "abbrev": "gn",
                    "name": "Gênesis",
                    "chapters": [
                      [
                        "No princípio criou Deus os céus e a terra.",
                        "E a terra era sem forma e vazia."
                      ]
                    ]
                  },
                  {
                    "abbrev": "jo",
                    "name": "João",
                    "chapters": [
                      [
                        "No princípio era o Verbo."
                      ]
                    ]
                  }
                ]
                """.trimIndent(),
                Charsets.UTF_8
            )

            val pendingJsonBooks = com.example.data.SqliteBibleRepository.inspectOrConvertLocalFile(
                id = "json_books",
                sourceFile = jsonBooksFile,
                destSqliteFile = jsonBooksDest,
                originalFileName = "acf_sample.json"
            ).getOrThrow()
            assertEquals(3, pendingJsonBooks.versesCount)
            val gen1 = com.example.data.SqliteBibleRepository.readChapterFromFile(
                file = jsonBooksDest,
                canonicalBookNumber = 1,
                chapter = 1
            )
            assertEquals("No princípio criou Deus os céus e a terra.", gen1[1])
            val jhn1FromAbbrev = com.example.data.SqliteBibleRepository.readChapterFromFile(
                file = jsonBooksDest,
                canonicalBookNumber = 43,
                chapter = 1
            )
            assertEquals("No princípio era o Verbo.", jhn1FromAbbrev[1])

            jsonFlatFile.writeText(
                """
                {
                  "name": "Nova Versão Internacional Teste",
                  "abbrev": "NVIT",
                  "language": "pt",
                  "verses": [
                    {"book": 43, "chapter": 1, "verse": 1, "text": "No princípio era aquele que é a Palavra."}
                  ]
                }
                """.trimIndent(),
                Charsets.UTF_8
            )
            val pendingFlat = com.example.data.SqliteBibleRepository.inspectOrConvertLocalFile(
                id = "json_flat",
                sourceFile = jsonFlatFile,
                destSqliteFile = jsonFlatDest,
                originalFileName = "nvi_flat.json"
            ).getOrThrow()
            assertEquals("NVIT", pendingFlat.suggestedCode)
            assertEquals(1, pendingFlat.versesCount)
            val flatJhn1 = com.example.data.SqliteBibleRepository.readChapterFromFile(
                file = jsonFlatDest,
                canonicalBookNumber = 43,
                chapter = 1
            )
            assertEquals("No princípio era aquele que é a Palavra.", flatJhn1[1])

            txtFile.writeText(
                """
                # Bíblia em texto puro
                Salmos 23:1 O Senhor é o meu pastor; nada me faltará.
                43${'\t'}3${'\t'}16${'\t'}Porque Deus amou o mundo de tal maneira.
                """.trimIndent(),
                Charsets.UTF_8
            )
            val pendingTxt = com.example.data.SqliteBibleRepository.inspectOrConvertLocalFile(
                id = "txt_sample",
                sourceFile = txtFile,
                destSqliteFile = txtDest,
                originalFileName = "Minha_ARA.txt"
            ).getOrThrow()
            assertEquals(2, pendingTxt.versesCount)
            assertEquals(
                "O Senhor é o meu pastor; nada me faltará.",
                com.example.data.SqliteBibleRepository.readChapterFromFile(txtDest, 19, 23)[1]
            )
            assertEquals(
                "Porque Deus amou o mundo de tal maneira.",
                com.example.data.SqliteBibleRepository.readChapterFromFile(txtDest, 43, 3)[16]
            )
        } finally {
            jsonBooksFile.delete()
            jsonBooksDest.delete()
            jsonFlatFile.delete()
            jsonFlatDest.delete()
            txtFile.delete()
            txtDest.delete()
        }
    }

    @Test
    fun `stepbible json interlinear parser and external file sniffer work accurately`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = java.io.File(context.cacheDir, "test_step_json_interlinear.db")
        if (dbFile.exists()) dbFile.delete()

        val interlinearJson = """
            [
              {
                "ref": "Gen.1.1#01=L",
                "hebrew": "בְּ/רֵאשִׁ֖ית",
                "transliteration": "be./re.Shit",
                "translation": "in/ beginning",
                "dStrongs": "H9003/{H7225G}",
                "grammar": "HR/Ncfsa"
              },
              {
                "book": "Mat",
                "chapter": 1,
                "verse": 1,
                "tokens": [
                  {
                    "greek": "Βίβλος (Biblos)",
                    "translation": "[The] book",
                    "dStrongs": "G0976",
                    "grammar": "N-NSF"
                  }
                ]
              }
            ]
        """.trimIndent()

        val repo = com.example.data.StepInterlinearRepository(context, customDbFile = dbFile)
        try {
            val pack = repo.importStream(
                fileName = "step_sample.json",
                inputStream = interlinearJson.byteInputStream(Charsets.UTF_8)
            ).getOrThrow()
            assertEquals(2, pack.versesCount)
            assertEquals(2, pack.tokensCount)

            val gen1 = repo.readChapter("gen", 1)
            assertEquals("בְּרֵאשִׁ֖ית", gen1[1]?.first)
            assertEquals("H9003/H7225G", gen1[1]?.second?.first()?.strongId)

            val mat1 = repo.readChapter("mat", 1)
            assertEquals("Βίβλος", mat1[1]?.first)
            assertEquals("Biblos", mat1[1]?.second?.first()?.transliteration)

            // Verify sniffer classification
            assertEquals(
                com.example.data.DetectedImportKind.BIBLE_TRANSLATION,
                com.example.data.StepInterlinearParser.classifyFileHead(
                    "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII),
                    "unknown_file"
                )
            )
            assertEquals(
                com.example.data.DetectedImportKind.STEP_INTERLINEAR,
                com.example.data.StepInterlinearParser.classifyFileHead(
                    interlinearJson.toByteArray(Charsets.UTF_8),
                    "shared.json"
                )
            )
            assertEquals(
                com.example.data.DetectedImportKind.BIBLE_TRANSLATION,
                com.example.data.StepInterlinearParser.classifyFileHead(
                    """[{"abbrev":"gn","chapters":[["No princípio..."]]}]""".toByteArray(Charsets.UTF_8),
                    "acf.json"
                )
            )
            assertEquals(
                com.example.data.DetectedImportKind.STEP_INTERLINEAR,
                com.example.data.StepInterlinearParser.classifyFileHead(
                    "Gen.1.1#01=L\tבְּ/רֵאשִׁ֖ית\tbe./re.Shit\tin/ beginning\t{H7225G}\tHR/Ncfsa".toByteArray(Charsets.UTF_8),
                    "tahot.txt"
                )
            )
            assertEquals(
                com.example.data.DetectedImportKind.BIBLE_TRANSLATION,
                com.example.data.StepInterlinearParser.classifyFileHead(
                    "Gênesis 1:1 No princípio criou Deus os céus e a terra.".toByteArray(Charsets.UTF_8),
                    "biblia.txt"
                )
            )
        } finally {
            repo.close()
            dbFile.delete()
        }
    }

    @Test
    fun `instantiate VerboViewModel like MainActivity`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val themePrefs = com.example.data.ThemePreferencesRepository(app.applicationContext)
        val vm = com.example.ui.viewmodel.VerboViewModel(
            userStudyRepository = UserStudyRepository(app.applicationContext),
            theoAiRepository = TheoAiRepository(),
            themePreferencesRepository = themePrefs,
            currentUserId = "local_scholar",
            userDisplayName = "Estudante",
            openBibleRepository = com.example.data.OpenBibleRepository(app.applicationContext),
            sqliteBibleRepository = com.example.data.SqliteBibleRepository(app.applicationContext),
            stepInterlinearRepository = com.example.data.StepInterlinearRepository(app.applicationContext)
        )
        org.junit.Assert.assertNotNull(vm)
    }

    @Test
    fun `launch MainActivity with Robolectric`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        org.junit.Assert.assertNotNull(activity)
    }
}
