package com.example.data

import com.example.model.BookOccurrenceStat
import com.example.model.CommentaryEntry
import com.example.model.DictionaryEntry
import com.example.model.InterlinearToken
import com.example.model.LexiconEntry
import com.example.model.MorphologyDecoder
import com.example.model.OriginalLanguage
import com.example.model.WordOccurrenceHit

object ScholarlyLibraryData {

    val lexiconEntries: List<LexiconEntry> = listOf(
        LexiconEntry(
            strongId = "G313",
            language = OriginalLanguage.GREEK,
            originalWord = "ἀναγεννάω",
            transliteration = "anagennaō",
            pronunciation = "a-na-guen-ná-ô",
            shortGloss = "fazer nascer de novo, regenerar",
            etymology = "Composto de ἀνά ('novamente', 'de cima') + γεννάω ('gerar', 'dar à luz').",
            definition = "Gerar novamente para uma nova ordem de vida espiritual; regenerar pela ação criadora de Deus.",
            semanticDomains = listOf("Regeneração Espiritual", "Soteriologia", "Nova Criação"),
            theologicalUsage = "No NT ocorre exclusivamente em 1 Pedro (1:3 e 1:23), destacando que a nova vida deriva da misericórdia de Deus (v. 3) por meio da Palavra viva e permanente (v. 23).",
            keyVerses = listOf("1 Pedro 1:3", "1 Pedro 1:23"),
            occurrences = 2,
            bookDistribution = listOf(
                BookOccurrenceStat("1 Pedro", 2)
            ),
            sampleOccurrences = listOf(
                WordOccurrenceHit(
                    verseRef = "1 Pedro 1:3", bookId = "1pe", chapter = 1, verseNumber = 3,
                    originalExcerpt = "ὁ κατὰ τὸ πολὺ αὐτοῦ ἔλεος ἀναγεννήσας ἡμᾶς",
                    grammarForm = "V-AAP-NMS (Part. Aor. Ativo)",
                    portugueseText = "...segundo a sua grande misericórdia, nos regenerou para uma viva esperança..."
                ),
                WordOccurrenceHit(
                    verseRef = "1 Pedro 1:23", bookId = "1pe", chapter = 1, verseNumber = 23,
                    originalExcerpt = "ἀναγεγεννημένοι οὐκ ἐκ σπορᾶς φθαρτῆς ἀλλὰ ἀφθάρτου",
                    grammarForm = "V-RPP-NMP (Part. Perf. Passivo)",
                    portugueseText = "fostes regenerados não de semente corruptível, mas de incorruptível..."
                )
            )
        ),
        LexiconEntry(
            strongId = "G3056",
            language = OriginalLanguage.GREEK,
            originalWord = "λόγος",
            transliteration = "logos",
            pronunciation = "ló-gos",
            shortGloss = "palavra, Verbo, discurso, razão",
            etymology = "Derivado do verbo λέγω ('reunir', 'dizer', 'expor racionalmente').",
            definition = "Palavra falada, mensagem revelada, declaração autoritativa, relato e, cristologicamente, o Filho eterno enquanto autoexpressão de Deus.",
            semanticDomains = listOf("Comunicação & Revelação", "Cristologia", "Pregação Apostólica"),
            theologicalUsage = "Conecta o conceito veterotestamentário da Palavra criadora (dabar, Sl 33:6) à encarnação de Jesus em João 1:1,14 e Apocalipse 19:13.",
            keyVerses = listOf("João 1:1", "João 1:14", "Hebreus 4:12", "Apocalipse 19:13"),
            occurrences = 330,
            bookDistribution = listOf(
                BookOccurrenceStat("Lucas-Atos", 97),
                BookOccurrenceStat("João & Epístolas", 56),
                BookOccurrenceStat("Paulo", 82),
                BookOccurrenceStat("Sinóticos (Mt/Mc)", 57),
                BookOccurrenceStat("Apocalipse & Gerais", 38)
            ),
            sampleOccurrences = listOf(
                WordOccurrenceHit(
                    verseRef = "João 1:1", bookId = "jhn", chapter = 1, verseNumber = 1,
                    originalExcerpt = "Ἐν ἀρχῇ ἦν ὁ Λόγος, καὶ ὁ Λόγος ἦν πρὸς τὸν Θεόν",
                    grammarForm = "N-NMS (Nom. Masc. Sing.)",
                    portugueseText = "No princípio era o Verbo, e o Verbo estava com Deus, e o Verbo era Deus."
                ),
                WordOccurrenceHit(
                    verseRef = "João 1:14", bookId = "jhn", chapter = 1, verseNumber = 14,
                    originalExcerpt = "Καὶ ὁ Λόγος σὰρξ ἐγένετο καὶ ἐσκήνωσεν ἐν ἡμῖν",
                    grammarForm = "N-NMS (Nom. Masc. Sing.)",
                    portugueseText = "E o Verbo se fez carne e habitou entre nós..."
                ),
                WordOccurrenceHit(
                    verseRef = "Hebreus 4:12", bookId = "heb", chapter = 4, verseNumber = 12,
                    originalExcerpt = "Ζῶν γὰρ ὁ λόγος τοῦ Θεοῦ καὶ ἐνεργής",
                    grammarForm = "N-NMS (Nom. Masc. Sing.)",
                    portugueseText = "Porque a palavra de Deus é viva e eficaz..."
                )
            )
        ),
        LexiconEntry(
            strongId = "G5485",
            language = OriginalLanguage.GREEK,
            originalWord = "χάρις",
            transliteration = "charis",
            pronunciation = "khá-ris",
            shortGloss = "graça, favor imerecido, dádiva",
            etymology = "Relacionado a χαίρω ('alegrar-se'); na LXX traduz frequentemente o hebraico חֵן (ḥēn).",
            definition = "Benevolência gratuita que não depende de mérito do destinatário; poder capacitador concedido por Deus.",
            semanticDomains = listOf("Favor Divino", "Soteriologia", "Dons Ministeriais"),
            theologicalUsage = "Termo central da soteriologia paulina (Rm 3:24; Ef 2:8), opondo a gratuidade da salvação em Cristo às obras da lei.",
            keyVerses = listOf("Efésios 2:8", "Romanos 3:24", "2 Coríntios 12:9", "João 1:14"),
            occurrences = 155,
            bookDistribution = listOf(
                BookOccurrenceStat("Epístolas Paulinas", 100),
                BookOccurrenceStat("Lucas-Atos", 25),
                BookOccurrenceStat("Epístolas Gerais", 24),
                BookOccurrenceStat("João & Apocalipse", 6)
            ),
            sampleOccurrences = listOf(
                WordOccurrenceHit(
                    verseRef = "Efésios 2:8", bookId = "eph", chapter = 2, verseNumber = 8,
                    originalExcerpt = "τῇ γὰρ χάριτί ἐστε σεσῳσμένοι διὰ πίστεως",
                    grammarForm = "N-DFS (Dat. Fem. Sing.)",
                    portugueseText = "Porque pela graça sois salvos, mediante a fé..."
                ),
                WordOccurrenceHit(
                    verseRef = "João 1:14", bookId = "jhn", chapter = 1, verseNumber = 14,
                    originalExcerpt = "πλήρης χάριτος καὶ ἀληθείας",
                    grammarForm = "N-GFS (Gen. Fem. Sing.)",
                    portugueseText = "...cheio de graça e de verdade."
                )
            )
        ),
        LexiconEntry(
            strongId = "G26",
            language = OriginalLanguage.GREEK,
            originalWord = "ἀγάπη",
            transliteration = "agapē",
            pronunciation = "a-gá-pê",
            shortGloss = "amor sacrificial e pactual",
            etymology = "Substantivo derivado de ἀγαπάω; raro no grego clássico, amplamente elevado pela LXX e pelo NT.",
            definition = "Amor deliberado, benevolente e abnegado que busca o bem supremo do outro independentemente de mérito.",
            semanticDomains = listOf("Atributos de Deus", "Ética Cristã", "Comunhão"),
            theologicalUsage = "Define a essência moral de Deus (1Jo 4:8) e a excelência da vida comunitária em 1 Coríntios 13.",
            keyVerses = listOf("1 Coríntios 13:4", "Romanos 5:8", "1 João 4:8"),
            occurrences = 116,
            bookDistribution = listOf(
                BookOccurrenceStat("Paulo", 75),
                BookOccurrenceStat("João & Epístolas", 28),
                BookOccurrenceStat("Gerais & Sinóticos", 13)
            ),
            sampleOccurrences = listOf(
                WordOccurrenceHit(
                    verseRef = "1 Coríntios 13:4", bookId = "1co", chapter = 13, verseNumber = 4,
                    originalExcerpt = "Ἡ ἀγάπη μακροθυμεῖ, χρηστεύεται ἡ ἀγάπη",
                    grammarForm = "N-NFS (Nom. Fem. Sing.)",
                    portugueseText = "O amor é paciente, é benigno..."
                )
            )
        ),
        LexiconEntry(
            strongId = "G4102",
            language = OriginalLanguage.GREEK,
            originalWord = "πίστις",
            transliteration = "pistis",
            pronunciation = "pís-tis",
            shortGloss = "fé, confiança, fidelidade",
            etymology = "Da raiz πείθω ('persuadir', 'confiar').",
            definition = "Convicção firme e confiança pessoal em Cristo para salvação; também fidelidade pactual e corpo doutrinário.",
            semanticDomains = listOf("Fé Salvífica", "Fidelidade", "Doutrina Apostólica"),
            theologicalUsage = "Instrumento receptivo da justificação diante de Deus (Rm 1:17; Ef 2:8; Hb 11:1).",
            keyVerses = listOf("Romanos 1:17", "Efésios 2:8", "Hebreus 11:1"),
            occurrences = 243,
            bookDistribution = listOf(
                BookOccurrenceStat("Paulo", 142),
                BookOccurrenceStat("Hebreus & Gerais", 62),
                BookOccurrenceStat("Sinóticos & Atos", 39)
            ),
            sampleOccurrences = listOf(
                WordOccurrenceHit(
                    verseRef = "Efésios 2:8", bookId = "eph", chapter = 2, verseNumber = 8,
                    originalExcerpt = "σεσῳσμένοι διὰ πίστεως",
                    grammarForm = "N-GFS (Gen. Fem. Sing.)",
                    portugueseText = "...salvos mediante a fé..."
                )
            )
        ),
        LexiconEntry(
            strongId = "G1680",
            language = OriginalLanguage.GREEK,
            originalWord = "ἐλπίς",
            transliteration = "elpis",
            pronunciation = "el-pís",
            shortGloss = "esperança expectante e segura",
            etymology = "Substantivo clássico ligado a ἔλπομαι ('aguardar com expectativa').",
            definition = "Expectativa confiante no cumprimento futuro das promessas divinas; âncora segura da alma.",
            semanticDomains = listOf("Escatologia", "Perseverança", "Virtudes Teologais"),
            theologicalUsage = "Em 1 Pedro 1:3, é qualificada como 'esperança viva' por repousar sobre a ressurreição de Jesus Cristo.",
            keyVerses = listOf("1 Pedro 1:3", "Romanos 15:13", "Hebreus 6:19"),
            occurrences = 53,
            bookDistribution = listOf(
                BookOccurrenceStat("Paulo", 36),
                BookOccurrenceStat("Atos", 8),
                BookOccurrenceStat("Hebreus & 1 Pedro", 8),
                BookOccurrenceStat("1 João", 1)
            ),
            sampleOccurrences = listOf(
                WordOccurrenceHit(
                    verseRef = "1 Pedro 1:3", bookId = "1pe", chapter = 1, verseNumber = 3,
                    originalExcerpt = "ἀναγεννήσας ἡμᾶς εἰς ἐλπίδα ζῶσαν",
                    grammarForm = "N-AFS (Acus. Fem. Sing.)",
                    portugueseText = "...nos regenerou para uma viva esperança..."
                )
            )
        ),
        LexiconEntry(
            strongId = "G2631",
            language = OriginalLanguage.GREEK,
            originalWord = "κατάκριμα",
            transliteration = "katakrima",
            pronunciation = "ka-tá-kri-ma",
            shortGloss = "sentença condenatória",
            etymology = "De κατά ('contra') + κρίμα ('juízo', 'veredicto').",
            definition = "Termo forense que engloba o veredicto judicial de culpa e a penalidade decorrente.",
            semanticDomains = listOf("Tribunal & Juízo", "Justificação"),
            theologicalUsage = "Ocorre exclusivamente em Romanos (5:16, 5:18 e 8:1), contrastando a condenação em Adão com a absolvição em Cristo.",
            keyVerses = listOf("Romanos 8:1", "Romanos 5:16", "Romanos 5:18"),
            occurrences = 3,
            bookDistribution = listOf(BookOccurrenceStat("Romanos", 3)),
            sampleOccurrences = listOf(
                WordOccurrenceHit(
                    verseRef = "Romanos 8:1", bookId = "rom", chapter = 8, verseNumber = 1,
                    originalExcerpt = "Οὐδὲν ἄρα νῦν κατάκριμα τοῖς ἐν Χριστῷ Ἰησοῦ",
                    grammarForm = "N-NNS (Nom. Neutro Sing.)",
                    portugueseText = "Agora, pois, já nenhuma condenação há para os que estão em Cristo Jesus."
                )
            )
        ),
        LexiconEntry(
            strongId = "H2617",
            language = OriginalLanguage.HEBREW,
            originalWord = "חֶסֶד",
            transliteration = "ḥesed",
            pronunciation = "khé-sed",
            shortGloss = "amor leal, misericórdia pactual",
            etymology = "Raiz semítica חסד indicando lealdade firme dentro de um relacionamento pactual.",
            definition = "Bondade amorosa, misericórdia fiel e lealdade inabalável fundamentada em aliança.",
            semanticDomains = listOf("Aliança", "Caráter de YHWH", "Misericórdia"),
            theologicalUsage = "Vocábulo central da auto-revelação de Deus em Êxodo 34:6 e refrão litúrgico do Salmo 136 ('porque o seu ḥesed dura para sempre').",
            keyVerses = listOf("Salmos 23:6", "Salmos 136:1", "Êxodo 34:6"),
            occurrences = 248,
            bookDistribution = listOf(
                BookOccurrenceStat("Salmos", 127),
                BookOccurrenceStat("Históricos", 52),
                BookOccurrenceStat("Profetas", 45),
                BookOccurrenceStat("Pentateuco & Outros", 24)
            ),
            sampleOccurrences = listOf(
                WordOccurrenceHit(
                    verseRef = "Salmos 23:6", bookId = "psa", chapter = 23, verseNumber = 6,
                    originalExcerpt = "אַךְ טוֹב וָחֶסֶד יִרְדְּפוּנִי",
                    grammarForm = "N-ms (Subst. Masc. Sing.)",
                    portugueseText = "Certamente que a bondade e a misericórdia me seguirão..."
                )
            )
        ),
        LexiconEntry(
            strongId = "H7307",
            language = OriginalLanguage.HEBREW,
            originalWord = "רוּחַ",
            transliteration = "rûaḥ",
            pronunciation = "rú-akh",
            shortGloss = "Espírito, vento, sopro vital",
            etymology = "Substantivo primitivo semítico para ar em movimento e respiração.",
            definition = "Vento, alento de vida, disposição espiritual humana e o Espírito criador/profético de Deus.",
            semanticDomains = listOf("Pneumatologia", "Criação", "Profecia"),
            theologicalUsage = "Presente desde Gênesis 1:2 sobre as águas até a promessa escatológica de renovação em Ezequiel 36:26 e Joel 2:28.",
            keyVerses = listOf("Gênesis 1:2", "Salmos 51:11", "Joel 2:28"),
            occurrences = 378,
            bookDistribution = listOf(
                BookOccurrenceStat("Profetas (esp. Ezequiel/Isaías)", 175),
                BookOccurrenceStat("Poéticos", 103),
                BookOccurrenceStat("Históricos & Pentateuco", 100)
            ),
            sampleOccurrences = listOf(
                WordOccurrenceHit(
                    verseRef = "Gênesis 1:2", bookId = "gen", chapter = 1, verseNumber = 2,
                    originalExcerpt = "וְרוּחַ אֱלֹהִים מְרַחֶפֶת עַל־פְּנֵי הַמָּיִם",
                    grammarForm = "N-fs (Subst. Fem. Sing.)",
                    portugueseText = "...e o Espírito de Deus pairava sobre as águas."
                )
            )
        ),
        LexiconEntry(
            strongId = "H1254",
            language = OriginalLanguage.HEBREW,
            originalWord = "בָּרָא",
            transliteration = "bārāʾ",
            pronunciation = "ba-rá",
            shortGloss = "criar (exclusivo de Deus)",
            etymology = "Raiz verbal hebraica empregada exclusivamente para a ação criadora divina.",
            definition = "Trazer à existência algo radicalmente novo; nunca tem sujeito humano no AT.",
            semanticDomains = listOf("Criação Cósmica", "Redenção & Novo Coração"),
            theologicalUsage = "Abre Gênesis 1:1 e ressurge em Salmos 51:10 ('Cria em mim um coração puro') e Isaías 65:17 (novos céus e nova terra).",
            keyVerses = listOf("Gênesis 1:1", "Salmos 51:10", "Isaías 40:28"),
            occurrences = 54,
            bookDistribution = listOf(
                BookOccurrenceStat("Isaías", 21),
                BookOccurrenceStat("Gênesis & Pentateuco", 15),
                BookOccurrenceStat("Salmos & Outros", 18)
            ),
            sampleOccurrences = listOf(
                WordOccurrenceHit(
                    verseRef = "Gênesis 1:1", bookId = "gen", chapter = 1, verseNumber = 1,
                    originalExcerpt = "בְּרֵאשִׁית בָּרָא אֱלֹהִים אֵת הַשָּׁמַיִם",
                    grammarForm = "V-Qal-Perf-3ms",
                    portugueseText = "No princípio criou Deus os céus e a terra."
                )
            )
        ),
        LexiconEntry(
            strongId = "H7965",
            language = OriginalLanguage.HEBREW,
            originalWord = "שָׁלוֹם",
            transliteration = "šālôm",
            pronunciation = "sha-lôm",
            shortGloss = "paz plena, integridade, completude",
            etymology = "Da raiz שלם ('ser inteiro, completo, restaurado').",
            definition = "Bem-estar integral, harmonia pactual com Deus e com a comunidade, prosperidade justa.",
            semanticDomains = listOf("Bênção Sacerdotal", "Reino Messiânico", "Reconciliação"),
            theologicalUsage = "Ápice da bênção arônica (Nm 6:26) e resultado da obra substitutiva do Servo em Isaías 53:5.",
            keyVerses = listOf("Números 6:26", "Isaías 53:5", "Salmos 29:11"),
            occurrences = 237,
            bookDistribution = listOf(
                BookOccurrenceStat("Históricos", 95),
                BookOccurrenceStat("Profetas", 88),
                BookOccurrenceStat("Poéticos & Pentateuco", 54)
            ),
            sampleOccurrences = emptyList()
        )
    )

    val commentaries: List<CommentaryEntry> = listOf(
        CommentaryEntry(
            id = "com_1pe_1",
            bookId = "1pe",
            bookName = "1 Pedro",
            passageRef = "1 Pedro 1:3-5",
            author = "Comentário Exegético Histórico-Gramatical",
            literaryStructure = "Berakah (bênção introdutória) em período único grego (vv. 3-12) estruturada por três orações preposicionais: εἰς ἐλπίδα ζῶσαν (v. 3), εἰς κληρονομίαν (v. 4), εἰς σωτηρίαν (v. 5).",
            historicalContext = "Escrita de Roma ('Babilônia', 5:13) por volta de 62-64 d.C. a cristãos dispersos pela Ásia Menor que enfrentavam ostracismo social e perseguição incipiente.",
            exegesis = "O foco sintático repousa sobre o particípio aoristo ἀναγεννήσας ('tendo-nos regenerado') atribuído ao Pai segundo 'muita misericórdia' (πολὺ ἔλεος). A ressurreição de Cristo constitui o fundamento objetivo da esperança viva (ἐλπίδα ζῶσαν).",
            theologicalApplication = "A esperança cristã não é otimismo subjetivo, mas certeza escatológica garantida no túmulo vazio e guardada pelo poder de Deus."
        ),
        CommentaryEntry(
            id = "com_jhn_1",
            bookId = "jhn",
            bookName = "João",
            passageRef = "João 1:1-14",
            author = "Comentário Exegético do Prólogo Joanino",
            literaryStructure = "Estrutura quiástica (A: Logos com Deus 1:1-2 → B: Criação 1:3-5 → C: Testemunho e Recepção 1:6-13 → A': Encarnação e Glória 1:14-18).",
            historicalContext = "Redigido em Éfeso no fim do século I, dialogando simultaneamente com a teologia sinagogal da Sabedoria/Palavra (dabar) e o pensamento grego.",
            exegesis = "A oposição aspectual entre ἦν (imperfeito durativo de εἰμί — existência pré-temporal) e ἐγένετο (aoristo de γίνομαι — vir a existir) distingue ontologicamente o Verbo eterno de toda criatura. No v. 14, ἐσκήνωσεν ('armou tenda') invoca o tabernáculo do Êxodo.",
            theologicalApplication = "Na encarnação, a glória de Deus torna-se visível e acessível na pessoa de Jesus Cristo, cheio de graça e verdade."
        ),
        CommentaryEntry(
            id = "com_gen_1",
            bookId = "gen",
            bookName = "Gênesis",
            passageRef = "Gênesis 1:1-3",
            author = "Comentário Crítico do Pentateuco",
            literaryStructure = "Prólogo cosmogônico (vv. 1-2) seguido pelo ciclo de seis dias ordenados em dois trípticos paralelos: formação de espaços (dias 1-3) e preenchimento (dias 4-6).",
            historicalContext = "Escrito no contexto da revelação mosaica para refutar cosmogonias politeístas mesopotâmicas e afirmar YHWH como Criador único e soberano.",
            exegesis = "A combinação בָּרָא + אֱלֹהִים (verbo no singular com substantivo plural de majestade) protege o monoteísmo absoluto. O particípio Piel מְרַחֶפֶת ('pairando/incubando') retrata o Espírito (רוּחַ) preparando a matéria informe (תֹהוּ וָבֹהוּ) para a palavra criadora.",
            theologicalApplication = "Toda a criação pertence a Deus e é ordenada por sua Palavra e Espírito."
        ),
        CommentaryEntry(
            id = "com_rom_8",
            bookId = "rom",
            bookName = "Romanos",
            passageRef = "Romanos 8:1-4",
            author = "Comentário Sintático das Epístolas Paulinas",
            literaryStructure = "Conclusão argumentativa (ἄρα νῦν, 'portanto agora') conectada a Romanos 5–7, abrindo o clímax pneumatológico da carta.",
            historicalContext = "Escrita em Corinto (~57 d.C.) à comunidade mista de judeus e gentios em Roma.",
            exegesis = "O adjetivo negativo enfático Οὐδὲν colocado em primeira posição nega cabalmente qualquer κατάκριμα ('sentença condenatória'). A expressão locativa 'ἐν Χριστῷ Ἰησοῦ' define a esfera da união salvífica onde atua a lei do Espírito da vida.",
            theologicalApplication = "A segurança da justificação forense produz liberdade real para andar segundo o Espírito."
        )
    )

    val dictionaryEntries: List<DictionaryEntry> = listOf(
        DictionaryEntry(
            id = "dic_regeneracao",
            term = "Regeneração",
            category = "Soteriologia",
            originalTerm = "παλιγγενεσία / ἀναγεννάω",
            summary = "Ato soberano de Deus pelo qual implanta nova vida espiritual no coração humano.",
            article = "Designa a transformação radical operada pelo Espírito Santo que capacita o pecador a responder com fé ao Evangelho. Aparece ligada ao novo nascimento em João 3:3-8, Tito 3:5 (παλιγγενεσία) e 1 Pedro 1:3,23 (ἀναγεννάω).",
            relatedVerses = listOf("João 3:3-8", "1 Pedro 1:3", "Tito 3:5", "Ezequiel 36:26")
        ),
        DictionaryEntry(
            id = "dic_alianca",
            term = "Aliança (Bərîṯ / Diathēkē)",
            category = "Teologia Bíblica",
            originalTerm = "בְּרִית / διαθήκη",
            summary = "Vínculo pactual solene que estrutura toda a revelação redentora das Escrituras.",
            article = "A aliança bíblica é uma disposição soberana de Deus com promessas, estipulações e sinais visíveis, culminando na Nova Aliança mediada pelo sangue de Cristo (Jr 31:31-34; Lc 22:20; Hb 8:6).",
            relatedVerses = listOf("Gênesis 15:18", "Jeremias 31:31-34", "Hebreus 8:6-13")
        ),
        DictionaryEntry(
            id = "dic_justificacao",
            term = "Justificação",
            category = "Soteriologia",
            originalTerm = "δικαίωσις / δικαιόω",
            summary = "Declaração forense pela qual Deus absolve o crente e imputa a justiça de Cristo.",
            article = "Vocabulário jurídico dos tribunais antigos: justificar significa pronunciar o veredicto de retidão diante da lei divina com base na obra substitutiva de Cristo recebida mediante a fé.",
            relatedVerses = listOf("Romanos 3:24-26", "Romanos 5:1", "Romanos 8:1", "Gálatas 2:16")
        ),
        DictionaryEntry(
            id = "dic_critica_textual",
            term = "Crítica Textual (Aparato)",
            category = "Hermenêutica & Manuscritos",
            originalTerm = "Papiros (𝔓), Unciais (א, B, A), TM, LXX",
            summary = "Disciplina que compara manuscritos antigos para estabelecer o texto original mais fiel.",
            article = "Analisa evidências externas (data, região e qualidade dos manuscritos como 𝔓46, 𝔓66, 𝔓75, Sinaítico א, Vaticano B, Rolo de Isaías 1QIsaᵃ) e critérios internos (lectio difficilior, brevior e estilo do autor).",
            relatedVerses = listOf("Romanos 8:1", "João 1:18", "1 Pedro 1:4")
        ),
        DictionaryEntry(
            id = "dic_septuaginta",
            term = "Septuaginta (LXX)",
            category = "História do Texto",
            originalTerm = "Ἑβδομήκοντα (LXX)",
            summary = "Tradução grega pré-cristã do Antigo Testamento feita em Alexandria (séc. III–II a.C.).",
            article = "Fundamental para a exegese do Novo Testamento, pois fornece o vocabulário grego usado pelos apóstolos (como λόγος, χάρις, ἀγάπη, δόξα, διαθήκη) para expressar conceitos teológicos hebraicos.",
            relatedVerses = listOf("Isaías 7:14", "Mateus 1:23", "Hebreus 10:5")
        )
    )

    private val supplementalStrongEntries: Map<String, LexiconEntry> = listOf(
        LexiconEntry("G2128", OriginalLanguage.GREEK, "εὐλογητός", "eulogētos", "eu-lo-guê-tós", "bendito, digno de louvor", "Composto de εὖ ('bem') + λέγω ('falar').", "Adjetivo verbal aplicado exclusivamente a Deus no Novo Testamento para designar sua perfeição digna de bênção.", listOf("Doxologia", "Atributos de Deus"), "Abre as berakhot apostólicas em 1Pe 1:3, Ef 1:3 e 2Co 1:3.", listOf("1 Pedro 1:3", "Efésios 1:3"), 8),
        LexiconEntry("G2316", OriginalLanguage.GREEK, "θεός", "theos", "the-ós", "Deus, divindade", "Substantivo masculino usado na LXX para traduzir אֱלֹהִים (Elohim) e יְהוָה.", "Designa o único Deus verdadeiro, Criador e Redentor; aplicado ao Pai, ao Filho (Jo 1:1, 20:28) e ao Espírito.", listOf("Teontologia", "Cristologia"), "Termo teológico fundamental com mais de 1.300 ocorrências no NT.", listOf("João 1:1", "1 Pedro 1:3"), 1314),
        LexiconEntry("G1656", OriginalLanguage.GREEK, "ἔλεος", "eleos", "é-le-os", "misericórdia, compaixão ativa", "Na LXX traduz frequentemente o hebraico חֶסֶד (ḥesed).", "Compaixão amorosa que socorre o aflito e remissiva do pecador; fundamento da regeneração em 1Pe 1:3.", listOf("Misericórdia", "Soteriologia"), "Expressa a atitude pactual graciosa de Deus para com a fragilidade humana.", listOf("1 Pedro 1:3", "Efésios 2:4"), 27),
        LexiconEntry("G386", OriginalLanguage.GREEK, "ἀνάστασις", "anastasis", "a-nás-ta-sis", "ressurreição, levantamento", "De ἀνά ('para cima') + ἵστημι ('levantar, pôr de pé').", "Ato de levantar-se dentre os mortos; a ressurreição corporal histórica de Cristo e a ressurreição futura dos crentes.", listOf("Escatologia", "Cristologia"), "Âncora objetiva da esperança cristã em 1Pe 1:3 e 1Co 15.", listOf("1 Pedro 1:3", "1 Coríntios 15:12"), 42),
        LexiconEntry("G2817", OriginalLanguage.GREEK, "κληρονομία", "klēronomia", "klê-ro-no-mí-a", "herança, patrimônio pactual", "De κλῆρος ('sorte/porção') + νέμω ('distribuir').", "Porção herdada por filiação; no NT designa a herança escatológica reservada aos filhos de Deus.", listOf("Filiação", "Escatologia"), "Paralelo à herança da terra prometida no AT, elevada ao plano celestial em 1Pe 1:4.", listOf("1 Pedro 1:4", "Efésios 1:14"), 14),
        LexiconEntry("G5083", OriginalLanguage.GREEK, "τηρέω", "tēreō", "tê-ré-ô", "guardar, preservar, custodiar", "Raiz ligada a τηρός ('vigia, sentinela').", "Manter sob proteção segura e vigilância contínua; preservar intacto para o destinatário.", listOf("Preservação", "Fidelidade"), "Em 1Pe 1:4 o particípio perfeito indica custódia garantida nos céus.", listOf("1 Pedro 1:4", "João 17:11"), 70),
        LexiconEntry("G4637", OriginalLanguage.GREEK, "σκηνόω", "skēnoō", "skê-nó-ô", "armar tenda, habitar", "Derivado de σκηνή ('tenda, tabernáculo'), ecoando o hebraico שָׁכַן (šākan).", "Fixar morada temporária ou tabernacular em meio a um povo.", listOf("Encarnação", "Tabernáculo"), "Em João 1:14 retrata Cristo como o novo santuário da presença divina.", listOf("João 1:14", "Apocalipse 21:3"), 5),
        LexiconEntry("G1391", OriginalLanguage.GREEK, "δόξα", "doxa", "dók-sa", "glória, esplendor, majestade", "Na LXX traduz o hebraico כָּבוֹד (kāḇôḏ, 'peso/glória').", "Manifestação radiante da presença, honra e caráter santo de Deus.", listOf("Atributos de Deus", "Cristologia"), "Em João 1:14 a glória visível no Sinai passa a ser contemplada na humanidade de Jesus.", listOf("João 1:14", "2 Coríntios 4:6"), 166),
        LexiconEntry("G225", OriginalLanguage.GREEK, "ἀλήθεια", "alētheia", "a-lê-thei-a", "verdade, fidelidade, realidade", "De ἀ- (privativo) + λανθάνω ('estar oculto'): aquilo que é desvelado.", "Veracidade absoluta, fidelidade pactual de Deus e revelação genuína em Cristo.", listOf("Revelação", "Epistemologia Bíblica"), "Unida a χάρις em João 1:14 para refletir חֶסֶד וֶאֱמֶת (Êx 34:6).", listOf("João 1:14", "João 14:6"), 109),
        LexiconEntry("G4982", OriginalLanguage.GREEK, "σῴζω", "sōzō", "sô-dzô", "salvar, livrar, restaurar", "Derivado de σῶς ('são, íntegro, preservado').", "Resgatar de perigo ou condenação eterna e restaurar à comunhão plena com Deus.", listOf("Soteriologia", "Redenção"), "Em Efésios 2:8 aparece no particípio perfeito passivo ressaltando salvação já efetivada pela graça.", listOf("Efésios 2:8", "Mateus 1:21"), 106),
        LexiconEntry("G3444", OriginalLanguage.GREEK, "μορφή", "morphē", "mor-fê", "forma essencial, natureza", "Termo clássico para a configuração intrínseca que expressa a essência.", "Forma autêntica correspondente à realidade íntima de algo (diferente de σχῆμα, aparência mutável).", listOf("Cristologia", "Pré-existência"), "Em Filipenses 2:6-7 contrasta a condição divina eterna com a forma de servo assumida.", listOf("Filipenses 2:6", "Filipenses 2:7"), 3),
        LexiconEntry("G5225", OriginalLanguage.GREEK, "ὑπάρχω", "hyparchō", "hy-pár-khô", "subsistir, existir antecedentemente", "De ὑπό ('sob') + ἄρχω ('começar/ser').", "Estar presente desde antes, possuir existência antecedente e contínua.", listOf("Cristologia"), "Em Fp 2:6 o particípio presente indica a divindade contínua antes da encarnação.", listOf("Filipenses 2:6"), 60),
        LexiconEntry("H7225", OriginalLanguage.HEBREW, "רֵאשִׁית", "rēʾšîṯ", "rê-shît", "princípio, primícias, início", "Derivado da raiz רֹאשׁ ('cabeça, topo, início').", "Ponto inicial absoluto de uma série temporal ou a melhor porção consagrada.", listOf("Cosmogonia", "Primícias"), "Em Gênesis 1:1 (בְּרֵאשִׁית) marca o início absoluto do tempo e do universo criado.", listOf("Gênesis 1:1", "Provérbios 1:7"), 51),
        LexiconEntry("H430", OriginalLanguage.HEBREW, "אֱלֹהִים", "ʾĕlōhîm", "e-lo-hîm", "Deus, Todo-Poderoso", "Plural de majestade/intensidade ligado a אֵל / אֱלוֹהַּ.", "Título do Deus criador que expressa plenitude de poder e soberania; rege verbos no singular quando refere-se a YHWH.", listOf("Teontologia", "Criação"), "Ocorre mais de 2.600 vezes no Antigo Testamento.", listOf("Gênesis 1:1", "Salmos 23"), 2602),
        LexiconEntry("H7462", OriginalLanguage.HEBREW, "רָעָה", "rāʿâ", "ra-á", "pastorear, apascentar, cuidar", "Raiz verbal primária para conduzir e nutrir rebanhos.", "Guiar, alimentar e proteger um rebanho; metaforicamente, o cuidado régio e paternal de Deus pelo seu povo.", listOf("Pastoreio", "Providência"), "No Salmo 23:1, רֹעִי (particípio com sufixo) significa 'Aquele que continuamente me pastoreia'.", listOf("Salmos 23:1", "Ezequiel 34:12"), 173),
        LexiconEntry("H3068", OriginalLanguage.HEBREW, "יְהוָה", "YHWH", "Adonai / Javé", "O Senhor (Nome Pactual)", "Associado ao verbo הָיָה ('ser/estar presente', Êx 3:14).", "Tetragrama sagrado do Deus da aliança com Israel, fiel às suas promessas redentoras.", listOf("Aliança", "Teônimo"), "Nome pessoal de Deus revelado a Moisés, com mais de 6.800 ocorrências no AT.", listOf("Êxodo 3:14", "Salmos 23:1"), 6828),
        LexiconEntry("G3588", OriginalLanguage.GREEK, "ὁ, ἡ, τό", "ho, hē, to", "hô / hê / tó", "o, a (artigo definido)", "Artigo definido grego declinado em gênero, número e caso.", "Especifica, individualiza ou substantiva um termo, particípio ou sintagma na oração grega.", listOf("Sintaxe Grega", "Determinação"), "Fundamental na identificação de sujeito vs. predicativo (ex.: Regra de Colwell em Jo 1:1).", listOf("João 1:1", "1 Pedro 1:3"), 19867),
        LexiconEntry("G2532", OriginalLanguage.GREEK, "καί", "kai", "kái", "e, também, até mesmo", "Conjunção coordenativa e adverbial primária do Grego Koiné.", "Liga palavras, orações ou introduz ênfase explicativa ('isto é', 'também').", listOf("Conjunções", "Sintaxe"), "Conjunção mais frequente do Novo Testamento.", listOf("João 1:1", "1 Pedro 1:3"), 9153),
        LexiconEntry("G3962", OriginalLanguage.GREEK, "πατήρ", "patēr", "pa-têr", "Pai, progenitor, patriarca", "Substantivo primário de parentesco.", "Designa o pai terreno, ancestral da aliança (Abraão) ou Deus Pai na relação trinitária e redentora.", listOf("Paternidade Divina", "Trindade"), "Em 1 Pedro 1:3 ('Deus e Pai de nosso Senhor Jesus Cristo') enfatiza a paternidade redentora.", listOf("1 Pedro 1:3", "João 1:14"), 413),
        LexiconEntry("G2962", OriginalLanguage.GREEK, "κύριος", "kyrios", "ký-ri-os", "Senhor, soberano, dono", "De κῦρος ('autoridade suprema'). Na LXX traduz יְהוָה (YHWH).", "Designa autoridade soberana; no NT é título cristológico central proclamando Jesus como Senhor exaltado.", listOf("Cristologia", "Soberania"), "Confissão apostólica primordial em Fp 2:11 e Rm 10:9.", listOf("1 Pedro 1:3", "Filipenses 2:11"), 717),
        LexiconEntry("G2424", OriginalLanguage.GREEK, "Ἰησοῦς", "Iēsous", "I-ê-soûs", "Jesus ('YHWH salva')", "Forma grega do hebraico יֵשׁוּעַ (Yeshua / Yehoshua).", "Nome pessoal do Salvador encarnado, dado pelo anjo em Mt 1:21 porque 'ele salvará o seu povo dos seus pecados'.", listOf("Soteriologia", "Cristologia"), "Nome humano e salvífico do Filho de Deus.", listOf("Mateus 1:21", "1 Pedro 1:3"), 917),
        LexiconEntry("G5547", OriginalLanguage.GREEK, "Χριστός", "Christos", "Khris-tós", "Cristo, Ungido, Messias", "Derivado de χρίω ('ungir'); traduz o hebraico מָשִׁיחַ (Māšîaḥ).", "Título real, sacerdotal e profético de Jesus como o Messias prometido nas Escrituras.", listOf("Cristologia", "Messianismo"), "Unido a Ἰησοῦς torna-se confissão e designação titular do Redentor.", listOf("Romanos 8:1", "1 Pedro 1:3"), 529),
        LexiconEntry("G1473", OriginalLanguage.GREEK, "ἐγώ / ἡμεῖς", "egō / hēmeis", "e-gô / hê-meîs", "eu / nós", "Pronome pessoal de primeira pessoa.", "Indica o falante (singular) ou a comunidade apostólica e dos crentes (plural ἡμῶν, ἡμᾶς).", listOf("Pronomes", "Comunhão"), "No plural inclusivo ('nosso Senhor', 'nos regenerou') une os crentes na graça.", listOf("1 Pedro 1:3", "Efésios 2:8"), 2583),
        LexiconEntry("G846", OriginalLanguage.GREEK, "αὐτός", "autos", "au-tós", "ele, ela, o mesmo, próprio", "Pronome pessoal/intensivo de terceira pessoa.", "Retoma o antecedente ('sua grande misericórdia', 'sua glória') ou enfatiza identidade.", listOf("Pronomes", "Referência"), "Pronome anafórico mais comum no NT grego.", listOf("1 Pedro 1:3", "João 1:14"), 5595),
        LexiconEntry("G4183", OriginalLanguage.GREEK, "πολύς", "polys", "po-lýs", "muito, abundante, numeroso", "Adjetivo de quantidade e intensidade.", "Indica grande medida, abundância ou multidão ('muita/grande misericórdia' em 1Pe 1:3).", listOf("Qualidade", "Abundância"), "Destaca a superabundância da graça e misericórdia de Deus.", listOf("1 Pedro 1:3", "João 3:16"), 416),
        LexiconEntry("G2596", OriginalLanguage.GREEK, "κατά", "kata", "ka-tá", "segundo, conforme, contra", "Preposição com genitivo ('contra/por') ou acusativo ('segundo, conforme').", "Com acusativo indica norma, medida ou padrão ('conforme a sua grande misericórdia').", listOf("Preposições", "Causalidade"), "Estabelece a misericórdia divina como norma e fonte da regeneração.", listOf("1 Pedro 1:3", "Efésios 1:5"), 473),
        LexiconEntry("G1519", OriginalLanguage.GREEK, "εἰς", "eis", "eîs", "para, em direção a, com vistas a", "Preposição com acusativo indicando direção, meta ou propósito.", "Expressa movimento para dentro ou finalidade teológica ('para uma esperança viva').", listOf("Preposições", "Propósito"), "Em 1Pe 1:3-5 estrutura as três metas da regeneração.", listOf("1 Pedro 1:3", "1 Pedro 1:4"), 1767),
        LexiconEntry("G1223", OriginalLanguage.GREEK, "διά", "dia", "di-á", "por meio de, através de, por causa de", "Preposição com genitivo (meio/instrumento) ou acusativo (razão).", "Com genitivo indica o agente ou meio instrumental ('por meio da ressurreição', 'por meio da fé').", listOf("Preposições", "Instrumento"), "Aponta o meio histórico e redentor da salvação.", listOf("1 Pedro 1:3", "Efésios 2:8"), 667),
        LexiconEntry("G1537", OriginalLanguage.GREEK, "ἐκ / ἐξ", "ek / ex", "ek", "de dentro de, dentre, procedente de", "Preposição com genitivo indicando origem ou separação.", "Indica saída de um estado ou grupo ('dentre os mortos').", listOf("Preposições", "Origem"), "Em 1Pe 1:3 e Rm 1:4 marca a vitória real de Cristo sobre a esfera da morte.", listOf("1 Pedro 1:3", "Romanos 8:11"), 914),
        LexiconEntry("G3498", OriginalLanguage.GREEK, "νεκρός", "nekros", "ne-krós", "morto, cadáver", "Ligado a νέκυς ('cadáver').", "Designa morte física ou espiritual; no plural anártico ('dentre mortos') ressalta a ressurreição de Cristo.", listOf("Escatologia", "Ressurreição"), "O contraste entre ἐκ νεκρῶν e ἐλπίδα ζῶσαν ancora a esperança cristã.", listOf("1 Pedro 1:3", "Efésios 2:1"), 128),
        LexiconEntry("G1722", OriginalLanguage.GREEK, "ἐν", "en", "en", "em, dentro de, por meio de", "Preposição com dativo locativo, esférico ou instrumental.", "Indica esfera de existência, união mística ('em Cristo Jesus') ou localização.", listOf("Preposições", "União com Cristo"), "Base da fórmula paulina 'ἐν Χριστῷ' e da abertura joanina 'Ἐν ἀρχῇ'.", listOf("João 1:1", "Romanos 8:1"), 2752),
        LexiconEntry("G746", OriginalLanguage.GREEK, "ἀρχή", "archē", "ar-khê", "princípio, origem, primazia", "De ἄρχω ('começar', 'governar').", "Início absoluto (Jo 1:1; Gn 1:1 LXX), fundamento ou autoridade governante.", listOf("Cosmogonia", "Pré-existência"), "Em João 1:1 remete à eternidade antes da criação.", listOf("João 1:1", "Colossenses 1:18"), 55),
        LexiconEntry("G1510", OriginalLanguage.GREEK, "εἰμί", "eimi", "ei-mí", "ser, existir, estar", "Verbo copulativo e existencial primário.", "No imperfeito ἦν (Jo 1:1) indica existência contínua pré-temporal; no presente ἐστε (Ef 2:8) integra a perífrase do perfeito.", listOf("Verbos", "Existência"), "Fundamental nos pronunciamentos 'Eu Sou' (Ἐγώ εἰμι) de João.", listOf("João 1:1", "Efésios 2:8"), 2462),
        LexiconEntry("G4314", OriginalLanguage.GREEK, "πρός", "pros", "prós", "junto a, face a face com, em direção a", "Preposição indicando proximidade dinâmica e comunhão pessoal.", "Com acusativo em João 1:1 ('ἦν πρὸς τὸν Θεόν') expressa comunhão relacional íntima entre o Verbo e o Pai.", listOf("Preposições", "Trindade"), "Sustenta a distinção pessoal intratrinitária em João 1:1-2.", listOf("João 1:1", "1 João 1:2"), 700),
        LexiconEntry("G4561", OriginalLanguage.GREEK, "σάρξ", "sarx", "sárks", "carne, natureza humana", "Substantivo feminino para o corpo físico e a existência humana.", "Em João 1:14 afirma a encarnação real e plena do Verbo eterno na história humana.", listOf("Encarnação", "Antropologia"), "Refuta o docetismo ao afirmar que o Logos se tornou verdadeiramente carne.", listOf("João 1:14", "Romanos 8:3"), 147),
        LexiconEntry("G1096", OriginalLanguage.GREEK, "γίνομαι", "ginomai", "guí-no-mai", "vir a ser, tornar-se, acontecer", "Verbo dinâmico de transição e ingresso na história.", "Contrasta com ἦν (Jo 1:1): o Verbo eterno 'veio a ser' carne em um ato histórico definido (aoristo ἐγένετο).", listOf("Verbos", "Encarnação"), "Marca a distinção entre ser eterno e tornar-se encarnado.", listOf("João 1:3", "João 1:14"), 669),
        LexiconEntry("G2300", OriginalLanguage.GREEK, "θεάομαι", "theaomai", "the-á-o-mai", "contemplar, observar atentamente", "De θέα ('visão, espetáculo').", "Olhar com admiração e discernimento atento, como testemunha ocular da glória de Cristo.", listOf("Testemunho", "Revelação"), "Em Jo 1:14 ('contemplamos a sua glória') enfatiza testemunho apostólico direto.", listOf("João 1:14", "1 João 1:1"), 22),
        LexiconEntry("G3439", OriginalLanguage.GREEK, "μονογενής", "monogenēs", "mo-no-gue-nês", "único em sua espécie, unigênito", "De μόνος ('único') + γένος ('espécie/linhagem').", "Designa o Filho único, singular e incomparável em sua relação eterna com o Pai.", listOf("Cristologia", "Filiação"), "Central em João 1:14, 1:18 e 3:16.", listOf("João 1:14", "João 3:16"), 9),
        LexiconEntry("G4134", OriginalLanguage.GREEK, "πλήρης", "plērēs", "plê-rês", "cheio, pleno, transbordante", "Relacionado a πίμπλημι ('encher').", "Indica plenitude total de graça e verdade manifestada no Verbo encarnado.", listOf("Plenitude", "Revelação"), "Em Jo 1:14 descreve a riqueza inagotável de Cristo.", listOf("João 1:14", "Atos 6:5"), 16),
        LexiconEntry("G1063", OriginalLanguage.GREEK, "γάρ", "gar", "gár", "pois, porque", "Conjunção explicativa e causal pós-positiva.", "Introduz a razão, explicação ou fundamento teológico da afirmação anterior.", listOf("Conjunções", "Argumentação"), "Essencial para seguir a lógica argumentativa de Paulo e João.", listOf("Efésios 2:8", "João 3:16"), 1041),
        LexiconEntry("G1435", OriginalLanguage.GREEK, "δῶρον", "dōron", "dô-ron", "dom, presente, oferta", "Substantivo neutro ligado a δίδωμι ('dar').", "Dádiva gratuita ou oferta entregue sem obrigação de retribuição.", listOf("Graça", "Soteriologia"), "Em Efésios 2:8 sublinha que a salvação é inteiramente dom de Deus.", listOf("Efésios 2:8", "Mateus 5:23"), 19),
        LexiconEntry("G3762", OriginalLanguage.GREEK, "οὐδείς", "oudeis", "ou-deís", "nenhum, ninguém, nada em absoluto", "De οὐδέ ('nem') + εἷς ('um').", "Negação enfática absoluta ('nenhum sequer').", listOf("Negação", "Justificação"), "Abre Romanos 8:1 proclamando absolvição total em Cristo.", listOf("Romanos 8:1", "João 10:28"), 234),
        LexiconEntry("G25", OriginalLanguage.GREEK, "ἀγαπάω", "agapaō", "a-ga-pá-ô", "amar com amor pactual", "Verbo correspondente ao substantivo ἀγάπη (G26).", "Amar de forma fiel, sacrificial e deliberada.", listOf("Amor Divino", "Ética"), "Usado em João 3:16 para o amor redentor do Pai pelo mundo.", listOf("João 3:16", "Efésios 5:25"), 143),
        LexiconEntry("G2889", OriginalLanguage.GREEK, "κόσμος", "kosmos", "kós-mos", "mundo, ordem criada, humanidade", "Originalmente 'ordem, ornamento'; no NT o sistema mundial ou a humanidade caída.", "Designa o universo ordenado ou a humanidade afastada de Deus, objeto do amor salvífico em Jo 3:16.", listOf("Cosmologia", "Soteriologia"), "Em João 3:16 destaca a amplitude universal da graça.", listOf("João 3:16", "João 1:10"), 186),
        LexiconEntry("G5207", OriginalLanguage.GREEK, "υἱός", "huios", "hy-iós", "filho, herdeiro", "Substantivo masculino indicando filiação, semelhança de natureza e herança.", "Título messiânico e trinitário por excelência do Filho de Deus.", listOf("Cristologia", "Adoção"), "Expressa comunhão plena de natureza e missão com o Pai.", listOf("João 3:16", "Hebreus 1:2"), 377),
        LexiconEntry("G2222", OriginalLanguage.GREEK, "ζωή", "zōē", "dzô-ê", "vida, vida eterna", "Relacionado a ζάω ('viver').", "Vida física e, teologicamente, a vida espiritual e eterna participada em Cristo.", listOf("Vida Eterna", "Soteriologia"), "Em João e Paulo contrasta com a morte espiritual (θάνατος).", listOf("João 3:16", "João 1:4"), 135),
        LexiconEntry("G166", OriginalLanguage.GREEK, "αἰώνιος", "aiōnios", "ai-ô-ni-os", "eterno, sem fim, da era vindoura", "De αἰών ('era, eternidade').", "Qualidade da vida pertencente ao reino eterno de Deus.", listOf("Escatologia", "Vida Eterna"), "Sempre qualifica 'vida eterna' (ζωὴ αἰώνιος) nos Evangelhos.", listOf("João 3:16", "Romanos 6:23"), 71),
        LexiconEntry("H8064", OriginalLanguage.HEBREW, "שָׁמַיִם", "šāmayim", "sha-má-yim", "céus, firmamento, morada divina", "Substantivo dual/plural sempre usado no plural.", "Designa a atmosfera, o cosmos celeste e a habitação transcendente de Deus.", listOf("Cosmogonia", "Criação"), "Com אֶרֶץ forma o merismo 'céus e terra' (todo o universo) em Gn 1:1.", listOf("Gênesis 1:1", "Salmos 19:1"), 420),
        LexiconEntry("H776", OriginalLanguage.HEBREW, "אֶרֶץ", "ʾereṣ", "é-rets", "terra, solo, país, mundo", "Substantivo feminino primitivo.", "Designa o planeta criado em Gn 1:1, a terra seca ou a terra da promessa.", listOf("Criação", "Aliança"), "Em Gn 1:2 contrasta o estado informe inicial com a ordem da Palavra.", listOf("Gênesis 1:1", "Gênesis 1:2"), 2504),
        LexiconEntry("H853", OriginalLanguage.HEBREW, "אֵת", "ʾēṯ", "êt", "[marcador de objeto direto]", "Partícula sintática hebraica não traduzida diretamente.", "Sinaliza o objeto direto definido de um verbo transitivo (ex.: 'os céus e a terra' em Gn 1:1).", listOf("Sintaxe Hebraica"), "Essencial na análise gramatical do Texto Massorético.", listOf("Gênesis 1:1"), 7000),
        LexiconEntry("H8414", OriginalLanguage.HEBREW, "תֹּהוּ", "tōhû", "tô-hû", "sem forma, vazio, desolação", "Substantivo masculino poético/cosmogônico.", "Descreve estado árido, deserto ou ainda não estruturado para a vida humana.", listOf("Cosmogonia"), "Forma aliteração com בֹּהוּ em Gn 1:2 ('sem forma e vazia').", listOf("Gênesis 1:2", "Isaías 45:18"), 20),
        LexiconEntry("H922", OriginalLanguage.HEBREW, "בֹּהוּ", "bōhû", "bô-hû", "vazio, desabitado", "Usado sempre em par aliterativo com תֹּהוּ.", "Indica ausência de conteúdo ou habitantes antes da obra ordenadora de Deus.", listOf("Cosmogonia"), "Ocorre apenas em Gn 1:2, Is 34:11 e Jr 4:23.", listOf("Gênesis 1:2"), 3),
        LexiconEntry("H2822", OriginalLanguage.HEBREW, "חֹשֶׁךְ", "ḥōšeḵ", "khô-shekh", "trevas, escuridão", "Da raiz חָשַׁךְ ('escurecer').", "Ausência física de luz ou símbolo de juízo e mistério antes da criação da luz.", listOf("Criação"), "Em Gn 1:2 precede o primeiro fiat criador: 'Haja luz'.", listOf("Gênesis 1:2", "Isaías 45:7"), 80),
        LexiconEntry("H8415", OriginalLanguage.HEBREW, "תְּהוֹם", "təhôm", "te-hôm", "abismo, oceano primordial", "Substantivo semítico para águas profundas.", "Designa a massa aquosa profunda coberta pelas trevas em Gn 1:2.", listOf("Cosmogonia"), "Totalmente subordinado à soberania criadora de Elohim.", listOf("Gênesis 1:2", "Salmos 104:6"), 36),
        LexiconEntry("H4325", OriginalLanguage.HEBREW, "מַיִם", "mayim", "má-yim", "águas", "Substantivo dual/plural masculino.", "Águas primordiais, fontes, mares ou símbolo bíblico de vida e purificação.", listOf("Criação", "Simbolismo"), "Sobre a face das águas paira o Espírito de Deus em Gn 1:2.", listOf("Gênesis 1:2", "Salmos 23:2"), 580),
        LexiconEntry("H2896", OriginalLanguage.HEBREW, "טוֹב", "ṭôḇ", "tôv", "bom, excelente, agradável, benéfico", "Adjetivo e substantivo ligado à perfeição funcional e moral.", "Em Gn 1 avalia a criação como bela e adequada; no Sl 23:6 acompanha חֶסֶד como bênção fiel.", listOf("Criação", "Bondade Divina"), "Expressa harmonia plena com o propósito de Deus.", listOf("Salmos 23:6", "Gênesis 1:4"), 559),
        LexiconEntry("H7291", OriginalLanguage.HEBREW, "רָדַף", "rāḏap̄", "ra-daf", "perseguir, ir ao encalço, seguir de perto", "Raiz verbal para perseguição intensa.", "No Sl 23:6 revela poeticamente a bondade e misericórdia de Deus seguindo ativamente o crente.", listOf("Providência"), "Inversão poética de perseguição hostil em cuidado gracioso.", listOf("Salmos 23:6"), 143),
        LexiconEntry("H3427", OriginalLanguage.HEBREW, "יָשַׁב", "yāšaḇ", "ya-shav", "habitar, assentar-se, permanecer", "Raiz verbal fundamental de moradia e entronização.", "Indica habitação estável e comunhão duradoura na casa de YHWH.", listOf("Adoração", "Presença"), "No Sl 23:6 expressa permanência eterna junto ao Senhor.", listOf("Salmos 23:6", "Salmos 27:4"), 1088),
        LexiconEntry("G3107", OriginalLanguage.GREEK, "μακάριος", "makarios", "ma-ká-ri-os", "bem-aventurado, abençoado por Deus", "Adjetivo de plenitude espiritual e favor divino.", "Não descreve mera alegria circunstancial, mas o estado objetivo de quem participa do Reino de Deus.", listOf("Bem-aventuranças", "Reino"), "Abre o Sermão do Monte em Mateus 5:3-11.", listOf("Mateus 5:3", "Romanos 4:7"), 50),
        LexiconEntry("G4434", OriginalLanguage.GREEK, "πτωχός", "ptōchos", "ptô-khós", "pobre, indigente, dependente", "De πτώσσω ('encolher-se, depender totalmente').", "Indica pobreza extrema que nada possui de si mesma; com 'em espírito' expressa humildade diante de Deus.", listOf("Humildade", "Sermão do Monte"), "Os 'pobres de espírito' reconhecem total dependência da graça.", listOf("Mateus 5:3", "Lucas 4:18"), 34),
        LexiconEntry("G4151", OriginalLanguage.GREEK, "πνεῦμα", "pneuma", "pneû-ma", "espírito, sopro, vento, Espírito Santo", "De πνέω ('soprar, respirar').", "Designa o Espírito de Deus, o espírito humano ou disposição interior.", listOf("Pneumatologia", "Antropologia"), "Paralelo grego do hebraico רוּחַ (H7307).", listOf("Mateus 5:3", "Romanos 8:9"), 379),
        LexiconEntry("G932", OriginalLanguage.GREEK, "βασιλεία", "basileia", "ba-si-leí-a", "reino, reinado, soberania real", "De βασιλεύς ('rei').", "Enfatiza o governo dinâmico e soberano de Deus sobre seu povo e a criação.", listOf("Escatologia", "Evangelhos"), "Tema central da pregação de Jesus em Mateus ('Reino dos céus').", listOf("Mateus 5:3", "Mateus 6:33"), 162),
        LexiconEntry("G3772", OriginalLanguage.GREEK, "οὐρανός", "ouranos", "ou-ra-nós", "céu, céus, esfera celestial", "Substantivo masculino usado no singular ou plural.", "Designa o firmamento físico ou a habitação transcendente de Deus.", listOf("Cosmologia", "Reino"), "Em Mateus ('βασιλεία τῶν οὐρανῶν') funciona como circunlóquio reverente para Deus.", listOf("Mateus 5:3", "1 Pedro 1:4"), 273),
        LexiconEntry("G4181", OriginalLanguage.GREEK, "πολυμερῶς", "polymerōs", "po-ly-me-rôs", "de muitas maneiras / em muitas porções", "Advérbio composto de πολύς + μέρος ('parte').", "Descreve a revelação progressiva de Deus no Antigo Testamento em múltiplas porções.", listOf("Revelação", "Hermenêutica"), "Abre a Epístola aos Hebreus destacando a progressão histórico-redentora.", listOf("Hebreus 1:1"), 1),
        LexiconEntry("G2980", OriginalLanguage.GREEK, "λαλέω", "laleō", "la-lé-ô", "falar, proclamar, revelar", "Verbo de comunicação verbal clara.", "Em Hebreus 1:1-2 une a fala profética antiga à revelação definitiva no Filho.", listOf("Revelação", "Profecia"), "Sublinha que Deus é um Deus que fala e se dá a conhecer.", listOf("Hebreus 1:1", "João 12:49"), 296),
        LexiconEntry("H835", OriginalLanguage.HEBREW, "אֶשֶׁר / אַשְׁרֵי", "ʾašrê", "ash-rê", "bem-aventurado, feliz é", "Construto plural intensivo de אֶשֶׁר ('felicidade, retidão').", "Exclamação sapiencial que declara o caminho abençoado do justo que medita na Torá.", listOf("Sabedoria", "Salmos"), "Abre o Saltério em Salmos 1:1.", listOf("Salmos 1:1", "Salmos 32:1"), 45),
        LexiconEntry("H8451", OriginalLanguage.HEBREW, "תּוֹרָה", "tôrâ", "tô-rá", "lei, instrução, ensinamento divino", "De יָרָה ('apontar o caminho, instruir').", "Designa a instrução pactual revelada por Deus para guiar a vida de Israel.", listOf("Aliança", "Sabedoria"), "No Salmo 1:2 é objeto de deleite e meditação contínua.", listOf("Salmos 1:2", "Salmos 119:1"), 220),
        LexiconEntry("H1961", OriginalLanguage.HEBREW, "הָיָה", "hāyâ", "ha-yá", "ser, tornar-se, acontecer, existir", "Raiz verbal ligada ao Tetragrama יְהוָה (YHWH).", "Na fórmula אֶהְיֶה אֲשֶׁר אֶהְיֶה (Êx 3:14) declara a presença fiel, ativa e autoexistente de Deus.", listOf("Teologia Própria", "Aliança"), "Base etimológica da revelação do nome divino a Moisés.", listOf("Êxodo 3:14", "Gênesis 1:2"), 3561),
        LexiconEntry("H7965", OriginalLanguage.HEBREW, "שָׁלוֹם", "šālôm", "sha-lôm", "paz, integridade, plenitude, restauração", "De שָׁלֵם ('ser completo, íntegro').", "Vai além da ausência de guerra: significa bem-estar total, reconciliação e saúde espiritual.", listOf("Redenção", "Paz"), "Em Isaías 53:5 o castigo sobre o Servo traz nosso shalom.", listOf("Isaías 53:5", "Números 6:26"), 237),
        LexiconEntry("H7495", OriginalLanguage.HEBREW, "רָפָא", "rāp̄āʾ", "ra-fá", "curar, restaurar, sarar", "Raiz verbal de cura física e espiritual.", "Em Isaías 53:5 ('pelas suas pisaduras fomos sarados') aponta para a restauração redentora do pecado.", listOf("Redenção", "Cura"), "Citado em 1 Pedro 2:24 aplicado à obra expiatória de Cristo.", listOf("Isaías 53:5", "Êxodo 15:26"), 67),
        LexiconEntry("G2198", OriginalLanguage.GREEK, "ζάω", "zaō", "dzá-ô", "viver, estar vivo, ter vida plena", "Verbo primário correspondente a ζωή (vida).", "No particípio ζῶσαν em 1Pe 1:3 qualifica a esperança cristã como viva, dinâmica e operante.", listOf("Esperança", "Ressurreição"), "Contrasta a esperança viva em Cristo com ilusões perecíveis.", listOf("1 Pedro 1:3", "Romanos 6:11"), 140),
        LexiconEntry("G862", OriginalLanguage.GREEK, "ἄφθαρτος", "aphthartos", "áf-thar-tos", "incorruptível, imperecível, imortal", "De α- privativo + φθείρω ('corromper, destruir').", "Qualifica a herança celestial em 1Pe 1:4 como imune à decadência ou destruição.", listOf("Herança", "Escatologia"), "Primeiro dos três adjetivos com alfa privativo em 1Pe 1:4.", listOf("1 Pedro 1:4", "1 Coríntios 15:52"), 7),
        LexiconEntry("G283", OriginalLanguage.GREEK, "ἀμίαντος", "amiantos", "a-mí-an-tos", "incontaminável, puro, sem mácula", "De α- privativo + μιαίνω ('manchar, contaminar').", "Indica pureza ritual e moral absoluta; nada pode macular a herança reservada ao crente.", listOf("Herança", "Santidade"), "Segundo adjetivo com alfa privativo em 1Pe 1:4.", listOf("1 Pedro 1:4", "Hebreus 7:26"), 4),
        LexiconEntry("G263", OriginalLanguage.GREEK, "ἀμάραντος", "amarantos", "a-má-ran-tos", "imarcescível, que não murcha", "De α- privativo + μαραίνω ('murchar, fenecer').", "Hápax legómenon no NT: descreve beleza e vigor eternos que jamais perdem o viço.", listOf("Herança", "Eternidade"), "Terceiro adjetivo com alfa privativo em 1Pe 1:4.", listOf("1 Pedro 1:4"), 1),
        LexiconEntry("G686", OriginalLanguage.GREEK, "ἄρα", "ara", "á-ra", "portanto, logo, por conseguinte", "Partícula inferencial pós-positiva.", "Introduz conclusão lógica direta dos argumentos precedentes, como a absolvição em Romanos 8:1.", listOf("Sintaxe", "Argumentação"), "Liga Romanos 8:1 à obra libertadora de Cristo nos capítulos 5–7.", listOf("Romanos 8:1", "Gálatas 3:7"), 49),
        LexiconEntry("G3568", OriginalLanguage.GREEK, "νῦν", "nyn", "nýn", "agora, neste momento escatológico", "Advérbio de tempo presente.", "Em Romanos 8:1 marca o 'agora' da nova era inaugurada pela morte e ressurreição de Cristo.", listOf("Escatologia", "Justificação"), "Contrasta o regime antigo da lei com a liberdade atual no Espírito.", listOf("Romanos 8:1", "2 Coríntios 6:2"), 147),
        LexiconEntry("G3114", OriginalLanguage.GREEK, "μακροθυμέω", "makrothymeō", "ma-kro-thy-mé-ô", "ser longânimo, paciente, tardio em irar-se", "De μακρός ('longo') + θυμός ('ânimo/ira').", "Paciência perseverante diante de ofensas e provações sem revidar.", listOf("Amor", "Fruto do Espírito"), "Primeiro verbo que define a ação do amor (ἀγάπη) em 1Co 13:4.", listOf("1 Coríntios 13:4", "Tiago 5:7"), 10),
        LexiconEntry("G5541", OriginalLanguage.GREEK, "χρηστεύομαι", "chrēsteuomai", "khrês-teú-o-mai", "ser benigno, agir com bondade ativa", "De χρηστός ('bom, útil, gentil').", "Hápax no NT: descreve bondade prática, generosa e acolhedora em ação.", listOf("Amor", "Ética Cristã"), "Par positivo de μακροθυμεῖ em 1 Coríntios 13:4.", listOf("1 Coríntios 13:4"), 1),
        LexiconEntry("G2206", OriginalLanguage.GREEK, "ζηλόω", "zēloō", "dzê-ló-ô", "ter ciúmes, invejar ou zelar ardentemente", "De ζῆλος ('fervor, zelo, ciúme').", "Em sentido negativo (1Co 13:4) significa arder em inveja ou rivalidade.", listOf("Ética Cristã"), "Em 1Co 13:4 inicia a série de oito negações do que o amor não faz.", listOf("1 Coríntios 13:4", "1 Coríntios 12:31"), 11),
        LexiconEntry("G725", OriginalLanguage.GREEK, "ἁρπαγμός", "harpagmos", "har-pag-mós", "usurpação, presa a reter egoisticamente", "De ἁρπάζω ('arrebatar, tomar à força').", "Termo-chave do hino cristológico de Fp 2:6: Cristo não explorou sua igualdade divina para proveito próprio.", listOf("Cristologia", "Kénosis"), "Hápax legómenon no NT em Filipenses 2:6.", listOf("Filipenses 2:6"), 1),
        LexiconEntry("G2470", OriginalLanguage.GREEK, "ἴσος", "isos", "í-sos", "igual, equivalente em natureza/condição", "Adjetivo de igualdade plena.", "Em Fp 2:6 ('τὸ εἶναι ἴσα Θεῷ') expressa a igualdade ontológica do Filho com Deus.", listOf("Cristologia", "Trindade"), "Sustenta a divindade plena de Cristo antes da encarnação.", listOf("Filipenses 2:6", "João 5:18"), 8),
        LexiconEntry("H7363", OriginalLanguage.HEBREW, "רָחַף", "rāḥap̄", "ra-kháf", "pairar, incubar, mover-se suavemente", "Raiz verbal rara usada no tronco Piel em Gn 1:2.", "Descreve o cuidado protetor e vivificador do Espírito de Deus sobre as águas primordiais.", listOf("Criação", "Pneumatologia"), "Mesma raiz usada para a águia que paira sobre o ninho em Dt 32:11.", listOf("Gênesis 1:2", "Deuteronômio 32:11"), 3),
        LexiconEntry("H3808", OriginalLanguage.HEBREW, "לֹא", "lōʾ", "lô", "não (negação objetiva/absoluta)", "Partícula negativa padrão do hebraico bíblico.", "Nega fatos verbais ou expressa proibições categóricas ('nada me faltará' em Sl 23:1).", listOf("Sintaxe Hebraica"), "Em Sl 23:1 ('לֹא אֶחְסָר') expressa suficiência absoluta no Senhor.", listOf("Salmos 23:1", "Salmos 1:1"), 5185),
        LexiconEntry("H2637", OriginalLanguage.HEBREW, "חָסֵר", "ḥāsēr", "kha-sêr", "ter falta, carecer, diminuir", "Raiz verbal de carência ou privação.", "No Sl 23:1 com negação לֹא declara que o rebanho do Senhor não sofre privação essencial.", listOf("Providência", "Salmos"), "Contrasta com a provisão fiel do Pastor divino.", listOf("Salmos 23:1", "Deuteronômio 2:7"), 23)
    ).associateBy { it.strongId.uppercase() }

    private val commonWordTokens: List<InterlinearToken> = listOf(
        // 1 Pedro 1:3-4 & common Greek words
        InterlinearToken("Εὐλογητὸς", "Eulogētos", "Bendito", "G2128", "Adj-NMS", "εὐλογητός"),
        InterlinearToken("ὁ", "ho", "o", "G3588", "Art-NMS", "ὁ"),
        InterlinearToken("ἡ", "hē", "a", "G3588", "Art-NFS", "ὁ"),
        InterlinearToken("τὸ", "to", "o/a", "G3588", "Art-ANS", "ὁ"),
        InterlinearToken("τοῦ", "tou", "do", "G3588", "Art-GMS", "ὁ"),
        InterlinearToken("τὸν", "ton", "o", "G3588", "Art-AMS", "ὁ"),
        InterlinearToken("τὴν", "tēn", "a", "G3588", "Art-AFS", "ὁ"),
        InterlinearToken("τῇ", "tē", "pela/à", "G3588", "Art-DFS", "ὁ"),
        InterlinearToken("τοῖς", "tois", "aos/para os", "G3588", "Art-DMP", "ὁ"),
        InterlinearToken("τῶν", "tōn", "dos", "G3588", "Art-GMP", "ὁ"),
        InterlinearToken("Θεὸς", "Theos", "Deus", "G2316", "N-NMS", "θεός"),
        InterlinearToken("Θεοῦ", "Theou", "de Deus", "G2316", "N-GMS", "θεός"),
        InterlinearToken("Θεόν", "Theon", "Deus", "G2316", "N-AMS", "θεός"),
        InterlinearToken("Θεῷ", "Theō", "a Deus", "G2316", "N-DMS", "θεός"),
        InterlinearToken("καὶ", "kai", "e", "G2532", "Conj", "καί"),
        InterlinearToken("Πατὴρ", "Patēr", "Pai", "G3962", "N-NMS", "πατήρ"),
        InterlinearToken("Πατρός", "Patros", "do Pai", "G3962", "N-GMS", "πατήρ"),
        InterlinearToken("Κυρίου", "Kyriou", "do Senhor", "G2962", "N-GMS", "κύριος"),
        InterlinearToken("ἡμῶν", "hēmōn", "nosso", "G1473", "Pron-1GP", "ἡμεῖς"),
        InterlinearToken("ἡμᾶς", "hēmas", "nos", "G1473", "Pron-1AP", "ἡμεῖς"),
        InterlinearToken("ἡμῖν", "hēmin", "entre nós / a nós", "G1473", "Pron-1DP", "ἡμεῖς"),
        InterlinearToken("ὑμᾶς", "hymas", "vocês / vós", "G1473", "Pron-2AP", "ὑμεῖς"),
        InterlinearToken("ὑμῶν", "hymōn", "de vós", "G1473", "Pron-2GP", "ὑμεῖς"),
        InterlinearToken("Ἰησοῦ", "Iēsou", "de Jesus", "G2424", "N-GMS", "Ἰησοῦς"),
        InterlinearToken("Χριστοῦ", "Christou", "de Cristo", "G5547", "N-GMS", "Χριστός"),
        InterlinearToken("κατὰ", "kata", "segundo/conforme", "G2596", "Prep", "κατά"),
        InterlinearToken("πολὺ", "poly", "grande/muito", "G4183", "Adj-ANS", "πολύς"),
        InterlinearToken("αὐτοῦ", "autou", "sua/dele", "G846", "Pron-GMS", "αὐτός"),
        InterlinearToken("αὐτῶν", "autōn", "deles", "G846", "Pron-GMP", "αὐτός"),
        InterlinearToken("εἰς", "eis", "para", "G1519", "Prep", "εἰς"),
        InterlinearToken("δι’", "di’", "por meio de", "G1223", "Prep", "διά"),
        InterlinearToken("διὰ", "dia", "por meio de", "G1223", "Prep", "διά"),
        InterlinearToken("ἐκ", "ek", "dentre", "G1537", "Prep", "ἐκ"),
        InterlinearToken("νεκρῶν", "nekrōn", "mortos", "G3498", "Adj-GMP", "νεκρός"),
        InterlinearToken("ἐν", "en", "em", "G1722", "Prep", "ἐν"),
        InterlinearToken("οὐρανοῖς", "ouranois", "céus", "G3772", "N-DMP", "οὐρανός"),
        InterlinearToken("οὐρανῶν", "ouranōn", "dos céus", "G3772", "N-GMP", "οὐρανός"),
        InterlinearToken("πρὸς", "pros", "junto a/com", "G4314", "Prep", "πρός"),
        InterlinearToken("ἐθεασάμεθα", "etheasametha", "contemplamos", "G2300", "V-ADI-1P", "θεάομαι"),
        InterlinearToken("μονογενοῦς", "monogenous", "do Unigênito", "G3439", "Adj-GMS", "μονογενής"),
        InterlinearToken("μονογενῆ", "monogenē", "Unigênito", "G3439", "Adj-AMS", "μονογενής"),
        InterlinearToken("πλήρης", "plērēs", "cheio", "G4134", "Adj-NMS", "πλήρης"),
        InterlinearToken("γὰρ", "gar", "pois", "G1063", "Conj", "γάρ"),
        InterlinearToken("οὐκ", "ouk", "não", "G3762", "Adv", "οὐ"),
        InterlinearToken("οὐ", "ou", "não", "G3762", "Adv", "οὐ"),
        InterlinearToken("ἠγάπησεν", "ēgapēsen", "amou", "G25", "V-AAI-3S", "ἀγαπάω"),
        InterlinearToken("κόσμον", "kosmon", "mundo", "G2889", "N-AMS", "κόσμος"),
        InterlinearToken("Υἱὸν", "Huion", "Filho", "G5207", "N-AMS", "υἱός"),
        InterlinearToken("ζωὴν", "zōēn", "vida", "G2222", "N-AFS", "ζωή"),
        InterlinearToken("αἰώνιον", "aiōnion", "eterna", "G166", "Adj-AFS", "αἰώνιος"),
        InterlinearToken("Μακάριοι", "Makarioi", "Bem-aventurados", "G3107", "Adj-NMP", "μακάριος"),
        InterlinearToken("πτωχοὶ", "ptōchoi", "os pobres", "G4434", "Adj-NMP", "πτωχός"),
        InterlinearToken("πνεύματι", "pneumati", "em espírito", "G4151", "N-DNS", "πνεῦμα"),
        InterlinearToken("βασιλεία", "basileia", "reino", "G932", "N-NFS", "βασιλεία"),
        // Common Hebrew words
        InterlinearToken("בְּרֵאשִׁית", "bərēʾšîṯ", "No princípio", "H7225", "Prep+N-fs", "רֵאשִׁית"),
        InterlinearToken("בָּרָא", "bārāʾ", "criou", "H1254", "V-Qal-Perf-3ms", "בָּרָא"),
        InterlinearToken("אֱלֹהִים", "ʾĕlōhîm", "Deus", "H430", "N-mp", "אֱלֹהִים"),
        InterlinearToken("אֵת", "ʾēṯ", "[marc. obj.]", "H853", "DirObj", "אֵת"),
        InterlinearToken("וְאֵת", "wəʾēṯ", "e [marc. obj.]", "H853", "Conj+DirObj", "אֵת"),
        InterlinearToken("הַשָּׁמַיִם", "haššāmayim", "os céus", "H8064", "Art+N-mp", "שָׁמַיִם"),
        InterlinearToken("הָאָרֶץ", "hāʾāreṣ", "a terra", "H776", "Art+N-fs", "אֶרֶץ"),
        InterlinearToken("וְהָאָרֶץ", "wəhāʾāreṣ", "e a terra", "H776", "Conj+Art+N-fs", "אֶרֶץ"),
        InterlinearToken("תֹהוּ", "ṯōhû", "sem forma", "H8414", "N-ms", "תֹּהוּ"),
        InterlinearToken("וָבֹהוּ", "wāḇōhû", "e vazia", "H922", "Conj+N-ms", "בֹּהוּ"),
        InterlinearToken("וְחֹשֶׁךְ", "wəḥōšeḵ", "e trevas", "H2822", "Conj+N-ms", "חֹשֶׁךְ"),
        InterlinearToken("תְהוֹם", "ṯəhôm", "abismo", "H8415", "N-fs", "תְּהוֹם"),
        InterlinearToken("הַמָּיִם", "hammāyim", "as águas", "H4325", "Art+N-mp", "מַיִם"),
        InterlinearToken("וְרוּחַ", "wərûaḥ", "e o Espírito", "H7307", "Conj+N-fs", "רוּחַ"),
        InterlinearToken("יְהוָה", "YHWH", "O Senhor", "H3068", "N-Proper", "יְהוָה"),
        InterlinearToken("טוֹב", "ṭôḇ", "bondade", "H2896", "N-ms", "טוֹב"),
        InterlinearToken("וָחֶסֶד", "wāḥeseḏ", "e amor leal", "H2617", "Conj+N-ms", "חֶסֶד"),
        InterlinearToken("יִרְדְּפוּנִי", "yirdəp̄ûnî", "me perseguirão", "H7291", "V-Qal-Impf-3mp+1cs", "רָדַף"),
        InterlinearToken("וְשַׁבְתִּי", "wəšaḇtî", "e habitarei", "H3427", "Conj+V-Qal-Perf-1cs", "יָשַׁב"),
        InterlinearToken("אַשְׁרֵי", "ʾašrê", "Bem-aventurado", "H835", "N-mp", "אֶשֶׁר"),
        InterlinearToken("בְּתוֹרַת", "bəṯôraṯ", "na lei/instrução de", "H8451", "Prep+N-fs", "תּוֹרָה"),
        InterlinearToken("אֶהְיֶה", "ʾehyeh", "EU SOU", "H1961", "V-Qal-Impf-1cs", "הָיָה"),
        InterlinearToken("שְׁלוֹמֵנוּ", "šəlômēnû", "nossa paz", "H7965", "N-ms", "שָׁלוֹם"),
        InterlinearToken("נִרְפָּא", "nirpāʾ", "foi curado", "H7495", "V-Niphal-Perf-3ms", "רָפָא")
    )

    private val greekFallbackByClean: Map<String, InterlinearToken> by lazy {
        buildMap {
            commonWordTokens.forEach { token ->
                if (!token.strongId.startsWith("H", ignoreCase = true)) {
                    putIfAbsent(MorphologyDecoder.stripPunctuation(token.originalWord), token)
                }
            }
        }
    }

    private val greekFallbackByNorm: Map<String, InterlinearToken> by lazy {
        buildMap {
            commonWordTokens.forEach { token ->
                if (!token.strongId.startsWith("H", ignoreCase = true)) {
                    putIfAbsent(MorphologyDecoder.normalizeBiblicalToken(token.originalWord), token)
                    if (token.lemma.isNotBlank()) {
                        putIfAbsent(MorphologyDecoder.normalizeBiblicalToken(token.lemma), token)
                    }
                }
            }
        }
    }

    private val hebrewFallbackByClean: Map<String, InterlinearToken> by lazy {
        buildMap {
            commonWordTokens.forEach { token ->
                if (token.strongId.startsWith("H", ignoreCase = true)) {
                    putIfAbsent(MorphologyDecoder.stripPunctuation(token.originalWord), token)
                }
            }
        }
    }

    private val hebrewFallbackByNorm: Map<String, InterlinearToken> by lazy {
        buildMap {
            commonWordTokens.forEach { token ->
                if (token.strongId.startsWith("H", ignoreCase = true)) {
                    putIfAbsent(MorphologyDecoder.normalizeBiblicalToken(token.originalWord), token)
                    if (token.lemma.isNotBlank()) {
                        putIfAbsent(MorphologyDecoder.normalizeBiblicalToken(token.lemma), token)
                    }
                }
            }
        }
    }

    private val lexiconByStrongId: Map<String, LexiconEntry> by lazy {
        buildMap {
            supplementalStrongEntries.forEach { (k, v) -> put(k.uppercase(), v) }
            lexiconEntries.forEach { put(it.strongId.uppercase(), it) }
        }
    }

    fun findFallbackToken(rawWord: String, isOldTestament: Boolean): InterlinearToken? {
        val clean = MorphologyDecoder.stripPunctuation(rawWord)
        if (clean.isBlank()) return null
        val cleanMap = if (isOldTestament) hebrewFallbackByClean else greekFallbackByClean
        cleanMap[clean]?.let { return it }

        val norm = MorphologyDecoder.normalizeBiblicalToken(clean)
        val normMap = if (isOldTestament) hebrewFallbackByNorm else greekFallbackByNorm
        normMap[norm]?.let { return it }

        if (isOldTestament && norm.length > 2 && norm.first() in MorphologyDecoder.hebrewInseparablePrefixes) {
            normMap[norm.drop(1)]?.let { return it }
        }
        return null
    }

    private val strongCodeRegex = Regex("([HG])0*(\\d+)")

    fun findStrongEntry(strongId: String): LexiconEntry? {
        val raw = strongId.trim().uppercase()
        if (raw.isBlank()) return null
        lexiconByStrongId[raw]?.let { return it }

        // Prefer lexical Strongs over STEPBible prefix codes (H9001..H9099)
        val matches = strongCodeRegex.findAll(raw).map { m ->
            val letter = m.groupValues[1]
            val num = m.groupValues[2].toIntOrNull() ?: 0
            Pair("$letter$num", letter == "H" && num in 9000..9099)
        }.toList()

        val prioritized = matches.sortedBy { if (it.second) 1 else 0 }
        for ((canonical, _) in prioritized) {
            lexiconByStrongId[canonical]?.let { return it }
        }
        return null
    }
}

