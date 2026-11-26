package com.example.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BibleCatalogData
import com.example.data.DetectedImportKind
import com.example.data.OpenBibleRepository
import com.example.data.PendingSqliteBible
import com.example.data.ScholarlyBibleVersesData
import com.example.data.ScholarlyLibraryData
import com.example.data.SqliteBibleRepository
import com.example.data.StepInterlinearRepository
import com.example.data.TheoAiRepository
import com.example.data.ThemePreferencesRepository
import com.example.data.UserStudyRepository
import com.example.model.AiStreamState
import com.example.model.BibleBook
import com.example.model.BibleTranslation
import com.example.model.BibleVerse
import com.example.model.DailyVerseStudy
import com.example.model.ImportedBibleVersion
import com.example.model.ImportedInterlinearPack
import com.example.model.InterlinearToken
import com.example.model.LexiconEntry
import com.example.model.MorphologyDecoder
import com.example.model.OriginalLanguage
import com.example.model.ReadingPlan
import com.example.model.SelectedWordStudy
import com.example.model.StepInterlinearImportProgress
import com.example.model.StudyNote
import com.example.model.Testament
import com.example.model.UiState
import com.example.model.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TheoChatMessage(
    val id: String,
    val isFromUser: Boolean,
    val text: String
)

enum class ReaderDisplayMode(val label: String) {
    TEXT("Texto"),
    INTERLINEAR("Interlinear"),
    PARALLEL("Paralelo"),
    COMMENTARY("Comentário")
}

data class BibleReaderState(
    val selectedBook: BibleBook = BibleCatalogData.allBooks.first { it.id == "1pe" },
    val selectedChapter: Int = 1,
    val selectedTranslation: BibleTranslation = BibleTranslation.BLIVRE,
    val selectedCustomVersionId: String? = null,
    val importedBibles: List<ImportedBibleVersion> = emptyList(),
    val importedInterlinearPacks: List<ImportedInterlinearPack> = emptyList(),
    val interlinearBookIds: Set<String> = emptySet(),
    val isImportingInterlinear: Boolean = false,
    val interlinearImportProgress: StepInterlinearImportProgress? = null,
    val displayMode: ReaderDisplayMode = ReaderDisplayMode.INTERLINEAR,
    val isBookSelectorOpen: Boolean = true,
    val selectedTestament: Testament = Testament.NEW,
    val searchQuery: String = "",
    val verses: List<BibleVerse> = ScholarlyBibleVersesData.getVerses("1pe", 1),
    val isLoadingChapter: Boolean = false,
    val isSyncingFullChapter: Boolean = false,
    val isEnrichingOriginals: Boolean = false,
    val chapterError: String? = null
) {
    val activeTranslationCode: String
        get() = importedBibles.firstOrNull { it.id == selectedCustomVersionId }?.code
            ?: selectedTranslation.code
}

class VerboViewModel(
    private val userStudyRepository: UserStudyRepository,
    private val theoAiRepository: TheoAiRepository,
    private val themePreferencesRepository: ThemePreferencesRepository,
    private val currentUserId: String,
    private val userDisplayName: String,
    private val openBibleRepository: OpenBibleRepository = OpenBibleRepository(),
    private val sqliteBibleRepository: SqliteBibleRepository = SqliteBibleRepository(),
    private val stepInterlinearRepository: StepInterlinearRepository = StepInterlinearRepository()
) : ViewModel() {

    val dailyStudy: DailyVerseStudy = BibleCatalogData.getDailyStudyForToday()

    val isDarkMode: StateFlow<Boolean> = themePreferencesRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = false
        )

    val profileState: StateFlow<UiState<UserProfile?>> = userStudyRepository
        .observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(TAG, "Error observing profile", error)
            emit(UiState.Error(error.message ?: "Erro ao carregar perfil."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val notesState: StateFlow<UiState<List<StudyNote>>> = userStudyRepository
        .observeNotes(currentUserId)
        .map<List<StudyNote>, UiState<List<StudyNote>>> { UiState.Success(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(TAG, "Error observing notes", error)
            emit(UiState.Error(error.message ?: "Erro ao carregar notas."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val plansState: StateFlow<UiState<List<ReadingPlan>>> = userStudyRepository
        .observeReadingPlans(currentUserId)
        .map<List<ReadingPlan>, UiState<List<ReadingPlan>>> { UiState.Success(it) }
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(TAG, "Error observing reading plans", error)
            emit(UiState.Error(error.message ?: "Erro ao carregar planos de leitura."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _bibleState = MutableStateFlow(BibleReaderState())
    val bibleState: StateFlow<BibleReaderState> = _bibleState.asStateFlow()

    private val _theoMessages = MutableStateFlow<List<TheoChatMessage>>(emptyList())
    val theoMessages: StateFlow<List<TheoChatMessage>> = _theoMessages.asStateFlow()

    private val _theoStreamState = MutableStateFlow<AiStreamState>(AiStreamState.Idle)
    val theoStreamState: StateFlow<AiStreamState> = _theoStreamState.asStateFlow()

    private val _lastTheoPrompt = MutableStateFlow<String?>(null)

    private val _libraryStreamState = MutableStateFlow<AiStreamState>(AiStreamState.Idle)
    val libraryStreamState: StateFlow<AiStreamState> = _libraryStreamState.asStateFlow()

    private val _occurrenceStreamState = MutableStateFlow<AiStreamState>(AiStreamState.Idle)
    val occurrenceStreamState: StateFlow<AiStreamState> = _occurrenceStreamState.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _selectedLexiconToken = MutableStateFlow<SelectedWordStudy?>(null)
    val selectedLexiconToken: StateFlow<SelectedWordStudy?> = _selectedLexiconToken.asStateFlow()

    private val _wordModalAiState = MutableStateFlow<AiStreamState>(AiStreamState.Idle)
    val wordModalAiState: StateFlow<AiStreamState> = _wordModalAiState.asStateFlow()

    private val _selectedWordVerseLine = MutableStateFlow("")
    private val _selectedWordIsOt = MutableStateFlow(false)

    private val _pendingSqliteImport = MutableStateFlow<PendingSqliteBible?>(null)
    val pendingSqliteImport: StateFlow<PendingSqliteBible?> = _pendingSqliteImport.asStateFlow()

    private val _isImportingSqlite = MutableStateFlow(false)
    val isImportingSqlite: StateFlow<Boolean> = _isImportingSqlite.asStateFlow()

    private val _pickerInterlinearChapters = MutableStateFlow<Set<Int>>(emptySet())
    val pickerInterlinearChapters: StateFlow<Set<Int>> = _pickerInterlinearChapters.asStateFlow()

    private val aiEnrichmentCache = mutableMapOf<String, List<BibleVerse>>()
    private val pendingExternalBibleQueue = ArrayDeque<Uri>()

    private var chapterLoadJob: Job? = null
    private var chapterEnrichJob: Job? = null
    private var wordAnalysisJob: Job? = null
    private var theoJob: Job? = null
    private var libraryJob: Job? = null
    private var occurrenceJob: Job? = null

    init {
        viewModelScope.launch {
            userStudyRepository.ensureUserProfileAndStarterPlan(currentUserId, userDisplayName)
        }
        viewModelScope.launch {
            userStudyRepository.observeImportedBibles().collect { imported ->
                val activeCustomId = _bibleState.value.selectedCustomVersionId
                    ?.takeIf { id -> imported.any { it.id == id } }
                _bibleState.value = _bibleState.value.copy(
                    importedBibles = imported,
                    selectedCustomVersionId = activeCustomId
                )
                refreshCurrentChapterCustomTranslations(imported)
            }
        }
        viewModelScope.launch {
            stepInterlinearRepository.observePacks().collect { packs ->
                _bibleState.value = _bibleState.value.copy(importedInterlinearPacks = packs)
            }
        }
        viewModelScope.launch {
            stepInterlinearRepository.observeAvailableBookIds().collect { bookIds ->
                _bibleState.value = _bibleState.value.copy(interlinearBookIds = bookIds)
                refreshCurrentChapterStepInterlinear()
            }
        }
        viewModelScope.launch {
            userStudyRepository.observeUserProfile(currentUserId).collect { profile ->
                if (profile != null) {
                    val pref = profile.preferredTranslation
                    val importedMatch = _bibleState.value.importedBibles.firstOrNull {
                        it.id == pref || it.code.equals(pref, ignoreCase = true)
                    }
                    if (importedMatch != null) {
                        _bibleState.value = _bibleState.value.copy(selectedCustomVersionId = importedMatch.id)
                    } else {
                        val safeTranslation = BibleTranslation.fromCodeOrDefault(pref)
                        if (_bibleState.value.selectedTranslation != safeTranslation ||
                            _bibleState.value.selectedCustomVersionId != null
                        ) {
                            _bibleState.value = _bibleState.value.copy(
                                selectedTranslation = safeTranslation,
                                selectedCustomVersionId = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            themePreferencesRepository.setDarkMode(enabled)
        }
    }

    fun selectTestament(testament: Testament) {
        _bibleState.value = _bibleState.value.copy(selectedTestament = testament)
    }

    fun updateBibleSearchQuery(query: String) {
        _bibleState.value = _bibleState.value.copy(searchQuery = query)
    }

    fun setTranslation(translation: BibleTranslation) {
        _bibleState.value = _bibleState.value.copy(
            selectedTranslation = translation,
            selectedCustomVersionId = null
        )
        viewModelScope.launch {
            userStudyRepository.updatePreferredTranslation(currentUserId, translation.code)
        }
    }

    fun selectImportedBible(version: ImportedBibleVersion) {
        _bibleState.value = _bibleState.value.copy(selectedCustomVersionId = version.id)
        viewModelScope.launch {
            userStudyRepository.updatePreferredTranslation(currentUserId, version.id)
        }
    }

    fun inspectSqliteBibleUri(uri: Uri) {
        viewModelScope.launch {
            inspectBibleUriSuspend(uri)
        }
    }

    private suspend fun inspectBibleUriSuspend(uri: Uri) {
        _isImportingSqlite.value = true
        val result = sqliteBibleRepository.stageAndInspectUri(uri)
        _isImportingSqlite.value = false
        result.onSuccess { pending ->
            _pendingSqliteImport.value = pending
        }.onFailure { err ->
            _actionMessage.value = err.localizedMessage ?: "Não foi possível importar o arquivo bíblico."
            processNextQueuedBibleUri()
        }
    }

    private fun processNextQueuedBibleUri() {
        val next = pendingExternalBibleQueue.removeFirstOrNull() ?: return
        viewModelScope.launch {
            inspectBibleUriSuspend(next)
        }
    }

    fun confirmSqliteBibleImport(customCode: String, customName: String) {
        val pending = _pendingSqliteImport.value ?: return
        val cleanCode = SqliteBibleRepository.sanitizeCode(customCode.ifBlank { pending.suggestedCode })
        val cleanName = customName.trim().ifBlank { pending.suggestedName }.take(60)
        val version = ImportedBibleVersion(
            id = pending.id,
            code = cleanCode,
            fullName = cleanName,
            languageLabel = "${pending.detectedLanguage} · ${pending.versesCount} vers.",
            localFilePath = pending.localFilePath,
            schemaType = pending.schemaType,
            versesCount = pending.versesCount,
            createdAtMillis = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val saved = userStudyRepository.saveImportedBible(version)
            if (saved.isSuccess) {
                _pendingSqliteImport.value = null
                _bibleState.value = _bibleState.value.copy(selectedCustomVersionId = version.id)
                userStudyRepository.updatePreferredTranslation(currentUserId, version.id)
                _actionMessage.value = "Versão $cleanCode ($cleanName) importada (${pending.versesCount} versículos)."
                processNextQueuedBibleUri()
            } else {
                _actionMessage.value = "Erro ao salvar versão importada."
            }
        }
    }

    fun cancelPendingSqliteImport() {
        _pendingSqliteImport.value?.let { pending ->
            sqliteBibleRepository.discardStagedFile(pending.localFilePath)
        }
        _pendingSqliteImport.value = null
        processNextQueuedBibleUri()
    }

    fun deleteImportedBible(version: ImportedBibleVersion) {
        viewModelScope.launch {
            userStudyRepository.deleteImportedBible(version.id)
            sqliteBibleRepository.deleteLocalFile(version.localFilePath)
            if (_bibleState.value.selectedCustomVersionId == version.id) {
                _bibleState.value = _bibleState.value.copy(selectedCustomVersionId = null)
                userStudyRepository.updatePreferredTranslation(
                    currentUserId,
                    _bibleState.value.selectedTranslation.code
                )
            }
            _actionMessage.value = "Versão ${version.code} removida."
        }
    }

    fun receiveExternalFiles(uris: List<Uri>) {
        val distinctUris = uris.distinct()
        if (distinctUris.isEmpty()) return

        viewModelScope.launch {
            val classified = stepInterlinearRepository.classifyUris(distinctUris)
            val interlinearUris = classified[DetectedImportKind.STEP_INTERLINEAR].orEmpty()
            val bibleUris = classified[DetectedImportKind.BIBLE_TRANSLATION].orEmpty()
            val unknownCount = classified[DetectedImportKind.UNKNOWN].orEmpty().size

            if (interlinearUris.isNotEmpty()) {
                importInterlinearFilesSuspend(interlinearUris)
            }
            if (bibleUris.isNotEmpty()) {
                if (_pendingSqliteImport.value == null && !_isImportingSqlite.value) {
                    bibleUris.drop(1).forEach { pendingExternalBibleQueue.addLast(it) }
                    inspectBibleUriSuspend(bibleUris.first())
                } else {
                    bibleUris.forEach { pendingExternalBibleQueue.addLast(it) }
                }
            }
            if (interlinearUris.isEmpty() && bibleUris.isEmpty() && unknownCount > 0) {
                _actionMessage.value =
                    "Formato não reconhecido. Envie Bíblias (.sqlite, .db, .json, .txt) ou Interlinear STEPBible (.txt, .tsv, .json)."
            }
        }
    }

    fun loadInterlinearChaptersForBook(bookId: String) {
        viewModelScope.launch {
            _pickerInterlinearChapters.value = stepInterlinearRepository.getAvailableChapters(bookId)
        }
    }

    fun importInterlinearFiles(uris: List<Uri>) {
        if (uris.isEmpty() || _bibleState.value.isImportingInterlinear) return
        viewModelScope.launch {
            importInterlinearFilesSuspend(uris)
        }
    }

    private suspend fun importInterlinearFilesSuspend(uris: List<Uri>) {
        if (uris.isEmpty() || _bibleState.value.isImportingInterlinear) return
        _bibleState.value = _bibleState.value.copy(
            isImportingInterlinear = true,
            interlinearImportProgress = null
        )
        val summary = stepInterlinearRepository.importUris(uris) { progress ->
            _bibleState.value = _bibleState.value.copy(interlinearImportProgress = progress)
        }
        _bibleState.value = _bibleState.value.copy(
            isImportingInterlinear = false,
            interlinearImportProgress = null
        )
        refreshCurrentChapterStepInterlinear()
        val importedCount = summary.importedPacks.size
        val totalVerses = summary.importedPacks.sumOf { it.versesCount }
        val totalTokens = summary.importedPacks.sumOf { it.tokensCount }
        _actionMessage.value = when {
            importedCount > 0 && summary.failedFiles.isEmpty() ->
                "$importedCount arquivo(s) interlinear(es) importado(s): $totalVerses versículos ($totalTokens palavras)."
            importedCount > 0 ->
                "$importedCount arquivo(s) importado(s) ($totalVerses vers.); ${summary.failedFiles.size} falhou(aram): ${summary.failedFiles.first().second}"
            summary.failedFiles.isNotEmpty() ->
                summary.failedFiles.first().second
            else -> "Nenhum arquivo selecionado."
        }
    }

    fun deleteInterlinearPack(packId: String) {
        viewModelScope.launch {
            stepInterlinearRepository.deletePack(packId)
            val state = _bibleState.value
            if (!state.isBookSelectorOpen) {
                openChapter(state.selectedBook.id, state.selectedChapter)
            }
            _actionMessage.value = "Arquivo interlinear removido."
        }
    }

    fun clearAllInterlinearPacks() {
        viewModelScope.launch {
            stepInterlinearRepository.clearAll()
            val state = _bibleState.value
            if (!state.isBookSelectorOpen) {
                openChapter(state.selectedBook.id, state.selectedChapter)
            }
            _actionMessage.value = "Todos os textos interlineares locais foram removidos."
        }
    }

    fun setReaderDisplayMode(mode: ReaderDisplayMode) {
        _bibleState.value = _bibleState.value.copy(displayMode = mode)
    }

    fun showBookSelector(show: Boolean) {
        _bibleState.value = _bibleState.value.copy(isBookSelectorOpen = show)
    }

    fun openChapterInterlinear(bookId: String, chapter: Int) {
        _bibleState.value = _bibleState.value.copy(displayMode = ReaderDisplayMode.INTERLINEAR)
        openChapter(bookId, chapter)
    }

    fun openChapter(bookId: String, chapter: Int) {
        val book = BibleCatalogData.allBooks.find { it.id == bookId } ?: return
        val safeChapter = chapter.coerceIn(1, book.chaptersCount)
        val cacheKey = "${book.id}:$safeChapter"
        val bundled = ScholarlyBibleVersesData.getVerses(book.id, safeChapter)
        val cachedFull = openBibleRepository.getCachedFullChapter(book.id, safeChapter)
        val aiCached = aiEnrichmentCache[cacheKey]

        val initialVerses = when {
            cachedFull != null -> mergeAiEnrichment(cachedFull, aiCached)
            bundled.isNotEmpty() -> bundled
            !aiCached.isNullOrEmpty() -> aiCached
            else -> emptyList()
        }

        _bibleState.value = _bibleState.value.copy(
            selectedBook = book,
            selectedChapter = safeChapter,
            selectedTestament = book.testament,
            isBookSelectorOpen = false,
            verses = initialVerses,
            chapterError = null,
            isLoadingChapter = initialVerses.isEmpty(),
            isSyncingFullChapter = cachedFull == null && initialVerses.isNotEmpty(),
            isEnrichingOriginals = false
        )

        if (cachedFull == null) {
            loadFullChapter(book, safeChapter, bundled)
        } else {
            viewModelScope.launch {
                refreshCurrentChapterStepInterlinear()
            }
        }
    }

    fun retryLoadCurrentChapter() {
        val state = _bibleState.value
        val bundled = ScholarlyBibleVersesData.getVerses(state.selectedBook.id, state.selectedChapter)
        loadFullChapter(state.selectedBook, state.selectedChapter, bundled)
    }

    private fun loadFullChapter(
        book: BibleBook,
        chapter: Int,
        bundled: List<BibleVerse>
    ) {
        chapterLoadJob?.cancel()
        chapterLoadJob = viewModelScope.launch {
            val cacheKey = "${book.id}:$chapter"
            val importedSnapshot = _bibleState.value.importedBibles
            val customByVersion = sqliteBibleRepository.readChapterForVersions(
                versions = importedSnapshot,
                bookId = book.id,
                chapter = chapter
            )
            val stepInterlinear = stepInterlinearRepository.readChapter(book.id, chapter)

            // Immediately show offline SQLite translations & offline STEPBible interlinear while online PT fetch runs
            if ((customByVersion.isNotEmpty() || stepInterlinear.isNotEmpty()) &&
                _bibleState.value.selectedBook.id == book.id &&
                _bibleState.value.selectedChapter == chapter
            ) {
                val base = _bibleState.value.verses.ifEmpty { bundled }
                val mergedCustom = mergeCustomTranslations(
                    bookId = book.id,
                    bookName = book.name,
                    chapter = chapter,
                    baseVerses = base,
                    customByVersion = customByVersion
                )
                val mergedOffline = mergeStepInterlinear(
                    bookId = book.id,
                    bookName = book.name,
                    chapter = chapter,
                    baseVerses = mergedCustom,
                    stepInterlinear = stepInterlinear
                )
                _bibleState.value = _bibleState.value.copy(
                    verses = mergedOffline,
                    isLoadingChapter = false,
                    chapterError = null
                )
            }

            val result = openBibleRepository.fetchFullChapter(
                bookId = book.id,
                bookName = book.name,
                chapter = chapter,
                bundledScholarlyVerses = bundled
            )
            result.onSuccess { fullVerses ->
                val latestImported = _bibleState.value.importedBibles
                val latestCustom = if (latestImported == importedSnapshot) {
                    customByVersion
                } else {
                    sqliteBibleRepository.readChapterForVersions(latestImported, book.id, chapter)
                }
                val latestStep = stepInterlinear.ifEmpty {
                    stepInterlinearRepository.readChapter(book.id, chapter)
                }
                val mergedWithAi = mergeAiEnrichment(fullVerses, aiEnrichmentCache[cacheKey])
                val mergedCustom = mergeCustomTranslations(
                    bookId = book.id,
                    bookName = book.name,
                    chapter = chapter,
                    baseVerses = mergedWithAi,
                    customByVersion = latestCustom
                )
                val mergedAll = mergeStepInterlinear(
                    bookId = book.id,
                    bookName = book.name,
                    chapter = chapter,
                    baseVerses = mergedCustom,
                    stepInterlinear = latestStep
                )
                if (_bibleState.value.selectedBook.id == book.id &&
                    _bibleState.value.selectedChapter == chapter
                ) {
                    _bibleState.value = _bibleState.value.copy(
                        verses = mergedAll,
                        isLoadingChapter = false,
                        isSyncingFullChapter = false,
                        chapterError = null
                    )
                }
            }.onFailure { error ->
                if (_bibleState.value.selectedBook.id == book.id &&
                    _bibleState.value.selectedChapter == chapter
                ) {
                    val hasExisting = _bibleState.value.verses.isNotEmpty()
                    _bibleState.value = _bibleState.value.copy(
                        isLoadingChapter = false,
                        isSyncingFullChapter = false,
                        chapterError = if (hasExisting) null else {
                            error.localizedMessage ?: "Não foi possível carregar este capítulo."
                        }
                    )
                }
            }
        }
    }

    private suspend fun refreshCurrentChapterStepInterlinear() {
        val state = _bibleState.value
        val book = state.selectedBook
        val chapter = state.selectedChapter
        val stepInterlinear = stepInterlinearRepository.readChapter(book.id, chapter)
        if (stepInterlinear.isEmpty()) return
        if (_bibleState.value.selectedBook.id == book.id &&
            _bibleState.value.selectedChapter == chapter
        ) {
            val updated = mergeStepInterlinear(
                bookId = book.id,
                bookName = book.name,
                chapter = chapter,
                baseVerses = _bibleState.value.verses,
                stepInterlinear = stepInterlinear
            )
            _bibleState.value = _bibleState.value.copy(
                verses = updated,
                isLoadingChapter = false,
                chapterError = null
            )
        }
    }

    private fun mergeStepInterlinear(
        bookId: String,
        bookName: String,
        chapter: Int,
        baseVerses: List<BibleVerse>,
        stepInterlinear: Map<Int, Pair<String, List<InterlinearToken>>>
    ): List<BibleVerse> {
        if (stepInterlinear.isEmpty()) return baseVerses
        val baseByVerse = baseVerses.associateBy { it.verse }
        val allVerseNumbers = sortedSetOf<Int>().apply {
            addAll(baseByVerse.keys)
            addAll(stepInterlinear.keys)
        }
        return allVerseNumbers.mapNotNull { verseNum ->
            val existing = baseByVerse[verseNum]
            val stepData = stepInterlinear[verseNum]
            when {
                existing != null && stepData != null -> {
                    existing.copy(
                        originalTextLine = stepData.first.ifBlank { existing.originalTextLine },
                        interlinear = stepData.second.ifEmpty { existing.interlinear }
                    )
                }
                existing != null -> existing
                stepData != null -> BibleVerse(
                    bookId = bookId,
                    bookName = bookName,
                    chapter = chapter,
                    verse = verseNum,
                    originalTextLine = stepData.first,
                    translations = emptyMap(),
                    interlinear = stepData.second,
                    commentary = ""
                )
                else -> null
            }
        }
    }

    private suspend fun refreshCurrentChapterCustomTranslations(imported: List<ImportedBibleVersion>) {
        val state = _bibleState.value
        val book = state.selectedBook
        val chapter = state.selectedChapter
        val customByVersion = sqliteBibleRepository.readChapterForVersions(imported, book.id, chapter)
        if (_bibleState.value.selectedBook.id == book.id &&
            _bibleState.value.selectedChapter == chapter
        ) {
            val updated = mergeCustomTranslations(
                bookId = book.id,
                bookName = book.name,
                chapter = chapter,
                baseVerses = _bibleState.value.verses,
                customByVersion = customByVersion
            )
            _bibleState.value = _bibleState.value.copy(verses = updated)
        }
    }

    private fun mergeCustomTranslations(
        bookId: String,
        bookName: String,
        chapter: Int,
        baseVerses: List<BibleVerse>,
        customByVersion: Map<String, Map<Int, String>>
    ): List<BibleVerse> {
        if (customByVersion.isEmpty() && baseVerses.none { it.customTranslations.isNotEmpty() }) {
            return baseVerses
        }
        val baseByVerse = baseVerses.associateBy { it.verse }
        val allVerseNumbers = sortedSetOf<Int>().apply {
            addAll(baseByVerse.keys)
            customByVersion.values.forEach { addAll(it.keys) }
        }
        return allVerseNumbers.mapNotNull { verseNum ->
            val existing = baseByVerse[verseNum]
            val customMap = buildMap {
                customByVersion.forEach { (versionId, verseMap) ->
                    val text = verseMap[verseNum]
                    if (!text.isNullOrBlank()) put(versionId, text)
                }
            }
            if (existing != null) {
                existing.copy(customTranslations = customMap)
            } else if (customMap.isNotEmpty()) {
                BibleVerse(
                    bookId = bookId,
                    bookName = bookName,
                    chapter = chapter,
                    verse = verseNum,
                    originalTextLine = "",
                    translations = emptyMap(),
                    customTranslations = customMap,
                    interlinear = emptyList(),
                    commentary = ""
                )
            } else {
                null
            }
        }
    }

    fun enrichCurrentChapterOriginalsWithAi() {
        val state = _bibleState.value
        val book = state.selectedBook
        val chapter = state.selectedChapter
        val cacheKey = "${book.id}:$chapter"
        val targetNumbers = state.verses
            .filter { it.originalTextLine.isBlank() }
            .take(3)
            .map { it.verse }
            .ifEmpty { listOf(1, 2, 3) }

        chapterEnrichJob?.cancel()
        chapterEnrichJob = viewModelScope.launch {
            _bibleState.value = _bibleState.value.copy(isEnrichingOriginals = true)
            val result = theoAiRepository.fetchChapterStudyOnDemand(
                bookId = book.id,
                bookName = book.name,
                chapter = chapter,
                isOldTestament = book.testament == Testament.OLD,
                targetVerseNumbers = targetNumbers
            )
            result.onSuccess { aiVerses ->
                val existingAi = aiEnrichmentCache[cacheKey].orEmpty()
                val combinedAi = (existingAi + aiVerses).associateBy { it.verse }.values.sortedBy { it.verse }
                aiEnrichmentCache[cacheKey] = combinedAi

                if (_bibleState.value.selectedBook.id == book.id &&
                    _bibleState.value.selectedChapter == chapter
                ) {
                    val updated = if (_bibleState.value.verses.isNotEmpty()) {
                        mergeAiEnrichment(_bibleState.value.verses, combinedAi)
                    } else {
                        combinedAi
                    }
                    _bibleState.value = _bibleState.value.copy(
                        verses = updated,
                        isEnrichingOriginals = false
                    )
                }
            }.onFailure { err ->
                if (_bibleState.value.selectedBook.id == book.id &&
                    _bibleState.value.selectedChapter == chapter
                ) {
                    _bibleState.value = _bibleState.value.copy(isEnrichingOriginals = false)
                    _actionMessage.value = err.localizedMessage ?: "Não foi possível gerar os originais agora."
                }
            }
        }
    }

    private fun mergeAiEnrichment(
        baseVerses: List<BibleVerse>,
        aiVerses: List<BibleVerse>?
    ): List<BibleVerse> {
        if (aiVerses.isNullOrEmpty()) return baseVerses
        val aiByVerse = aiVerses.associateBy { it.verse }
        return baseVerses.map { base ->
            val ai = aiByVerse[base.verse] ?: return@map base
            base.copy(
                originalTextLine = base.originalTextLine.ifBlank { ai.originalTextLine },
                interlinear = base.interlinear.ifEmpty { ai.interlinear },
                commentary = base.commentary.ifBlank { ai.commentary },
                textualNote = base.textualNote.ifBlank { ai.textualNote }
            )
        }
    }

    fun openOriginalWordStudy(rawWord: String, verse: BibleVerse) {
        val cleanWord = MorphologyDecoder.stripPunctuation(rawWord)
        if (cleanWord.isBlank()) return
        val bookIsOt = BibleCatalogData.allBooks
            .firstOrNull { it.id == verse.bookId }?.testament == Testament.OLD
        _selectedWordVerseLine.value = verse.originalTextLine
        _selectedWordIsOt.value = bookIsOt
        _wordModalAiState.value = AiStreamState.Idle

        val matchedToken = MorphologyDecoder.findMatchingToken(cleanWord, verse.interlinear)
            ?: ScholarlyLibraryData.findFallbackToken(cleanWord, bookIsOt)
        val verseRef = "${verse.bookName} ${verse.chapter}:${verse.verse}"
        if (matchedToken != null) {
            openLexiconForToken(
                token = matchedToken,
                clickedSurfaceWord = cleanWord,
                verseRef = verseRef
            )
        } else {
            val lang = if (bookIsOt) OriginalLanguage.HEBREW else OriginalLanguage.GREEK
            val fallbackEntry = LexiconEntry(
                strongId = if (bookIsOt) "H—" else "G—",
                language = lang,
                originalWord = cleanWord,
                transliteration = cleanWord,
                pronunciation = "Consulta contextual",
                shortGloss = "Palavra do texto original em $verseRef",
                etymology = "Forma extraída diretamente do texto original ($verseRef).",
                definition = "Este vocábulo ainda não possui entrada pré-indexada no catálogo local. A análise completa do léxico Strong, raiz e morfologia é gerada abaixo.",
                semanticDomains = listOf("Análise Contextual", lang.label),
                theologicalUsage = "Consulte abaixo a análise morfológica, raiz e definição Strong gerada em tempo real para esta ocorrência.",
                keyVerses = listOf(verseRef),
                occurrences = 1
            )
            val study = SelectedWordStudy(
                clickedWord = cleanWord,
                clickedTransliteration = "",
                contextualGloss = "Ocorrência em $verseRef",
                morphologyCode = "",
                lemma = cleanWord,
                verseRef = verseRef,
                lexiconEntry = fallbackEntry,
                isIndexedInStrongCatalog = false
            )
            _selectedLexiconToken.value = study
            analyzeSelectedWordWithAi(study)
        }
    }

    fun openLexiconForToken(
        token: InterlinearToken,
        clickedSurfaceWord: String = token.originalWord,
        verseRef: String = ""
    ) {
        _wordModalAiState.value = AiStreamState.Idle
        val catalogEntry = ScholarlyLibraryData.findStrongEntry(token.strongId)
        val isHebrew = token.strongId.startsWith("H", ignoreCase = true)
        val entry = catalogEntry ?: LexiconEntry(
            strongId = token.strongId,
            language = if (isHebrew) OriginalLanguage.HEBREW else OriginalLanguage.GREEK,
            originalWord = token.lemma.ifBlank { token.originalWord },
            transliteration = token.transliteration,
            pronunciation = token.transliteration,
            shortGloss = token.portugueseGloss,
            etymology = "Lema / raiz lexical: ${token.lemma.ifBlank { token.originalWord }} (${token.strongId}).",
            definition = "Forma no texto: ${token.originalWord} (${token.morphology}). Glosa contextual: ${token.portugueseGloss}.",
            semanticDomains = listOf("Análise Morfológica", token.morphology),
            theologicalUsage = "Toque em 'Aprofundar com IA' para análise léxico-teológica completa e concordância.",
            keyVerses = if (verseRef.isNotBlank()) listOf(verseRef) else emptyList(),
            occurrences = 1
        )
        _selectedLexiconToken.value = SelectedWordStudy(
            clickedWord = clickedSurfaceWord,
            clickedTransliteration = token.transliteration,
            contextualGloss = token.portugueseGloss,
            morphologyCode = token.morphology,
            lemma = token.lemma.ifBlank { entry.originalWord },
            verseRef = verseRef,
            lexiconEntry = entry,
            isIndexedInStrongCatalog = catalogEntry != null
        )
    }

    fun openLexiconByStrongId(
        strongId: String,
        morphologyCode: String = "",
        verseRef: String = ""
    ) {
        _wordModalAiState.value = AiStreamState.Idle
        val found = ScholarlyLibraryData.findStrongEntry(strongId) ?: return
        val resolvedMorph = morphologyCode.ifBlank {
            found.sampleOccurrences.firstOrNull()?.grammarForm
                ?.substringBefore(" (")
                ?.trim()
                .orEmpty()
        }
        val resolvedRef = verseRef.ifBlank {
            found.sampleOccurrences.firstOrNull()?.verseRef
                ?: found.keyVerses.firstOrNull().orEmpty()
        }
        _selectedLexiconToken.value = SelectedWordStudy(
            clickedWord = found.originalWord,
            clickedTransliteration = found.transliteration,
            contextualGloss = found.shortGloss,
            morphologyCode = resolvedMorph,
            lemma = found.originalWord,
            verseRef = resolvedRef,
            lexiconEntry = found,
            isIndexedInStrongCatalog = true
        )
    }

    fun analyzeSelectedWordWithAi(study: SelectedWordStudy) {
        wordAnalysisJob?.cancel()
        _wordModalAiState.value = AiStreamState.Loading
        wordAnalysisJob = viewModelScope.launch {
            var lastText = ""
            try {
                theoAiRepository.streamOriginalWordStrongAnalysis(
                    word = study.clickedWord,
                    verseRef = study.verseRef.ifBlank { "Escrituras" },
                    originalLine = _selectedWordVerseLine.value,
                    isOldTestament = study.lexiconEntry.language == OriginalLanguage.HEBREW || _selectedWordIsOt.value
                ).collect { partial ->
                    lastText = partial
                    _wordModalAiState.value = AiStreamState.Streaming(partial)
                }
                if (lastText.isNotBlank()) {
                    _wordModalAiState.value = AiStreamState.Success(lastText)
                } else {
                    _wordModalAiState.value = AiStreamState.Error("O modelo retornou uma resposta vazia.")
                }
            } catch (e: TimeoutCancellationException) {
                if (lastText.isNotBlank()) {
                    _wordModalAiState.value = AiStreamState.Success(
                        "$lastText\n\n_(Limite de 60s atingido; análise parcial exibida.)_"
                    )
                } else {
                    Log.e(TAG, "Original word Strong analysis timed out", e)
                    _wordModalAiState.value = AiStreamState.Error("Tempo limite excedido ao consultar Strong/Morfologia.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Original word Strong analysis failed", e)
                _wordModalAiState.value = AiStreamState.Error(
                    e.localizedMessage ?: "Erro ao consultar análise Strong."
                )
            }
        }
    }

    fun dismissLexiconDialog() {
        wordAnalysisJob?.cancel()
        _selectedLexiconToken.value = null
        _wordModalAiState.value = AiStreamState.Idle
    }

    fun sendTheoPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return

        _lastTheoPrompt.value = trimmed
        val lastMsg = _theoMessages.value.lastOrNull()
        if (lastMsg == null || !lastMsg.isFromUser || lastMsg.text != trimmed) {
            val userMsg = TheoChatMessage(
                id = "u_${System.currentTimeMillis()}",
                isFromUser = true,
                text = trimmed
            )
            _theoMessages.value = _theoMessages.value + userMsg
        }
        executeTheoStream(trimmed)
    }

    fun retryLastTheoPrompt() {
        val prompt = _lastTheoPrompt.value
            ?: _theoMessages.value.lastOrNull { it.isFromUser }?.text
            ?: return
        executeTheoStream(prompt)
    }

    private fun executeTheoStream(trimmed: String) {
        theoJob?.cancel()
        _theoStreamState.value = AiStreamState.Loading

        theoJob = viewModelScope.launch {
            var finalAnswer = ""
            try {
                theoAiRepository.streamTheologicalResponse(trimmed).collect { chunked ->
                    finalAnswer = chunked
                    _theoStreamState.value = AiStreamState.Streaming(chunked)
                }
                if (finalAnswer.isNotBlank()) {
                    _theoMessages.value = _theoMessages.value + TheoChatMessage(
                        id = "m_${System.currentTimeMillis()}",
                        isFromUser = false,
                        text = finalAnswer
                    )
                    _theoStreamState.value = AiStreamState.Success(finalAnswer)
                } else {
                    _theoStreamState.value = AiStreamState.Error("O modelo retornou uma resposta vazia.")
                }
            } catch (e: TimeoutCancellationException) {
                if (finalAnswer.isNotBlank()) {
                    val preserved = "$finalAnswer\n\n_(Limite de tempo atingido; estudo parcial preservado.)_"
                    _theoMessages.value = _theoMessages.value + TheoChatMessage(
                        id = "m_${System.currentTimeMillis()}",
                        isFromUser = false,
                        text = preserved
                    )
                    _theoStreamState.value = AiStreamState.Success(preserved)
                } else {
                    Log.e(TAG, "Exegesis study timed out", e)
                    _theoStreamState.value = AiStreamState.Error(
                        "A consulta exegética demorou além do esperado. Verifique a conexão e toque em Tentar novamente."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Exegesis study error", e)
                _theoStreamState.value = AiStreamState.Error(
                    e.localizedMessage ?: "Erro ao consultar bancada exegética."
                )
            }
        }
    }

    fun streamLibraryQuery(query: String, modeLabel: String) {
        val prompt = """
            Você é uma obra de referência acadêmica de exegese bíblica ($modeLabel).
            Apresente uma análise aprofundada em português sobre: "$query".
            Inclua: lema original (Grego Koiné/Hebraico Bíblico), morfologia/etimologia, ocorrências nos testamentos, contexto histórico-gramatical e referências principais.
        """.trimIndent()

        libraryJob?.cancel()
        _libraryStreamState.value = AiStreamState.Loading
        libraryJob = viewModelScope.launch {
            var lastText = ""
            try {
                theoAiRepository.streamTheologicalResponse(prompt).collect { partial ->
                    lastText = partial
                    _libraryStreamState.value = AiStreamState.Streaming(partial)
                }
                if (lastText.isNotBlank()) {
                    _libraryStreamState.value = AiStreamState.Success(lastText)
                } else {
                    _libraryStreamState.value = AiStreamState.Error("O modelo retornou uma resposta vazia.")
                }
            } catch (e: TimeoutCancellationException) {
                if (lastText.isNotBlank()) {
                    _libraryStreamState.value = AiStreamState.Success(
                        "$lastText\n\n_(Limite de 60s atingido; consulta parcial exibida.)_"
                    )
                } else {
                    Log.e(TAG, "Library AI query timed out", e)
                    _libraryStreamState.value = AiStreamState.Error("Tempo limite excedido na consulta.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Library AI query failed", e)
                _libraryStreamState.value = AiStreamState.Error(
                    e.localizedMessage ?: "Erro ao consultar biblioteca exegética."
                )
            }
        }
    }

    fun analyzeWordOccurrencesWithAi(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        occurrenceJob?.cancel()
        _occurrenceStreamState.value = AiStreamState.Loading
        occurrenceJob = viewModelScope.launch {
            var lastText = ""
            try {
                theoAiRepository.streamWordOccurrencesAnalysis(trimmed).collect { partial ->
                    lastText = partial
                    _occurrenceStreamState.value = AiStreamState.Streaming(partial)
                }
                if (lastText.isNotBlank()) {
                    _occurrenceStreamState.value = AiStreamState.Success(lastText)
                } else {
                    _occurrenceStreamState.value = AiStreamState.Error("O modelo retornou uma resposta vazia.")
                }
            } catch (e: TimeoutCancellationException) {
                if (lastText.isNotBlank()) {
                    _occurrenceStreamState.value = AiStreamState.Success(
                        "$lastText\n\n_(Limite de 60s atingido; concordância parcial exibida.)_"
                    )
                } else {
                    Log.e(TAG, "Word occurrences AI timed out", e)
                    _occurrenceStreamState.value = AiStreamState.Error("Tempo limite excedido na concordância.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Word occurrences AI failed", e)
                _occurrenceStreamState.value = AiStreamState.Error(
                    e.localizedMessage ?: "Erro ao consultar ocorrências."
                )
            }
        }
    }

    fun addNote(verseRef: String, title: String, content: String, tag: String?) {
        viewModelScope.launch {
            val result = userStudyRepository.addNote(currentUserId, verseRef, title, content, tag)
            result.onSuccess {
                _actionMessage.value = "Nota exegética salva com sucesso."
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Falha ao salvar nota."
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            val result = userStudyRepository.deleteNote(currentUserId, noteId)
            result.onSuccess {
                _actionMessage.value = "Nota removida."
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Falha ao remover nota."
            }
        }
    }

    fun togglePlanDayCompletion(plan: ReadingPlan, dayNumber: Long) {
        val currentProfile = (profileState.value as? UiState.Success)?.data
        viewModelScope.launch {
            val result = userStudyRepository.togglePlanDayCompletion(
                userId = currentUserId,
                plan = plan,
                dayNumber = dayNumber,
                currentProfile = currentProfile
            )
            result.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Falha ao atualizar progresso."
            }
        }
    }

    fun generatePersonalizedPlan(focusTopic: String, days: Int, pace: String) {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val aiResult = theoAiRepository.generateCustomReadingPlan(focusTopic, days, pace)
            aiResult.onSuccess { payload ->
                val existingPlans = (plansState.value as? UiState.Success)?.data.orEmpty()
                val activeIds = existingPlans.filter { it.active }.map { it.id }
                val saveResult = userStudyRepository.createReadingPlan(
                    userId = currentUserId,
                    title = payload.title,
                    description = payload.description,
                    readings = payload.readings,
                    existingPlanIdsToDeactivate = activeIds
                )
                saveResult.onSuccess {
                    _actionMessage.value = "Plano '${payload.title}' criado e ativado!"
                }.onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "Erro ao salvar plano."
                }
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Erro ao gerar plano personalizado."
            }
            _isGeneratingPlan.value = false
        }
    }

    fun saveNotificationSettings(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            val result = userStudyRepository.updateNotificationSettings(
                userId = currentUserId,
                enabled = enabled,
                hour = hour,
                minute = minute
            )
            result.onSuccess {
                _actionMessage.value = if (enabled) {
                    String.format("Lembrete diário agendado para %02d:%02d.", hour, minute)
                } else {
                    "Lembretes diários desativados."
                }
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Erro ao salvar notificações."
            }
        }
    }

    companion object {
        private const val TAG = "VerboViewModel"
        private const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
