package com.example.data

import com.example.model.BibleBook
import com.example.model.CanonicalDivision
import com.example.model.DailyVerseStudy
import com.example.model.OriginalLanguage
import com.example.model.Testament
import java.util.Calendar

object BibleCatalogData {

    val allBooks: List<BibleBook> = listOf(
        // Pentateuco
        BibleBook("gen", "Gênesis", "Gn", Testament.OLD, CanonicalDivision.PENTATEUCH, 50, "Moisés", "Criação, Queda e Aliança Patriarcal", "Relata as origens do cosmos, da humanidade e da eleição de Abraão e sua descendência."),
        BibleBook("exo", "Êxodo", "Êx", Testament.OLD, CanonicalDivision.PENTATEUCH, 40, "Moisés", "Redenção, Lei e Tabernáculo", "Narra a libertação de Israel do Egito, a aliança no Sinai e a habitação de Deus entre o povo."),
        BibleBook("lev", "Levítico", "Lv", Testament.OLD, CanonicalDivision.PENTATEUCH, 27, "Moisés", "Santidade e Sacrifício", "Instruções sacerdotais sobre adoração, expiação e vida santa diante de Deus."),
        BibleBook("num", "Números", "Nm", Testament.OLD, CanonicalDivision.PENTATEUCH, 36, "Moisés", "Peregrinação no Deserto", "A jornada de Israel do Sinai até as campinas de Moabe e a fidelidade divina."),
        BibleBook("deu", "Deuteronômio", "Dt", Testament.OLD, CanonicalDivision.PENTATEUCH, 34, "Moisés", "Renovação da Aliança", "Discursos finais de Moisés exortando Israel à obediência amorosa (Shemá)."),

        // Históricos
        BibleBook("jos", "Josué", "Js", Testament.OLD, CanonicalDivision.HISTORICAL, 24, "Josué", "Conquista e Herança", "Entrada na Terra Prometida e cumprimento das promessas feitas aos patriarcas."),
        BibleBook("jdg", "Juízes", "Jz", Testament.OLD, CanonicalDivision.HISTORICAL, 21, "Samuel (trad.)", "Ciclos de Apostasia e Livramento", "Retrata a instabilidade espiritual de Israel antes da monarquia."),
        BibleBook("rut", "Rute", "Rt", Testament.OLD, CanonicalDivision.HISTORICAL, 4, "Samuel (trad.)", "Fidelidade e Resgate (Goel)", "História de amor leal (ḥesed) e redenção na linhagem do rei Davi."),
        BibleBook("1sa", "1 Samuel", "1Sm", Testament.OLD, CanonicalDivision.HISTORICAL, 31, "Samuel / Natã / Gade", "Transição para a Monarquia", "Ministério de Samuel, reinado de Saul e unção de Davi."),
        BibleBook("2sa", "2 Samuel", "2Sm", Testament.OLD, CanonicalDivision.HISTORICAL, 24, "Natã / Gade", "Aliança Davídica", "Reinado de Davi em Jerusalém e a promessa do trono eterno (2Sm 7)."),
        BibleBook("1ki", "1 Reis", "1Rs", Testament.OLD, CanonicalDivision.HISTORICAL, 22, "Jeremias (trad.)", "Templo de Salomão e Divisão", "Apogeu de Salomão, divisão entre Israel e Judá e o ministério de Elias."),
        BibleBook("2ki", "2 Reis", "2Rs", Testament.OLD, CanonicalDivision.HISTORICAL, 25, "Jeremias (trad.)", "Profetas e Exílio", "Ministério de Eliseu e a queda de Samaria e Jerusalém."),
        BibleBook("1ch", "1 Crônicas", "1Cr", Testament.OLD, CanonicalDivision.HISTORICAL, 29, "Esdras (trad.)", "Genealogia e Adoração", "Relê a história de Davi com foco no templo e na liturgia."),
        BibleBook("2ch", "2 Crônicas", "2Cr", Testament.OLD, CanonicalDivision.HISTORICAL, 36, "Esdras (trad.)", "Reis de Judá e Reforma", "História do reino do sul destacando reformas e arrependimento."),
        BibleBook("ezr", "Esdras", "Ed", Testament.OLD, CanonicalDivision.HISTORICAL, 10, "Esdras", "Restauração do Templo e da Lei", "Retorno do exílio babilônico e reavivamento espiritual."),
        BibleBook("neh", "Neemias", "Ne", Testament.OLD, CanonicalDivision.HISTORICAL, 13, "Neemias", "Reconstrução dos Muros", "Liderança íntegra na reconstrução de Jerusalém e renovação da aliança."),
        BibleBook("est", "Ester", "Et", Testament.OLD, CanonicalDivision.HISTORICAL, 10, "Desconhecido", "Providência Oculta", "Preservação do povo judeu na Pérsia pela soberana providência divina."),

        // Poéticos & Sapienciais
        BibleBook("job", "Jó", "Jó", Testament.OLD, CanonicalDivision.POETIC, 42, "Desconhecido", "Sofrimento e Soberania", "Diálogo poético sobre teodiceia, fé desinteressada e sabedoria divina."),
        BibleBook("psa", "Salmos", "Sl", Testament.OLD, CanonicalDivision.POETIC, 150, "Davi, Asafe e outros", "Hinário e Orações de Israel", "Cânticos de louvor, lamento, sabedoria e profecia messiânica."),
        BibleBook("pro", "Provérbios", "Pv", Testament.OLD, CanonicalDivision.POETIC, 31, "Salomão e sábios", "Temor do Senhor e Sabedoria", "Máximas práticas para uma vida justa diante de Deus."),
        BibleBook("ecc", "Eclesiastes", "Ec", Testament.OLD, CanonicalDivision.POETIC, 12, "Salomão (Qohelet)", "Sentido da Vida Debaixo do Sol", "Reflexão filosófica sobre a transitoriedade e o temor a Deus."),
        BibleBook("sng", "Cânticos", "Ct", Testament.OLD, CanonicalDivision.POETIC, 8, "Salomão", "Beleza do Amor Conjugal", "Poesia lírica celebrando o amor fiel."),

        // Profetas
        BibleBook("isa", "Isaías", "Is", Testament.OLD, CanonicalDivision.PROPHETS, 66, "Isaías", "Santo de Israel e Servo Sofredor", "Juízo e consolação messiânica culminando no Servo Sofredor."),
        BibleBook("jer", "Jeremias", "Jr", Testament.OLD, CanonicalDivision.PROPHETS, 52, "Jeremias", "Nova Aliança no Coração", "Chamado ao arrependimento e promessa da Nova Aliança (Jr 31)."),
        BibleBook("lam", "Lamentações", "Lm", Testament.OLD, CanonicalDivision.PROPHETS, 5, "Jeremias", "Lamento e Misericórdia", "Poemas acrósticos sobre Jerusalém e a fidelidade divina."),
        BibleBook("ezk", "Ezequiel", "Ez", Testament.OLD, CanonicalDivision.PROPHETS, 48, "Ezequiel", "Glória de Deus e Novo Espírito", "Visões da glória divina, vale de ossos secos e novo templo."),
        BibleBook("dan", "Daniel", "Dn", Testament.OLD, CanonicalDivision.PROPHETS, 12, "Daniel", "Reino Eterno de Deus", "Fidelidade na Babilônia e visões proféticas do Filho do Homem."),
        BibleBook("hos", "Oseias", "Os", Testament.OLD, CanonicalDivision.PROPHETS, 14, "Oseias", "Amor Fiel por um Povo Infiel", "Casamento profético ilustrando a misericórdia restauradora."),
        BibleBook("jol", "Joel", "Jl", Testament.OLD, CanonicalDivision.PROPHETS, 3, "Joel", "Dia do Senhor e Espírito", "Praga de gafanhotos, arrependimento e promessa do Pentecostes."),
        BibleBook("amo", "Amós", "Am", Testament.OLD, CanonicalDivision.PROPHETS, 9, "Amós", "Justiça Social e Retidão", "Denúncia contra a opressão e chamado à justiça."),
        BibleBook("oba", "Obadias", "Ob", Testament.OLD, CanonicalDivision.PROPHETS, 1, "Obadias", "Juízo sobre Edom", "Profecia sobre a queda do orgulho e a vitória do Reino."),
        BibleBook("jon", "Jonas", "Jn", Testament.OLD, CanonicalDivision.PROPHETS, 4, "Jonas", "Compaixão Universal de Deus", "Missão profética em Nínive revelando a graça às nações."),
        BibleBook("mic", "Miqueias", "Mq", Testament.OLD, CanonicalDivision.PROPHETS, 7, "Miqueias", "Justiça, Misericórdia e Belém", "Profecia de Belém e do andar humilde com Deus."),
        BibleBook("nah", "Naum", "Na", Testament.OLD, CanonicalDivision.PROPHETS, 3, "Naum", "Consolação contra Nínive", "Anúncio da justiça divina contra a opressão assíria."),
        BibleBook("hab", "Habacuque", "Hc", Testament.OLD, CanonicalDivision.PROPHETS, 3, "Habacuque", "O Justo Viverá pela Fé", "Diálogo de fé diante da injustiça culminando em louvor."),
        BibleBook("zep", "Sofonias", "Sf", Testament.OLD, CanonicalDivision.PROPHETS, 3, "Sofonias", "Dia do Senhor e Júbilo", "Purificação do remanescente e alegria de Deus."),
        BibleBook("hag", "Ageu", "Ag", Testament.OLD, CanonicalDivision.PROPHETS, 2, "Ageu", "Prioridade na Casa de Deus", "Exortação à reconstrução do segundo templo."),
        BibleBook("zec", "Zacarias", "Zc", Testament.OLD, CanonicalDivision.PROPHETS, 14, "Zacarias", "Rei Humilde e Fonte Purificadora", "Visões noturnas e profecias messiânicas."),
        BibleBook("mal", "Malaquias", "Ml", Testament.OLD, CanonicalDivision.PROPHETS, 4, "Malaquias", "Mensageiro da Aliança", "Último profeta do AT anunciando o precursor e o Sol da Justiça."),

        // Evangelhos & Atos
        BibleBook("mat", "Mateus", "Mt", Testament.NEW, CanonicalDivision.GOSPELS, 28, "Mateus", "Jesus, o Rei Messias Prometido", "Apresenta Jesus como cumprimento das Escrituras e Mestre do Sermão do Monte."),
        BibleBook("mrk", "Marcos", "Mc", Testament.NEW, CanonicalDivision.GOSPELS, 16, "João Marcos", "O Servo Sofredor em Ação", "Evangelho ágil focado nos milagres, discipulado e cruz de Cristo."),
        BibleBook("luk", "Lucas", "Lc", Testament.NEW, CanonicalDivision.GOSPELS, 24, "Lucas", "Salvador de Todas as Nações", "Relato histórico detalhado destacando graça, oração e compaixão."),
        BibleBook("jhn", "João", "Jo", Testament.NEW, CanonicalDivision.GOSPELS, 21, "João", "O Verbo Encarnado e Vida Eterna", "Sinais teológicos e discursos do 'Eu Sou' para gerar fé em Cristo."),
        BibleBook("act", "Atos", "At", Testament.NEW, CanonicalDivision.GOSPELS, 28, "Lucas", "Expansão da Igreja pelo Espírito", "De Jerusalém até Roma: Pentecostes e missões apostólicas."),

        // Epístolas
        BibleBook("rom", "Romanos", "Rm", Testament.NEW, CanonicalDivision.EPISTLES, 16, "Paulo", "Evangelho da Justiça de Deus", "Tratado sistemático sobre pecado, justificação pela fé e vida no Espírito."),
        BibleBook("1co", "1 Coríntios", "1Co", Testament.NEW, CanonicalDivision.EPISTLES, 16, "Paulo", "Unidade, Cruz e Amor (Ágape)", "Respostas pastorais sobre santidade, ceia, dons e ressurreição."),
        BibleBook("2co", "2 Coríntios", "2Co", Testament.NEW, CanonicalDivision.EPISTLES, 13, "Paulo", "Poder na Fraqueza", "Defesa do ministério da Nova Aliança e generosidade cristã."),
        BibleBook("gal", "Gálatas", "Gl", Testament.NEW, CanonicalDivision.EPISTLES, 6, "Paulo", "Liberdade Cristã pela Fé", "Carta magna da justificação somente pela fé e do fruto do Espírito."),
        BibleBook("eph", "Efésios", "Ef", Testament.NEW, CanonicalDivision.EPISTLES, 6, "Paulo", "A Igreja como Corpo de Cristo", "Bênçãos espirituais nas regiões celestiais e unidade em Cristo."),
        BibleBook("php", "Filipenses", "Fp", Testament.NEW, CanonicalDivision.EPISTLES, 4, "Paulo", "Alegria e Mente de Cristo", "Hino cristológico (Fp 2) e contentamento cristão."),
        BibleBook("col", "Colossenses", "Cl", Testament.NEW, CanonicalDivision.EPISTLES, 4, "Paulo", "Supremacia Cósmica de Cristo", "Plenitude da divindade habitando corporalmente em Cristo."),
        BibleBook("1th", "1 Tessalonicenses", "1Ts", Testament.NEW, CanonicalDivision.EPISTLES, 5, "Paulo", "Santidade e Volta de Cristo", "Encorajamento à igreja jovem e esperança na parousia."),
        BibleBook("2th", "2 Tessalonicenses", "2Ts", Testament.NEW, CanonicalDivision.EPISTLES, 3, "Paulo", "Perseverança até o Dia do Senhor", "Esclarecimentos escatológicos e firmeza na verdade."),
        BibleBook("1ti", "1 Timóteo", "1Tm", Testament.NEW, CanonicalDivision.EPISTLES, 6, "Paulo", "Ordem na Casa de Deus", "Orientações pastorais sobre sã doutrina e liderança."),
        BibleBook("2ti", "2 Timóteo", "2Tm", Testament.NEW, CanonicalDivision.EPISTLES, 4, "Paulo", "Guarda o Bom Depósito", "Testamento espiritual de Paulo sobre fidelidade às Escrituras."),
        BibleBook("tit", "Tito", "Tt", Testament.NEW, CanonicalDivision.EPISTLES, 3, "Paulo", "Graça que Educa", "Organização eclesiástica unindo sã doutrina e caráter transformado."),
        BibleBook("phm", "Filemom", "Fm", Testament.NEW, CanonicalDivision.EPISTLES, 1, "Paulo", "Reconciliação Fraterna", "Apelo pessoal pela restauração de Onésimo como irmão amado."),
        BibleBook("heb", "Hebreus", "Hb", Testament.NEW, CanonicalDivision.EPISTLES, 13, "Desconhecido", "Sumo Sacerdócio de Cristo", "Mostra Cristo superior aos anjos, a Moisés e ao sistema levítico."),
        BibleBook("jas", "Tiago", "Tg", Testament.NEW, CanonicalDivision.EPISTLES, 5, "Tiago", "Sabedoria Prática e Fé Viva", "Fé autêntica demonstrada em obras, paciência e misericórdia."),
        BibleBook("1pe", "1 Pedro", "1Pe", Testament.NEW, CanonicalDivision.EPISTLES, 5, "Pedro", "Peregrinos da Viva Esperança", "Consolo e santidade para peregrinos em meio à provação."),
        BibleBook("2pe", "2 Pedro", "2Pe", Testament.NEW, CanonicalDivision.EPISTLES, 3, "Pedro", "Crescimento na Graça", "Alerta contra falsos mestres e certeza da promessa."),
        BibleBook("1jn", "1 João", "1Jo", Testament.NEW, CanonicalDivision.EPISTLES, 5, "João", "Comunhão, Luz e Amor", "Certeza da vida eterna através da fé ortodoxa e do amor."),
        BibleBook("2jn", "2 João", "2Jo", Testament.NEW, CanonicalDivision.EPISTLES, 1, "João", "Andar na Verdade", "Discernimento doutrinário e hospitalidade cristã."),
        BibleBook("3jn", "3 João", "3Jo", Testament.NEW, CanonicalDivision.EPISTLES, 1, "João", "Cooperação com a Verdade", "Elogio a Gaio pela fidelidade no apoio aos missionários."),
        BibleBook("jud", "Judas", "Jd", Testament.NEW, CanonicalDivision.EPISTLES, 1, "Judas", "Batalhar pela Fé", "Chamado à vigilância doutrinária e perseverança."),

        // Apocalipse
        BibleBook("rev", "Apocalipse", "Ap", Testament.NEW, CanonicalDivision.APOCALYPTIC, 22, "João", "O Cordeiro Vence", "Visões proféticas da soberania de Cristo e consumação da Nova Jerusalém.")
    )

    val dailyStudies: List<DailyVerseStudy> = listOf(
        DailyVerseStudy(
            verseRef = "1 Pedro 1:3",
            bookId = "1pe",
            chapter = 1,
            verseNumber = 3,
            originalExcerpt = "Εὐλογητὸς ὁ Θεὸς καὶ Πατὴρ ... ὁ κατὰ τὸ πολὺ αὐτοῦ ἔλεος ἀναγεννήσας ἡμᾶς εἰς ἐλπίδα ζῶσαν",
            verseText = "Bendito seja o Deus e Pai de nosso Senhor Jesus Cristo, que, segundo a sua grande misericórdia, nos regenerou para uma viva esperança, pela ressurreição de Jesus Cristo dentre os mortos.",
            translationLabel = "1 Pedro 1:3 · Tradução Brasileira (1917)",
            originalWord = "ἀναγεννάω",
            transliteration = "anagennaō",
            morphology = "V-AAP-NMS",
            originalGloss = "gerar de novo, regenerar do alto",
            strongId = "G313",
            occurrencesCount = 2,
            originalLanguage = OriginalLanguage.GREEK,
            doctrineTitle = "Regeneração",
            doctrineSubtitle = "Ação soberana da misericórdia divina",
            devotionalTitle = "Análise Exegética: ἀναγεννάω e a Esperança Viva",
            devotionalBody = "O particípio aoristo ἀναγεννήσας subordina toda a vida cristã à iniciativa misericordiosa de Deus (πολὺ ἔλεος) ancorada historicamente na ressurreição de Cristo.",
            readingMinutes = 6
        ),
        DailyVerseStudy(
            verseRef = "João 1:14",
            bookId = "jhn",
            chapter = 1,
            verseNumber = 14,
            originalExcerpt = "Καὶ ὁ Λόγος σὰρξ ἐγένετο καὶ ἐσκήνωσεν ἐν ἡμῖν, καὶ ἐθεασάμεθα τὴν δόξαν αὐτοῦ",
            verseText = "E o Verbo se fez carne, e habitou entre nós, e vimos a sua glória, glória como do unigênito do Pai, cheio de graça e de verdade.",
            translationLabel = "João 1:14 · Bíblia Livre (CC BY 3.0)",
            originalWord = "σκηνόω",
            transliteration = "skēnoō",
            morphology = "V-AAI-3S",
            originalGloss = "armar tenda, tabernacular",
            strongId = "G4637",
            occurrencesCount = 5,
            originalLanguage = OriginalLanguage.GREEK,
            doctrineTitle = "Encarnação",
            doctrineSubtitle = "Continuidade tipológica com o Tabernáculo",
            devotionalTitle = "O Verbo Tabernaculou entre Nós",
            devotionalBody = "O verbo ἐσκήνωσεν remete ao hebraico šākan (habitar no santuário, Êx 40:34), revelando Cristo como o verdadeiro lugar da presença gloriosa de Deus.",
            readingMinutes = 5
        ),
        DailyVerseStudy(
            verseRef = "Salmos 23:6",
            bookId = "psa",
            chapter = 23,
            verseNumber = 6,
            originalExcerpt = "אַךְ טוֹב וָחֶסֶד יִרְדְּפוּנִי כָּל־יְמֵי חַיָּי",
            verseText = "Certamente que a bondade e a misericórdia me seguirão todos os dias da minha vida; e habitarei na casa do Senhor por longos dias.",
            translationLabel = "Salmos 23:6 · Almeida Clássica (1911)",
            originalWord = "חֶסֶד",
            transliteration = "ḥesed",
            morphology = "N-ms",
            originalGloss = "amor leal da aliança, fidelidade pactual",
            strongId = "H2617",
            occurrencesCount = 248,
            originalLanguage = OriginalLanguage.HEBREW,
            doctrineTitle = "Aliança",
            doctrineSubtitle = "Fidelidade pactual de YHWH",
            devotionalTitle = "O Alcance Pactual de חֶסֶד (ḥesed)",
            devotionalBody = "Associado ao verbo רָדַף ('perseguir'), o termo חֶסֶד demonstra que a lealdade pactual de Deus vai ativamente ao encontro do adorador.",
            readingMinutes = 6
        ),
        DailyVerseStudy(
            verseRef = "Romanos 8:1",
            bookId = "rom",
            chapter = 8,
            verseNumber = 1,
            originalExcerpt = "Οὐδὲν ἄρα νῦν κατάκριμα τοῖς ἐν Χριστῷ Ἰησοῦ",
            verseText = "Portanto agora nenhuma condenação há para os que estão em Cristo Jesus.",
            translationLabel = "Romanos 8:1 · Tradução Brasileira (1917)",
            originalWord = "κατάκριμα",
            transliteration = "katakrima",
            morphology = "N-NNS",
            originalGloss = "sentença condenatória forense",
            strongId = "G2631",
            occurrencesCount = 3,
            originalLanguage = OriginalLanguage.GREEK,
            doctrineTitle = "Justificação",
            doctrineSubtitle = "Absolvição forense em união com Cristo",
            devotionalTitle = "Οὐδὲν κατάκριμα: Estrutura Sintática da Absolvição",
            devotionalBody = "A posição enfática de Οὐδὲν ('nenhuma em absoluto') no início da oração nega toda sentença condenatória (κατάκριμα) para os unidos a Cristo.",
            readingMinutes = 7
        )
    )

    fun getDailyStudyForToday(): DailyVerseStudy {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 329).mod(dailyStudies.size)
        return dailyStudies[index]
    }
}
