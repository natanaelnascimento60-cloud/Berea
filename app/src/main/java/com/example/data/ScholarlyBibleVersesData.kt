package com.example.data

import com.example.model.BibleTranslation
import com.example.model.BibleVerse
import com.example.model.InterlinearToken

data class BundledPassageSummary(
    val bookId: String,
    val bookName: String,
    val shortName: String,
    val chapter: Int,
    val label: String,
    val isOldTestament: Boolean
)

object ScholarlyBibleVersesData {

    fun getVerses(bookId: String, chapter: Int): List<BibleVerse> {
        return versesByKey["$bookId:$chapter"] ?: emptyList()
    }

    fun hasBundledChapter(bookId: String, chapter: Int): Boolean {
        return versesByKey.containsKey("$bookId:$chapter")
    }

    private val bundledBookIds: Set<String> by lazy {
        bundledPassages.mapTo(hashSetOf()) { it.bookId }
    }

    private val allVerses: List<BibleVerse> by lazy {
        versesByKey.values.flatten()
    }

    fun hasBundledBook(bookId: String): Boolean {
        return bundledBookIds.contains(bookId)
    }

    val bundledPassages: List<BundledPassageSummary> by lazy {
        listOf(
            BundledPassageSummary("1pe", "1 Pedro", "1Pe", 1, "1Pe 1", false),
            BundledPassageSummary("jhn", "João", "Jo", 1, "Jo 1", false),
            BundledPassageSummary("jhn", "João", "Jo", 3, "Jo 3", false),
            BundledPassageSummary("gen", "Gênesis", "Gn", 1, "Gn 1", true),
            BundledPassageSummary("exo", "Êxodo", "Êx", 3, "Êx 3", true),
            BundledPassageSummary("psa", "Salmos", "Sl", 1, "Sl 1", true),
            BundledPassageSummary("psa", "Salmos", "Sl", 23, "Sl 23", true),
            BundledPassageSummary("isa", "Isaías", "Is", 53, "Is 53", true),
            BundledPassageSummary("mat", "Mateus", "Mt", 5, "Mt 5", false),
            BundledPassageSummary("rom", "Romanos", "Rm", 8, "Rm 8", false),
            BundledPassageSummary("1co", "1 Coríntios", "1Co", 13, "1Co 13", false),
            BundledPassageSummary("eph", "Efésios", "Ef", 2, "Ef 2", false),
            BundledPassageSummary("php", "Filipenses", "Fp", 2, "Fp 2", false),
            BundledPassageSummary("heb", "Hebreus", "Hb", 1, "Hb 1", false),
            BundledPassageSummary("1jn", "1 João", "1Jo", 1, "1Jo 1", false)
        )
    }

    fun searchVerses(query: String): List<BibleVerse> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        return allVerses.filter { verse ->
            verse.bookName.contains(q, ignoreCase = true) ||
                verse.originalTextLine.contains(q, ignoreCase = true) ||
                verse.translations.values.any { it.contains(q, ignoreCase = true) } ||
                verse.interlinear.any {
                    it.originalWord.contains(q, ignoreCase = true) ||
                        it.transliteration.contains(q, ignoreCase = true) ||
                        it.portugueseGloss.contains(q, ignoreCase = true) ||
                        it.strongId.contains(q, ignoreCase = true)
                }
        }
    }

    private val versesByKey: Map<String, List<BibleVerse>> = mapOf(
        "1pe:1" to listOf(
            BibleVerse(
                bookId = "1pe", bookName = "1 Pedro", chapter = 1, verse = 3,
                originalTextLine = "Εὐλογητὸς ὁ Θεὸς καὶ Πατὴρ τοῦ Κυρίου ἡμῶν Ἰησοῦ Χριστοῦ, ὁ κατὰ τὸ πολὺ αὐτοῦ ἔλεος ἀναγεννήσας ἡμᾶς εἰς ἐλπίδα ζῶσαν δι’ ἀναστάσεως Ἰησοῦ Χριστοῦ ἐκ νεκρῶν,",
                translations = mapOf(
                    BibleTranslation.BLTR to "Bendito seja o Deus e Pai de nosso Senhor Jesus Cristo, que, segundo a sua grande misericórdia, nos regenerou para uma viva esperança, pela ressurreição de Jesus Cristo dentre os mortos,",
                    BibleTranslation.BLIVRE to "Bendito seja o Deus e Pai do nosso Senhor Jesus Cristo, que, segundo a sua grande misericórdia, nos gerou de novo para uma viva esperança, pela ressurreição de Jesus Cristo dentre os mortos,",
                    BibleTranslation.ALM to "Bendito seja o Deus e Pai de nosso Senhor Jesus Cristo, que, segundo a sua grande misericórdia, nos gerou de novo para uma viva esperança, pela ressurreição de Jesus Cristo dentre os mortos,"
                ),
                interlinear = listOf(
                    InterlinearToken("Εὐλογητὸς", "Eulogētos", "Bendito", "G2128", "Adj-NMS", "εὐλογητός"),
                    InterlinearToken("ὁ", "ho", "o", "G3588", "Art-NMS", "ὁ"),
                    InterlinearToken("Θεὸς", "Theos", "Deus", "G2316", "N-NMS", "θεός"),
                    InterlinearToken("ἔλεος", "eleos", "misericórdia", "G1656", "N-ANS", "ἔλεος"),
                    InterlinearToken("ἀναγεννήσας", "anagennēsas", "tendo regenerado", "G313", "V-AAP-NMS", "ἀναγεννάω"),
                    InterlinearToken("ἐλπίδα", "elpida", "esperança", "G1680", "N-AFS", "ἐλπίς"),
                    InterlinearToken("ζῶσαν", "zōsan", "viva", "G2198", "V-PPA-AFS", "ζάω"),
                    InterlinearToken("ἀναστάσεως", "anastaseōs", "ressurreição", "G386", "N-GFS", "ἀνάστασις")
                ),
                commentary = "O particípio aoristo ἀναγεννήσας (anagennēsas) enfatiza uma ação soberana e decisiva de Deus: o crente é gerado de novo 'do alto' não por mérito próprio, mas segundo a grande misericórdia (πολὺ ἔλεος) divina. A ressurreição histórica de Cristo garante que essa esperança é viva (ζῶσαν) e imperecível.",
                textualNote = "NA28: leitura unânime nos principais códices (P72, א, A, B, C) sem variantes substanciais em ἀναγεννήσας.",
                crossReferences = listOf("João 3:3", "Tito 3:5", "Efésios 2:4-6")
            ),
            BibleVerse(
                bookId = "1pe", bookName = "1 Pedro", chapter = 1, verse = 4,
                originalTextLine = "εἰς κληρονομίαν ἄφθαρτον καὶ ἀμίαντον καὶ ἀμάραντον, τετηρημένην ἐν οὐρανοῖς εἰς ὑμᾶς",
                translations = mapOf(
                    BibleTranslation.BLTR to "para uma herança incorruptível, incontaminável e imarcescível, reservada nos céus para vós,",
                    BibleTranslation.BLIVRE to "para uma herança incorruptível, sem mácula, e que não se murcha, guardada nos céus para vós,",
                    BibleTranslation.ALM to "para uma herança incorruptível, incontaminável, e que se não pode murchar, guardada nos céus para vós,"
                ),
                interlinear = listOf(
                    InterlinearToken("κληρονομίαν", "klēronomian", "herança", "G2817", "N-AFS", "κληρονομία"),
                    InterlinearToken("ἄφθαρτον", "aphtharton", "incorruptível", "G862", "Adj-AFS", "ἄφθαρτος"),
                    InterlinearToken("ἀμίαντον", "amianton", "sem mácula", "G283", "Adj-AFS", "ἀμίαντος"),
                    InterlinearToken("ἀμάραντον", "amaranton", "imarcescível", "G263", "Adj-AFS", "ἀμάραντος"),
                    InterlinearToken("τετηρημένην", "tetērēmenēn", "guardada", "G5083", "V-RPP-AFS", "τηρέω")
                ),
                commentary = "Pedro usa três adjetivos gregos com alfa privativo (ἄφθαρτον, ἀμίαντον, ἀμάραντον) para contrastar a herança eterna com as terras e riquezas terrenas sujeitas a corrupção, profanação e decadência. O particípio perfeito τετηρημένην indica custódia contínua nos céus.",
                textualNote = "P72 e B atestam 'εἰς ὑμᾶς' ('para vós'), enquanto alguns manuscritos bizantinos trazem 'εἰς ἡμᾶς'.",
                crossReferences = listOf("Colossenses 1:5", "Mateus 6:20")
            )
        ),
        "jhn:1" to listOf(
            BibleVerse(
                bookId = "jhn", bookName = "João", chapter = 1, verse = 1,
                originalTextLine = "Ἐν ἀρχῇ ἦν ὁ Λόγος, καὶ ὁ Λόγος ἦν πρὸς τὸν Θεόν, καὶ Θεὸς ἦν ὁ Λόγος.",
                translations = mapOf(
                    BibleTranslation.BLTR to "No princípio era o Verbo, e o Verbo estava com Deus, e o Verbo era Deus.",
                    BibleTranslation.BLIVRE to "No princípio era a Palavra, e a Palavra estava com Deus, e a Palavra era Deus.",
                    BibleTranslation.ALM to "No princípio era o Verbo, e o Verbo estava com Deus, e o Verbo era Deus."
                ),
                interlinear = listOf(
                    InterlinearToken("Ἐν", "En", "Em", "G1722", "Prep", "ἐν"),
                    InterlinearToken("ἀρχῇ", "archē", "princípio", "G746", "N-DFS", "ἀρχή"),
                    InterlinearToken("ἦν", "ēn", "era/existia", "G1510", "V-IAI-3S", "εἰμί"),
                    InterlinearToken("ὁ", "ho", "o", "G3588", "Art-NMS", "ὁ"),
                    InterlinearToken("Λόγος", "Logos", "Verbo/Palavra", "G3056", "N-NMS", "λόγος"),
                    InterlinearToken("πρὸς", "pros", "face a face com", "G4314", "Prep", "πρός"),
                    InterlinearToken("τὸν", "ton", "o", "G3588", "Art-AMS", "ὁ"),
                    InterlinearToken("Θεόν", "Theon", "Deus", "G2316", "N-AMS", "θεός")
                ),
                commentary = "O imperfeito ἦν ('existia continuamente') contrasta com ἐγένετο ('veio a existir') no v. 3: o Logos não foi criado, mas preexiste eternamente. Pela Regra de Colwell, Θεὸς (predicativo anártico antes de ἦν) destaca a natureza divina qualitativa do Logos sem confundi-lo com a pessoa do Pai.",
                textualNote = "NA28: texto firme nos papiros P66, P75 e nos códices א e B.",
                crossReferences = listOf("Gênesis 1:1", "Colossenses 1:15-17", "Hebreus 1:2-3")
            ),
            BibleVerse(
                bookId = "jhn", bookName = "João", chapter = 1, verse = 14,
                originalTextLine = "Καὶ ὁ Λόγος σὰρξ ἐγένετο καὶ ἐσκήνωσεν ἐν ἡμῖν, καὶ ἐθεασάμεθα τὴν δόξαν αὐτοῦ, δόξαν ὡς μονογενοῦς παρὰ Πατρός, πλήρης χάριτος καὶ ἀληθείας.",
                translations = mapOf(
                    BibleTranslation.BLTR to "E o Verbo se fez carne e habitou entre nós, e vimos a sua glória, glória como do unigênito do Pai, cheio de graça e de verdade.",
                    BibleTranslation.BLIVRE to "E a Palavra se fez carne, e habitou entre nós, e vimos a sua glória, como a glória do unigênito do Pai, cheio de graça e de verdade.",
                    BibleTranslation.ALM to "E o Verbo se fez carne, e habitou entre nós, e vimos a sua glória, como a glória do unigênito do Pai, cheio de graça e de verdade."
                ),
                interlinear = listOf(
                    InterlinearToken("Λόγος", "Logos", "Verbo", "G3056", "N-NMS", "λόγος"),
                    InterlinearToken("σὰρξ", "sarx", "carne", "G4561", "N-NFS", "σάρξ"),
                    InterlinearToken("ἐγένετο", "egeneto", "tornou-se", "G1096", "V-ADI-3S", "γίνομαι"),
                    InterlinearToken("ἐσκήνωσεν", "eskēnōsen", "armou tenda", "G4637", "V-AAI-3S", "σκηνόω"),
                    InterlinearToken("δόξαν", "doxan", "glória", "G1391", "N-AFS", "δόξα"),
                    InterlinearToken("χάριτος", "charitos", "graça", "G5485", "N-GFS", "χάρις"),
                    InterlinearToken("ἀληθείας", "alētheias", "verdade", "G225", "N-GFS", "ἀλήθεια")
                ),
                commentary = "O verbo ἐσκήνωσεν ('armou tenda' / 'tabernaculou') evoca diretamente a Shekinah do tabernáculo mosaico (Êx 40:34). A dupla 'graça e verdade' (χάρις καὶ ἀλήθεια) traduz a díade pactual hebraica ḥesed we'emet de Êxodo 34:6.",
                textualNote = "πλήρης funciona como nominativo indeclinável ligando-se a ὁ Λόγος em aposição sintática.",
                crossReferences = listOf("Êxodo 34:6", "Êxodo 40:34", "Filipenses 2:6-8")
            )
        ),
        "gen:1" to listOf(
            BibleVerse(
                bookId = "gen", bookName = "Gênesis", chapter = 1, verse = 1,
                originalTextLine = "בְּרֵאשִׁית בָּרָא אֱלֹהִים אֵת הַשָּׁמַיִם וְאֵת הָאָרֶץ",
                translations = mapOf(
                    BibleTranslation.BLTR to "No princípio criou Deus os céus e a terra.",
                    BibleTranslation.BLIVRE to "No princípio Deus criou os céus e a terra.",
                    BibleTranslation.ALM to "No princípio criou Deus os céus e a terra."
                ),
                interlinear = listOf(
                    InterlinearToken("בְּרֵאשִׁית", "bərēʾšîṯ", "No princípio", "H7225", "Prep+N-fs", "רֵאשִׁית"),
                    InterlinearToken("בָּרָא", "bārāʾ", "criou", "H1254", "V-Qal-Perf-3ms", "בָּרָא"),
                    InterlinearToken("אֱלֹהִים", "ʾĕlōhîm", "Deus", "H430", "N-mp", "אֱלֹהִים"),
                    InterlinearToken("אֵת", "ʾēṯ", "[marc. obj.]", "H853", "DirObj", "אֵת"),
                    InterlinearToken("הַשָּׁמַיִם", "haššāmayim", "os céus", "H8064", "Art+N-mp", "שָׁמַיִם"),
                    InterlinearToken("הָאָרֶץ", "hāʾāreṣ", "a terra", "H776", "Art+N-fs", "אֶרֶץ")
                ),
                commentary = "O verbo בָּרָא (bārāʾ) no Antigo Testamento tem exclusivamente Deus como sujeito e indica criação originária sem material preexistente. Embora אֱלֹהִים (Elohim) tenha terminação plural de majestade, concorda com o verbo no singular בָּרָא.",
                textualNote = "TM (BHS) & LXX (Ἐν ἀρχῇ ἐποίησεν ὁ Θεὸς): merismo 'os céus e a terra' expressa a totalidade do cosmos.",
                crossReferences = listOf("João 1:1-3", "Hebreus 11:3", "Salmos 33:6")
            ),
            BibleVerse(
                bookId = "gen", bookName = "Gênesis", chapter = 1, verse = 2,
                originalTextLine = "וְהָאָרֶץ הָיְתָה תֹהוּ וָבֹהוּ וְחֹשֶׁךְ עַל־פְּנֵי תְהוֹם וְרוּחַ אֱלֹהִים מְרַחֶפֶת עַל־פְּנֵי הַמָּיִם",
                translations = mapOf(
                    BibleTranslation.BLTR to "A terra era sem forma e vazia; havia trevas sobre a face do abismo, e o Espírito de Deus pairava sobre as águas.",
                    BibleTranslation.BLIVRE to "E a terra era sem forma e vazia; e havia trevas sobre a face do abismo; e o Espírito de Deus se movia sobre a face das águas.",
                    BibleTranslation.ALM to "E a terra era sem forma e vazia; e havia trevas sobre a face do abismo; e o Espírito de Deus se movia sobre a face das águas."
                ),
                interlinear = listOf(
                    InterlinearToken("תֹהוּ", "ṯōhû", "sem forma", "H8414", "N-ms", "תֹּהוּ"),
                    InterlinearToken("וָבֹהוּ", "wāḇōhû", "e vazia", "H922", "Conj+N-ms", "בֹּהוּ"),
                    InterlinearToken("וְרוּחַ", "wərûaḥ", "e o Espírito", "H7307", "Conj+N-fs", "רוּחַ"),
                    InterlinearToken("אֱלֹהִים", "ʾĕlōhîm", "de Deus", "H430", "N-mp", "אֱלֹהִים"),
                    InterlinearToken("מְרַחֶפֶת", "məraḥep̄eṯ", "pairava/incubava", "H7363", "V-Piel-Part-fs", "רָחַף")
                ),
                commentary = "O particípio Piel מְרַחֶפֶת (meraḥefet) ocorre também em Deuteronômio 32:11 para descrever a águia que paira sobre seus filhotes, revelando cuidado criador e protetor do Espírito (רוּחַ) antes da ordem cósmica.",
                textualNote = "Aliteração hebraica תֹהוּ וָבֹהוּ (tōhû wāḇōhû) indica estado ainda não habitável e não ordenado.",
                crossReferences = listOf("Deuteronômio 32:11", "Salmos 104:30")
            )
        ),
        "psa:23" to listOf(
            BibleVerse(
                bookId = "psa", bookName = "Salmos", chapter = 23, verse = 1,
                originalTextLine = "מִזְמוֹר לְדָוִד יְהוָה רֹעִי לֹא אֶחְסָר",
                translations = mapOf(
                    BibleTranslation.BLTR to "Jeová é o meu pastor; nada me faltará.",
                    BibleTranslation.BLIVRE to "O SENHOR é o meu pastor; nada me faltará.",
                    BibleTranslation.ALM to "O Senhor é o meu pastor, nada me faltará."
                ),
                interlinear = listOf(
                    InterlinearToken("יְהוָה", "YHWH", "O Senhor", "H3068", "N-Proper", "יְהוָה"),
                    InterlinearToken("רֹעִי", "rōʿî", "meu pastor", "H7462", "V-Qal-Part-ms+1cs", "רָעָה"),
                    InterlinearToken("לֹא", "lōʾ", "não", "H3808", "Neg", "לֹא"),
                    InterlinearToken("אֶחְסָר", "ʾeḥsār", "terei falta", "H2637", "V-Qal-Impf-1cs", "חָסֵר")
                ),
                commentary = "רֹעִי (rōʿî) é um particípio ativo Qal com sufixo possessivo: 'Aquele que continuamente me apascenta'. O nome pactual יְהוָה (YHWH) abre o salmo reafirmando fidelidade pessoal.",
                textualNote = "LXX (Sl 22:1): Κύριος ποιμαίνει με καὶ οὐδέν με ὑστερήσει.",
                crossReferences = listOf("João 10:11", "Salmos 80:1", "Ezequiel 34:11")
            ),
            BibleVerse(
                bookId = "psa", bookName = "Salmos", chapter = 23, verse = 6,
                originalTextLine = "אַךְ טוֹב וָחֶסֶד יִרְדְּפוּנִי כָּל־יְמֵי חַיָּי וְשַׁבְתִּי בְּבֵית־יְהוָה לְאֹרֶךְ יָמִים",
                translations = mapOf(
                    BibleTranslation.BLTR to "Certamente que a bondade e a misericórdia me seguirão todos os dias da minha vida, e habitarei na casa de Jeová por longos dias.",
                    BibleTranslation.BLIVRE to "Certamente que a bondade e a misericórdia me seguirão todos os dias da minha vida; e habitarei na casa do SENHOR por longos dias.",
                    BibleTranslation.ALM to "Certamente que a bondade e a misericórdia me seguirão todos os dias da minha vida; e habitarei na casa do Senhor por longos dias."
                ),
                interlinear = listOf(
                    InterlinearToken("טוֹב", "ṭôḇ", "bondade", "H2896", "N-ms", "טוֹב"),
                    InterlinearToken("וָחֶסֶד", "wāḥeseḏ", "e amor leal", "H2617", "Conj+N-ms", "חֶסֶד"),
                    InterlinearToken("יִרְדְּפוּנִי", "yirdəp̄ûnî", "me perseguirão", "H7291", "V-Qal-Impf-3mp+1cs", "רָדַף"),
                    InterlinearToken("וְשַׁבְתִּי", "wəšaḇtî", "e habitarei/retornarei", "H3427", "Conj+V-Qal-Perf-1cs", "יָשַׁב")
                ),
                commentary = "O verbo יִרְדְּפוּנִי (de רָדַף, radaph) geralmente significa 'perseguir' um inimigo; aqui Davi o inverte poeticamente: a bondade (טוֹב) e a misericórdia pactual (חֶסֶד) de Deus vão ao seu encalço todos os dias.",
                textualNote = "וְשַׁבְתִּי (Qal Perf. de שוב com sentido de permanecer/retornar, apoiado por LXX καὶ τὸ κατοικεῖν με).",
                crossReferences = listOf("Salmos 136:1", "Lamentações 3:22-23")
            )
        ),
        "rom:8" to listOf(
            BibleVerse(
                bookId = "rom", bookName = "Romanos", chapter = 8, verse = 1,
                originalTextLine = "Οὐδὲν ἄρα νῦν κατάκριμα τοῖς ἐν Χριστῷ Ἰησοῦ.",
                translations = mapOf(
                    BibleTranslation.BLTR to "Portanto agora nenhuma condenação há para os que estão em Cristo Jesus.",
                    BibleTranslation.BLIVRE to "Portanto, agora nenhuma condenação há para os que estão em Cristo Jesus.",
                    BibleTranslation.ALM to "Portanto, agora nenhuma condenação há para os que estão em Cristo Jesus, que não andam segundo a carne, mas segundo o Espírito."
                ),
                interlinear = listOf(
                    InterlinearToken("Οὐδὲν", "Ouden", "Nenhuma", "G3762", "Adj-NNS", "οὐδείς"),
                    InterlinearToken("ἄρα", "ara", "portanto", "G686", "Conj", "ἄρα"),
                    InterlinearToken("νῦν", "nyn", "agora", "G3568", "Adv", "νῦν"),
                    InterlinearToken("κατάκριμα", "katakrima", "condenação", "G2631", "N-NNS", "κατάκριμα"),
                    InterlinearToken("ἐν", "en", "em", "G1722", "Prep", "ἐν"),
                    InterlinearToken("Χριστῷ", "Christō", "Cristo", "G5547", "N-DMS", "Χριστός"),
                    InterlinearToken("Ἰησοῦ", "Iēsou", "Jesus", "G2424", "N-DMS", "Ἰησοῦς")
                ),
                commentary = "Colocado enfaticamente no início da frase grega, Οὐδὲν ('nenhuma em absoluto') nega qualquer possibilidade de sentença condenatória (κατάκριμα) aos que vivem 'em Cristo Jesus' (ἐν Χριστῷ Ἰησοῦ), expressão paulina central para união salvífica.",
                textualNote = "NA28 omite 'μὴ κατὰ σάρκα περιπατοῦσιν' em א*, B, D*, G (assimilação tardia do v. 4 presente no Texto Recebido/Almeida 1911).",
                crossReferences = listOf("Romanos 5:1", "João 5:24", "Gálatas 3:13")
            )
        ),
        "eph:2" to listOf(
            BibleVerse(
                bookId = "eph", bookName = "Efésios", chapter = 2, verse = 8,
                originalTextLine = "τῇ γὰρ χάριτί ἐστε σεσῳσμένοι διὰ πίστεως· καὶ τοῦτο οὐκ ἐξ ὑμῶν, Θεοῦ τὸ δῶρον·",
                translations = mapOf(
                    BibleTranslation.BLTR to "Pois pela graça sois salvos, por meio da fé; e isso não vem de vós, é dom de Deus;",
                    BibleTranslation.BLIVRE to "Porque pela graça sois salvos, por meio da fé; e isto não vem de vós, é dom de Deus;",
                    BibleTranslation.ALM to "Porque pela graça sois salvos, por meio da fé; e isto não vem de vós, é dom de Deus."
                ),
                interlinear = listOf(
                    InterlinearToken("Τῇ", "Tē", "Pela", "G3588", "Art-DFS", "ὁ"),
                    InterlinearToken("χάριτι", "chariti", "graça", "G5485", "N-DFS", "χάρις"),
                    InterlinearToken("ἐστε", "este", "sois", "G1510", "V-PAI-2P", "εἰμί"),
                    InterlinearToken("σεσῳσμένοι", "sesōsmenoi", "tendo sido salvos", "G4982", "V-RPP-NMP", "σῴζω"),
                    InterlinearToken("διὰ", "dia", "por meio de", "G1223", "Prep", "διά"),
                    InterlinearToken("πίστεως", "pisteōs", "fé", "G4102", "N-GFS", "πίστις"),
                    InterlinearToken("δῶρον", "dōron", "dom/presente", "G1435", "N-NNS", "δῶρον")
                ),
                commentary = "A construção perifrástica ἐστε σεσῳσμένοι (presente de εἰμί + particípio perfeito passivo de σῴζω) significa que a salvação foi consumada pela graça no passado e permanece como estado presente contínuo. O neutro τοῦτο ('isto') refere-se a todo o processo da salvação pela graça mediante a fé.",
                textualNote = "P46, א, B: 'διὰ πίστεως' (sem artigo); D, F, G inserem τῆς antes de πίστεως.",
                crossReferences = listOf("Romanos 3:24", "Tito 3:5", "2 Timóteo 1:9")
            )
        ),
        "1co:13" to listOf(
            BibleVerse(
                bookId = "1co", bookName = "1 Coríntios", chapter = 13, verse = 4,
                originalTextLine = "Ἡ ἀγάπη μακροθυμεῖ, χρηστεύεται ἡ ἀγάπη, οὐ ζηλοῖ, ἡ ἀγάπη οὐ περπερεύεται, οὐ φυσιοῦται,",
                translations = mapOf(
                    BibleTranslation.BLTR to "A caridade é longânima, é benigna; a caridade não é invejosa; a caridade não se jacta, não se ensoberbece,",
                    BibleTranslation.BLIVRE to "O amor é paciente, é benigno; o amor não é invejoso; o amor não trata com leviandade, não se ensoberbece,",
                    BibleTranslation.ALM to "A caridade é sofredora, é benigna; a caridade não é invejosa; a caridade não trata com leviandade, não se ensoberbece,"
                ),
                interlinear = listOf(
                    InterlinearToken("Ἡ", "Hē", "O", "G3588", "Art-NFS", "ὁ"),
                    InterlinearToken("ἀγάπη", "agapē", "amor", "G26", "N-NFS", "ἀγάπη"),
                    InterlinearToken("μακροθυμεῖ", "makrothymei", "é longânimo", "G3114", "V-PAI-3S", "μακροθυμέω"),
                    InterlinearToken("χρηστεύεται", "chrēsteuetai", "age com bondade", "G5541", "V-PMI-3S", "χρηστεύομαι"),
                    InterlinearToken("ζηλοῖ", "zēloi", "tem inveja", "G2206", "V-PAI-3S", "ζηλόω")
                ),
                commentary = "Em todo o capítulo 13, Paulo descreve ἀγάπη (agapē) exclusivamente com verbos no presente indicativo, não como adjetivos abstratos: o amor é definido pelo que ele faz continuamente no corpo de Cristo.",
                textualNote = "χρηστεύεται é hápax legómenon no Novo Testamento, derivado de χρηστός ('benigno, útil').",
                crossReferences = listOf("1 João 4:7-8", "Gálatas 5:22", "Colossenses 3:14")
            )
        ),
        "php:2" to listOf(
            BibleVerse(
                bookId = "php", bookName = "Filipenses", chapter = 2, verse = 6,
                originalTextLine = "ὃς ἐν μορφῇ Θεοῦ ὑπάρχων οὐχ ἁρπαγμὸν ἡγήσατο τὸ εἶναι ἴσα Θεῷ,",
                translations = mapOf(
                    BibleTranslation.BLTR to "o qual, subsistindo em forma de Deus, não julgou que o ser igual a Deus fosse coisa de que não se devesse largar;",
                    BibleTranslation.BLIVRE to "o qual, existindo em forma de Deus, não considerou o ser igual a Deus algo a que se apegar;",
                    BibleTranslation.ALM to "que, sendo em forma de Deus, não teve por usurpação ser igual a Deus,"
                ),
                interlinear = listOf(
                    InterlinearToken("μορφῇ", "morphē", "forma/natureza", "G3444", "N-DFS", "μορφή"),
                    InterlinearToken("Θεοῦ", "Theou", "de Deus", "G2316", "N-GMS", "θεός"),
                    InterlinearToken("ὑπάρχων", "hyparchōn", "subsistindo", "G5225", "V-PPA-NMS", "ὑπάρχω"),
                    InterlinearToken("ἁρπαγμὸν", "harpagmon", "objeto de usurpação", "G725", "N-AMS", "ἁρπαγμός"),
                    InterlinearToken("ἴσα", "isa", "igual", "G2470", "Adj-ANP", "ἴσος")
                ),
                commentary = "μορφή (morphē) denota a essência e atributos internos que caracterizam uma realidade, e não mera aparência externa (σχῆμα). O particípio ὑπάρχων afirma a preexistência divina antes da kénosis (v. 7).",
                textualNote = "Hino pré-paulino (Carmen Christi, vv. 6-11): ἁρπαγμὸν (res rapta vs. res rapienda) indica algo possuído a que Cristo não se apegou egoisticamente.",
                crossReferences = listOf("João 17:5", "2 Coríntios 8:9", "Hebreus 1:3")
            )
        ),
        "jhn:3" to listOf(
            BibleVerse(
                bookId = "jhn", bookName = "João", chapter = 3, verse = 16,
                originalTextLine = "Οὕτως γὰρ ἠγάπησεν ὁ Θεὸς τὸν κόσμον, ὥστε τὸν Υἱὸν τὸν μονογενῆ ἔδωκεν, ἵνα πᾶς ὁ πιστεύων εἰς αὐτὸν μὴ ἀπόληται ἀλλ’ ἔχῃ ζωὴν αἰώνιον.",
                translations = mapOf(
                    BibleTranslation.BLTR to "Porque Deus amou o mundo de tal maneira que deu o seu Filho unigênito, para que todo aquele que nele crê não pereça, mas tenha a vida eterna.",
                    BibleTranslation.BLIVRE to "Porque Deus amou o mundo de tal maneira, que deu o seu Filho unigênito, para que todo aquele que nele crê não pereça, mas tenha a vida eterna.",
                    BibleTranslation.ALM to "Porque Deus amou o mundo de tal maneira que deu o seu Filho unigênito, para que todo aquele que nele crê não pereça, mas tenha a vida eterna."
                ),
                interlinear = listOf(
                    InterlinearToken("ἠγάπησεν", "ēgapēsen", "amou", "G25", "V-AAI-3S", "ἀγαπάω"),
                    InterlinearToken("Θεὸς", "Theos", "Deus", "G2316", "N-NMS", "θεός"),
                    InterlinearToken("κόσμον", "kosmon", "mundo", "G2889", "N-AMS", "κόσμος"),
                    InterlinearToken("Υἱὸν", "Huion", "Filho", "G5207", "N-AMS", "υἱός"),
                    InterlinearToken("μονογενῆ", "monogenē", "Unigênito", "G3439", "Adj-AMS", "μονογενής"),
                    InterlinearToken("ζωὴν", "zōēn", "vida", "G2222", "N-AFS", "ζωή"),
                    InterlinearToken("αἰώνιον", "aiōnion", "eterna", "G166", "Adj-AFS", "αἰώνιος")
                ),
                commentary = "O advérbio οὕτως ('de tal maneira / assim') destaca tanto a intensidade quanto o modo do amor divino (ἠγάπησεν, aoristo constativo). O particípio presente ὁ πιστεύων indica fé contínua e confiante no Filho Unigênito (μονογενῆ).",
                textualNote = "NA28: P66, P75, א e B trazem 'τὸν Υἱὸν τὸν μονογενῆ', enfatizando a singularidade filial pela repetição do artigo.",
                crossReferences = listOf("Romanos 5:8", "1 João 4:9-10", "Gênesis 22:2")
            )
        ),
        "mat:5" to listOf(
            BibleVerse(
                bookId = "mat", bookName = "Mateus", chapter = 5, verse = 3,
                originalTextLine = "Μακάριοι οἱ πτωχοὶ τῷ πνεύματι, ὅτι αὐτῶν ἐστιν ἡ βασιλεία τῶν οὐρανῶν.",
                translations = mapOf(
                    BibleTranslation.BLTR to "Bem-aventurados os pobres de espírito, porque deles é o reino dos céus.",
                    BibleTranslation.BLIVRE to "Bem-aventurados os pobres de espírito, porque deles é o Reino dos céus.",
                    BibleTranslation.ALM to "Bem-aventurados os pobres de espírito, porque deles é o reino dos céus;"
                ),
                interlinear = listOf(
                    InterlinearToken("Μακάριοι", "Makarioi", "Bem-aventurados", "G3107", "Adj-NMP", "μακάριος"),
                    InterlinearToken("πτωχοὶ", "ptōchoi", "os pobres", "G4434", "Adj-NMP", "πτωχός"),
                    InterlinearToken("πνεύματι", "pneumati", "em espírito", "G4151", "N-DNS", "πνεῦμα"),
                    InterlinearToken("βασιλεία", "basileia", "reino", "G932", "N-NFS", "βασιλεία"),
                    InterlinearToken("οὐρανῶν", "ouranōn", "dos céus", "G3772", "N-GMP", "οὐρανός")
                ),
                commentary = "πτωχοὶ (ptōchoi) descreve indigência total — diferente de πένης ('pobre trabalhador'). O dativo de referência τῷ πνεύματι ('quanto ao espírito') aponta para aqueles que reconhecem sua falência espiritual diante de Deus e dependem inteiramente da graça.",
                textualNote = "Mateus emprega 'βασιλεία τῶν οὐρανῶν' (Reino dos céus) em consonância com a reverência judaica ao nome divino; paralelo a Lucas 6:20.",
                crossReferences = listOf("Isaías 61:1", "Isaías 66:2", "Lucas 6:20")
            )
        ),
        "heb:1" to listOf(
            BibleVerse(
                bookId = "heb", bookName = "Hebreus", chapter = 1, verse = 1,
                originalTextLine = "Πολυμερῶς καὶ πολυτρόπως πάλαι ὁ Θεὸς λαλήσας τοῖς πατράσιν ἐν τοῖς προφήταις",
                translations = mapOf(
                    BibleTranslation.BLTR to "Havendo Deus antigamente falado muitas vezes e de muitas maneiras aos pais pelos profetas,",
                    BibleTranslation.BLIVRE to "Havendo Deus antigamente falado muitas vezes, e de muitas maneiras, aos pais, pelos profetas,",
                    BibleTranslation.ALM to "Havendo Deus antigamente falado muitas vezes, e de muitas maneiras, aos pais, pelos profetas, a nós falou-nos nestes últimos dias pelo Filho,"
                ),
                interlinear = listOf(
                    InterlinearToken("Πολυμερῶς", "Polymerōs", "Em muitas porções", "G4181", "Adv", "πολυμερῶς"),
                    InterlinearToken("Θεὸς", "Theos", "Deus", "G2316", "N-NMS", "θεός"),
                    InterlinearToken("λαλήσας", "lalēsas", "tendo falado", "G2980", "V-AAP-NMS", "λαλέω"),
                    InterlinearToken("πατράσιν", "patrasin", "aos pais", "G3962", "N-DMP", "πατήρ"),
                    InterlinearToken("ἐν", "en", "em/por meio de", "G1722", "Prep", "ἐν")
                ),
                commentary = "A aliteração inicial πολυμερῶς καὶ πολυτρόπως abre o período grego mais refinado do Novo Testamento. O particípio aoristo λαλήσας ('tendo falado') liga a revelação fragmentária dos profetas à revelação final e plena no Filho (v. 2).",
                textualNote = "P46, א, A, B trazem 'τοῖς πατράσιν' sem o possessivo 'ἡμῶν' ('nossos').",
                crossReferences = listOf("Números 12:6-8", "João 1:17-18", "2 Pedro 1:21")
            )
        ),
        "1jn:1" to listOf(
            BibleVerse(
                bookId = "1jn", bookName = "1 João", chapter = 1, verse = 1,
                originalTextLine = "Ὃ ἦν ἀπ’ ἀρχῆς, ὃ ἀκηκόαμεν, ὃ ἑωράκαμεν τοῖς ὀφθαλμοῖς ἡμῶν, ὃ ἐθεασάμεθα καὶ αἱ χεῖρες ἡμῶν ἐψηλάφησαν περὶ τοῦ Λόγου τῆς ζωῆς,",
                translations = mapOf(
                    BibleTranslation.BLTR to "O que era desde o princípio, o que ouvimos, o que temos visto com os nossos olhos, o que contemplamos, e as nossas mãos apalparam relativamente ao Verbo da vida,",
                    BibleTranslation.BLIVRE to "O que era desde o princípio, o que ouvimos, o que vimos com os nossos olhos, o que contemplamos, e as nossas mãos apalparam da Palavra da vida,",
                    BibleTranslation.ALM to "O que era desde o princípio, o que ouvimos, o que vimos com os nossos olhos, o que temos contemplado, e as nossas mãos tocaram da Palavra da vida"
                ),
                interlinear = listOf(
                    InterlinearToken("ἦν", "ēn", "era", "G1510", "V-IAI-3S", "εἰμί"),
                    InterlinearToken("ἀρχῆς", "archēs", "princípio", "G746", "N-GFS", "ἀρχή"),
                    InterlinearToken("ἐθεασάμεθα", "etheasametha", "contemplamos", "G2300", "V-ADI-1P", "θεάομαι"),
                    InterlinearToken("Λόγου", "Logou", "Verbo/Palavra", "G3056", "N-GMS", "λόγος"),
                    InterlinearToken("ζωῆς", "zōēs", "da vida", "G2222", "N-GFS", "ζωή")
                ),
                commentary = "João alterna o imperfeito pré-temporal ἦν ('existia desde o princípio') com verbos de testemunho sensorial no perfeito e aoristo (ἀκηκόαμεν, ἑωράκαμεν, ἐθεασάμεθα) para refutar o gnosticismo nascente que negava a encarnação física do Verbo da vida.",
                textualNote = "Eco intencional do prólogo de João 1:1 e 1:14 através de ἀρχῆς, Λόγου e ἐθεασάμεθα.",
                crossReferences = listOf("João 1:1-4", "João 20:27", "Lucas 24:39")
            )
        ),
        "exo:3" to listOf(
            BibleVerse(
                bookId = "exo", bookName = "Êxodo", chapter = 3, verse = 14,
                originalTextLine = "וַיֹּאמֶר אֱלֹהִים אֶל־מֹשֶׁה אֶהְיֶה אֲשֶׁר אֶהְיֶה",
                translations = mapOf(
                    BibleTranslation.BLTR to "Respondeu Deus a Moisés: EU SOU O QUE SOU; e disse: Assim dirás aos filhos de Israel: EU SOU me enviou a vós.",
                    BibleTranslation.BLIVRE to "E disse Deus a Moisés: EU SOU O QUE SOU. Disse mais: Assim dirás aos filhos de Israel: EU SOU me enviou a vós.",
                    BibleTranslation.ALM to "E disse Deus a Moisés: EU SOU O QUE SOU. Disse mais: Assim dirás aos filhos de Israel: EU SOU me enviou a vós."
                ),
                interlinear = listOf(
                    InterlinearToken("אֱלֹהִים", "ʾĕlōhîm", "Deus", "H430", "N-mp", "אֱלֹהִים"),
                    InterlinearToken("אֶהְיֶה", "ʾehyeh", "EU SOU / Serei", "H1961", "V-Qal-Impf-1cs", "הָיָה"),
                    InterlinearToken("אֶהְיֶה", "ʾehyeh", "EU SOU / Serei", "H1961", "V-Qal-Impf-1cs", "הָיָה")
                ),
                commentary = "A fórmula אֶהְיֶה אֲשֶׁר אֶהְיֶה usa o imperfeito Qal de הָיָה (hāyâ), ligando-se diretamente ao Tetragrama יְהוָה (YHWH, v. 15). Expressa tanto a autoexistência eterna quanto a presença fiel e ativa de Deus com o seu povo na história.",
                textualNote = "LXX traduz: 'Ἐγώ εἰμι ὁ ὤν' ('Eu sou Aquele que É'), base para o 'Ἐγώ εἰμι' joanino (Jo 8:58).",
                crossReferences = listOf("João 8:58", "Apocalipse 1:8", "Êxodo 6:2-3")
            )
        ),
        "psa:1" to listOf(
            BibleVerse(
                bookId = "psa", bookName = "Salmos", chapter = 1, verse = 1,
                originalTextLine = "אַשְׁרֵי הָאִישׁ אֲשֶׁר לֹא הָלַךְ בַּעֲצַת רְשָׁעִים",
                translations = mapOf(
                    BibleTranslation.BLTR to "Bem-aventurado o homem que não anda segundo o conselho dos ímpios, nem se detém no caminho dos pecadores, nem se assenta na roda dos escarnecedores;",
                    BibleTranslation.BLIVRE to "Bem-aventurado o homem que não anda segundo o conselho dos perversos, nem se detém no caminho dos pecadores, nem se assenta na roda dos escarnecedores.",
                    BibleTranslation.ALM to "Bem-aventurado o varão que não anda segundo o conselho dos ímpios, nem se detém no caminho dos pecadores, nem se assenta na roda dos escarnecedores."
                ),
                interlinear = listOf(
                    InterlinearToken("אַשְׁרֵי", "ʾašrê", "Bem-aventurado", "H835", "N-mp", "אֶשֶׁר"),
                    InterlinearToken("לֹא", "lōʾ", "não", "H3808", "Neg", "לֹא")
                ),
                commentary = "O Saltério abre com אַשְׁרֵי (ʾašrê), forma plural construta intensiva ('ó múltiplas felicidades do homem!'). Os três verbos seguintes (andar, deter-se, assentar-se) formam uma gradação poética do afastamento progressivo da Torá.",
                textualNote = "Paralelismo sinonímico triplo característico da poesia sapiencial hebraica.",
                crossReferences = listOf("Jeremias 17:7-8", "Mateus 5:3", "Josué 1:8")
            ),
            BibleVerse(
                bookId = "psa", bookName = "Salmos", chapter = 1, verse = 2,
                originalTextLine = "כִּי אִם בְּתוֹרַת יְהוָה חֶפְצוֹ וּבְתוֹרָתוֹ יֶהְגֶּה יוֹמָם וָלָיְלָה",
                translations = mapOf(
                    BibleTranslation.BLTR to "antes tem o seu prazer na lei de Jeová, e na sua lei medita de dia e de noite.",
                    BibleTranslation.BLIVRE to "Antes tem o seu prazer na lei do SENHOR, e na sua lei medita de dia e de noite.",
                    BibleTranslation.ALM to "Antes tem o seu prazer na lei do Senhor, e na sua lei medita de dia e de noite."
                ),
                interlinear = listOf(
                    InterlinearToken("בְּתוֹרַת", "bəṯôraṯ", "na lei de", "H8451", "Prep+N-fs", "תּוֹרָה"),
                    InterlinearToken("יְהוָה", "YHWH", "O Senhor", "H3068", "N-Proper", "יְהוָה")
                ),
                commentary = "תּוֹרָה (tôrâ) significa mais amplamente 'instrução pactual de Deus' do que código jurídico restrito. O verbo יֶהְגֶּה (yehgeh) descreve murmurar em voz baixa a Escritura, ruminação contínua do texto sagrado.",
                textualNote = "Contraste sintático marcado por כִּי אִם ('mas antes / pelo contrário').",
                crossReferences = listOf("Salmos 119:97", "Josué 1:8")
            )
        ),
        "isa:53" to listOf(
            BibleVerse(
                bookId = "isa", bookName = "Isaías", chapter = 53, verse = 5,
                originalTextLine = "וְהוּא מְחֹלָל מִפְּשָׁעֵנוּ מְדֻכָּא מֵעֲוֺנֹתֵינוּ מוּסַר שְׁלוֹמֵנוּ עָלָיו וּבַחֲבֻרָתוֹ נִרְפָּא־לָנוּ",
                translations = mapOf(
                    BibleTranslation.BLTR to "Mas ele foi ferido pelas nossas transgressões e moído pelas nossas iniquidades; o castigo que nos traz a paz estava sobre ele, e pelas suas pisaduras fomos sarados.",
                    BibleTranslation.BLIVRE to "Mas ele foi ferido pelas nossas transgressões, e moído pelas nossas maldades; o castigo que nos traz a paz estava sobre ele, e pelas suas pisaduras fomos sarados.",
                    BibleTranslation.ALM to "Mas ele foi ferido pelas nossas transgressões, e moído pelas nossas iniquidades: o castigo que nos traz a paz estava sobre ele, e pelas suas pisaduras fomos sarados."
                ),
                interlinear = listOf(
                    InterlinearToken("שְׁלוֹמֵנוּ", "šəlômēnû", "nossa paz", "H7965", "N-ms", "שָׁלוֹם"),
                    InterlinearToken("נִרְפָּא", "nirpāʾ", "foi curado / sarado", "H7495", "V-Niphal-Perf-3ms", "רָפָא")
                ),
                commentary = "O quarto Cântico do Servo culmina na substituição vicária: 'o castigo que produz nosso שָׁלוֹם (šālôm, paz e restauração integral) caiu sobre ele'. O perfeito profético נִרְפָּא (Niphal de רָפָא) apresenta a cura espiritual como certa e consumada.",
                textualNote = "1QIsaᵃ (Manuscritos do Mar Morto) confirma a leitura massorética; citado em 1 Pedro 2:24 ('οὗ τῷ μώλωπι ἰάθητε').",
                crossReferences = listOf("1 Pedro 2:24", "Romanos 4:25", "2 Coríntios 5:21")
            )
        )
    )
}
