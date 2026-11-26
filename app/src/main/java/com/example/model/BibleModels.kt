package com.example.model

enum class BibleTranslation(
    val code: String,
    val fullName: String,
    val languageLabel: String
) {
    BLIVRE("BLIVRE", "Bíblia Livre", "Português · CC BY 3.0 BR"),
    BLTR("BLTR", "Bíblia Livre (Textus Receptus)", "Português · CC BY 3.0 BR"),
    ALM("ALM", "Almeida Clássica (1911)", "Português · Domínio Público");

    companion object {
        fun fromCodeOrDefault(code: String?): BibleTranslation {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: BLIVRE
        }
    }
}

data class ImportedBibleVersion(
    val id: String,
    val code: String,
    val fullName: String,
    val languageLabel: String,
    val localFilePath: String,
    val schemaType: String,
    val versesCount: Int,
    val createdAtMillis: Long
)

data class ImportedInterlinearPack(
    val id: String,
    val fileName: String,
    val corpusType: String,
    val booksSummary: String,
    val versesCount: Int,
    val tokensCount: Int,
    val importedAtMillis: Long
)

data class StepInterlinearImportProgress(
    val currentFileIndex: Int,
    val totalFiles: Int,
    val currentFileName: String,
    val parsedVersesCount: Int,
    val parsedTokensCount: Int
)

enum class Testament(val labelPt: String) {
    OLD("Antigo Testamento"),
    NEW("Novo Testamento")
}

enum class CanonicalDivision(val label: String, val testament: Testament) {
    PENTATEUCH("Pentateuco", Testament.OLD),
    HISTORICAL("Históricos", Testament.OLD),
    POETIC("Poéticos & Sapienciais", Testament.OLD),
    PROPHETS("Profetas", Testament.OLD),
    GOSPELS("Evangelhos & Atos", Testament.NEW),
    EPISTLES("Epístolas", Testament.NEW),
    APOCALYPTIC("Apocalipse", Testament.NEW)
}

data class BibleBook(
    val id: String,
    val name: String,
    val shortName: String,
    val testament: Testament,
    val division: CanonicalDivision,
    val chaptersCount: Int,
    val author: String,
    val theme: String,
    val overview: String
)

data class InterlinearToken(
    val originalWord: String,
    val transliteration: String,
    val portugueseGloss: String,
    val strongId: String,
    val morphology: String,
    val lemma: String
)

data class BibleVerse(
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val originalTextLine: String,
    val translations: Map<BibleTranslation, String>,
    val customTranslations: Map<String, String> = emptyMap(),
    val interlinear: List<InterlinearToken>,
    val commentary: String,
    val textualNote: String = "",
    val crossReferences: List<String> = emptyList()
)

enum class OriginalLanguage(val label: String) {
    GREEK("Grego Koiné"),
    HEBREW("Hebraico Bíblico")
}

data class BookOccurrenceStat(
    val bookName: String,
    val count: Int
)

data class WordOccurrenceHit(
    val verseRef: String,
    val bookId: String,
    val chapter: Int,
    val verseNumber: Int,
    val originalExcerpt: String,
    val grammarForm: String,
    val portugueseText: String
)

data class LexiconEntry(
    val strongId: String,
    val language: OriginalLanguage,
    val originalWord: String,
    val transliteration: String,
    val pronunciation: String,
    val shortGloss: String,
    val etymology: String,
    val definition: String,
    val semanticDomains: List<String>,
    val theologicalUsage: String,
    val keyVerses: List<String>,
    val occurrences: Int,
    val bookDistribution: List<BookOccurrenceStat> = emptyList(),
    val sampleOccurrences: List<WordOccurrenceHit> = emptyList()
)

data class CommentaryEntry(
    val id: String,
    val bookId: String,
    val bookName: String,
    val passageRef: String,
    val author: String,
    val literaryStructure: String,
    val historicalContext: String,
    val exegesis: String,
    val theologicalApplication: String
)

data class DictionaryEntry(
    val id: String,
    val term: String,
    val category: String,
    val originalTerm: String,
    val summary: String,
    val article: String,
    val relatedVerses: List<String>
)

data class DailyVerseStudy(
    val verseRef: String,
    val bookId: String,
    val chapter: Int,
    val verseNumber: Int,
    val originalExcerpt: String,
    val verseText: String,
    val translationLabel: String,
    val originalWord: String,
    val transliteration: String,
    val morphology: String,
    val originalGloss: String,
    val strongId: String,
    val occurrencesCount: Int,
    val originalLanguage: OriginalLanguage,
    val doctrineTitle: String,
    val doctrineSubtitle: String,
    val devotionalTitle: String,
    val devotionalBody: String,
    val readingMinutes: Int
)

data class SelectedWordStudy(
    val clickedWord: String,
    val clickedTransliteration: String,
    val contextualGloss: String,
    val morphologyCode: String,
    val lemma: String,
    val verseRef: String = "",
    val lexiconEntry: LexiconEntry,
    val isIndexedInStrongCatalog: Boolean = true
)

