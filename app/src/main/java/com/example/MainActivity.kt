package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.IntentCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.LocalStudyDatabase
import com.example.data.OpenBibleRepository
import com.example.data.SqliteBibleRepository
import com.example.data.StepInterlinearRepository
import com.example.data.TheoAiRepository
import com.example.data.ThemePreferencesRepository
import com.example.data.UserStudyRepository
import com.example.firebase.configureAppCheck
import com.example.model.UiState
import com.example.notifications.StudyNotificationHelper
import com.example.ui.components.AddNoteDialog
import com.example.ui.components.ConfirmSqliteImportDialog
import com.example.ui.components.LexiconDetailBottomSheet
import com.example.ui.components.ProfileAndSettingsSheet
import com.example.ui.screens.BibleScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LibrarySubTab
import com.example.ui.screens.NotesAndPlanScreen
import com.example.ui.screens.NotesPlanSubTab
import com.example.ui.screens.TheoScreen
import com.example.ui.theme.VerboTheme
import com.example.ui.viewmodel.VerboViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val ROUTE_HOME = "home"
private const val ROUTE_BIBLE = "bible"
private const val ROUTE_LIBRARY = "library"
private const val ROUTE_THEO = "exegesis_ai"
private const val ROUTE_NOTES = "notes"
private const val LOCAL_SCHOLAR_ID = "local_scholar"
private const val LOCAL_SCHOLAR_NAME = "Estudante"

private data class BereaNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomDestinations = listOf(
    BereaNavDestination(ROUTE_HOME, "Estudo", Icons.Filled.Home, Icons.Outlined.Home),
    BereaNavDestination(ROUTE_BIBLE, "Texto", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    BereaNavDestination(
        ROUTE_LIBRARY,
        "Léxico",
        Icons.AutoMirrored.Filled.LibraryBooks,
        Icons.AutoMirrored.Outlined.LibraryBooks
    ),
    BereaNavDestination(ROUTE_THEO, "Exegese", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    BereaNavDestination(ROUTE_NOTES, "Plano", Icons.Filled.Description, Icons.Outlined.Description)
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check for Firebase AI Logic safely
        runCatching {
            configureAppCheck(this, intent)
        }.onFailure { err ->
            Log.e("MainActivity", "Failed configuring Firebase App Check", err)
        }

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current.applicationContext
            val themePrefs = remember(context) { ThemePreferencesRepository(context) }
            val isDarkMode by themePrefs.isDarkMode.collectAsStateWithLifecycle(initialValue = false)

            VerboTheme(darkTheme = isDarkMode) {
                AppContent(
                    themePrefs = themePrefs,
                    isDarkMode = isDarkMode
                )
            }
        }
    }
}

@Composable
private fun AppContent(
    themePrefs: ThemePreferencesRepository,
    isDarkMode: Boolean
) {
    val context = LocalContext.current.applicationContext
    val viewModel: VerboViewModel = viewModel(
        key = LOCAL_SCHOLAR_ID,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val userStudyRepo = runCatching { UserStudyRepository(context) }.getOrElse {
                    Log.e("MainActivity", "UserStudyRepo init fallback", it)
                    UserStudyRepository(LocalStudyDatabase.getInstance(context).studyDao())
                }
                val theoRepo = runCatching { TheoAiRepository() }.getOrElse {
                    Log.e("MainActivity", "TheoAiRepo init fallback", it)
                    TheoAiRepository()
                }
                val openRepo = runCatching { OpenBibleRepository(context) }.getOrElse {
                    Log.e("MainActivity", "OpenBibleRepo init fallback", it)
                    OpenBibleRepository(context)
                }
                val sqliteRepo = runCatching { SqliteBibleRepository(context) }.getOrElse {
                    Log.e("MainActivity", "SqliteBibleRepo init fallback", it)
                    SqliteBibleRepository(context)
                }
                val stepRepo = runCatching { StepInterlinearRepository(context) }.getOrElse {
                    Log.e("MainActivity", "StepInterlinearRepo init fallback", it)
                    StepInterlinearRepository(context)
                }

                return VerboViewModel(
                    userStudyRepository = userStudyRepo,
                    theoAiRepository = theoRepo,
                    themePreferencesRepository = themePrefs,
                    currentUserId = LOCAL_SCHOLAR_ID,
                    userDisplayName = LOCAL_SCHOLAR_NAME,
                    openBibleRepository = openRepo,
                    sqliteBibleRepository = sqliteRepo,
                    stepInterlinearRepository = stepRepo
                ) as T
            }
        }
    )
    BereaApp(
        viewModel = viewModel,
        userName = LOCAL_SCHOLAR_NAME,
        isDarkMode = isDarkMode
    )
}

@Composable
private fun BereaApp(
    viewModel: VerboViewModel,
    userName: String,
    isDarkMode: Boolean
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_HOME

    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()

    var showProfileSheet by remember { mutableStateOf(false) }
    var noteDialogInitialVerse by remember { mutableStateOf<String?>(null) }
    var notesSubTab by remember { mutableStateOf(NotesPlanSubTab.PLAN) }
    var librarySubTab by remember { mutableStateOf(LibrarySubTab.OCCURRENCES) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    val navigateToTab: (String) -> Unit = remember(navController) {
        { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val initials = remember(userName) {
        userName.trim().split(" ").firstOrNull().orEmpty().take(2).uppercase().ifBlank { "BE" }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 720.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        bottomDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        bottomDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_HOME
                    ) {
                        composable(ROUTE_HOME) {
                            val profileState by viewModel.profileState.collectAsStateWithLifecycle()
                            val plansState by viewModel.plansState.collectAsStateWithLifecycle()
                            HomeScreen(
                                dailyStudy = viewModel.dailyStudy,
                                userName = userName,
                                profileState = profileState,
                                plansState = plansState,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = viewModel::toggleDarkMode,
                                onOpenProfileSheet = { showProfileSheet = true },
                                onStudyWithTheo = { study ->
                                    viewModel.sendTheoPrompt(
                                        "Faça uma análise exegética histórico-gramatical de ${study.verseRef} (${study.verseText}), " +
                                            "analisando o original ${study.originalWord} (${study.transliteration}, ${study.morphology}, ${study.strongId}), " +
                                            "suas ocorrências bíblicas e a doutrina de ${study.doctrineTitle}."
                                    )
                                    navigateToTab(ROUTE_THEO)
                                },
                                onOpenDailyWordLexicon = { strongId, morphology, verseRef ->
                                    viewModel.openLexiconByStrongId(strongId, morphology, verseRef)
                                },
                                onOpenPassageInBible = { bookId, chapter ->
                                    viewModel.openChapterInterlinear(bookId, chapter)
                                    navigateToTab(ROUTE_BIBLE)
                                },
                                onOpenWorkbenchOccurrences = {
                                    librarySubTab = LibrarySubTab.OCCURRENCES
                                    navigateToTab(ROUTE_LIBRARY)
                                },
                                onOpenWorkbenchLexicon = {
                                    librarySubTab = LibrarySubTab.LEXICON
                                    navigateToTab(ROUTE_LIBRARY)
                                },
                                onOpenExegesisAiTab = {
                                    navigateToTab(ROUTE_THEO)
                                },
                                onCompleteTodayPlanDay = { plan, day ->
                                    viewModel.togglePlanDayCompletion(plan, day)
                                },
                                onOpenPlanManager = {
                                    notesSubTab = NotesPlanSubTab.PLAN
                                    navigateToTab(ROUTE_NOTES)
                                }
                            )
                        }

                        composable(ROUTE_BIBLE) {
                            val bibleState by viewModel.bibleState.collectAsStateWithLifecycle()
                            val isImportingSqlite by viewModel.isImportingSqlite.collectAsStateWithLifecycle()
                            val pickerInterlinearChapters by viewModel.pickerInterlinearChapters.collectAsStateWithLifecycle()
                            BibleScreen(
                                bibleState = bibleState,
                                userInitials = initials,
                                isImportingSqlite = isImportingSqlite,
                                pickerInterlinearChapters = pickerInterlinearChapters,
                                onSelectTestament = viewModel::selectTestament,
                                onUpdateSearchQuery = viewModel::updateBibleSearchQuery,
                                onSelectTranslation = viewModel::setTranslation,
                                onSelectImportedBible = viewModel::selectImportedBible,
                                onImportSqliteUri = viewModel::inspectSqliteBibleUri,
                                onDeleteImportedBible = viewModel::deleteImportedBible,
                                onImportInterlinearUris = viewModel::importInterlinearFiles,
                                onDeleteInterlinearPack = viewModel::deleteInterlinearPack,
                                onClearInterlinearPacks = viewModel::clearAllInterlinearPacks,
                                onLoadBookInterlinearChapters = viewModel::loadInterlinearChaptersForBook,
                                onSelectDisplayMode = viewModel::setReaderDisplayMode,
                                onShowBookSelector = viewModel::showBookSelector,
                                onOpenChapter = viewModel::openChapter,
                                onRetryChapter = viewModel::retryLoadCurrentChapter,
                                onEnrichChapterOriginals = viewModel::enrichCurrentChapterOriginalsWithAi,
                                onOriginalWordClick = viewModel::openOriginalWordStudy,
                                onTokenClick = { token, verseRef ->
                                    viewModel.openLexiconForToken(token, verseRef = verseRef)
                                },
                                onOpenProfileSheet = { showProfileSheet = true },
                                onAddNoteForVerse = { verseRef ->
                                    noteDialogInitialVerse = verseRef
                                },
                                onAskTheoAboutVerse = { verse ->
                                    val customText = bibleState.selectedCustomVersionId?.let {
                                        verse.customTranslations[it]
                                    }
                                    val text = customText ?: verse.translations[bibleState.selectedTranslation].orEmpty()
                                    viewModel.sendTheoPrompt(
                                        "Faça a exegese de ${verse.bookName} ${verse.chapter}:${verse.verse} (\"$text\"), " +
                                            "analisando o texto original (${verse.originalTextLine}), morfologia, ocorrências dos termos-chave e aparato textual."
                                    )
                                    navigateToTab(ROUTE_THEO)
                                }
                            )
                        }

                        composable(ROUTE_LIBRARY) {
                            val libraryStreamState by viewModel.libraryStreamState.collectAsStateWithLifecycle()
                            val occurrenceStreamState by viewModel.occurrenceStreamState.collectAsStateWithLifecycle()
                            LibraryScreen(
                                initialSubTab = librarySubTab,
                                libraryStreamState = libraryStreamState,
                                occurrenceStreamState = occurrenceStreamState,
                                onLexiconEntryClick = { entry ->
                                    viewModel.openLexiconByStrongId(entry.strongId)
                                },
                                onJumpToPassage = { bookId, chapter ->
                                    viewModel.openChapterInterlinear(bookId, chapter)
                                    navigateToTab(ROUTE_BIBLE)
                                },
                                onQueryLibraryAi = viewModel::streamLibraryQuery,
                                onAnalyzeOccurrencesAi = viewModel::analyzeWordOccurrencesWithAi
                            )
                        }

                        composable(ROUTE_THEO) {
                            val theoMessages by viewModel.theoMessages.collectAsStateWithLifecycle()
                            val theoStreamState by viewModel.theoStreamState.collectAsStateWithLifecycle()
                            TheoScreen(
                                messages = theoMessages,
                                streamState = theoStreamState,
                                onSendPrompt = viewModel::sendTheoPrompt,
                                onRetryLastPrompt = viewModel::retryLastTheoPrompt
                            )
                        }

                        composable(ROUTE_NOTES) {
                            val profileState by viewModel.profileState.collectAsStateWithLifecycle()
                            val plansState by viewModel.plansState.collectAsStateWithLifecycle()
                            val notesState by viewModel.notesState.collectAsStateWithLifecycle()
                            val isGeneratingPlan by viewModel.isGeneratingPlan.collectAsStateWithLifecycle()
                            NotesAndPlanScreen(
                                initialSubTab = notesSubTab,
                                profileState = profileState,
                                plansState = plansState,
                                notesState = notesState,
                                isGeneratingPlan = isGeneratingPlan,
                                onTogglePlanDay = viewModel::togglePlanDayCompletion,
                                onGenerateCustomPlan = viewModel::generatePersonalizedPlan,
                                onSaveNotifications = viewModel::saveNotificationSettings,
                                onAddNoteClicked = { noteDialogInitialVerse = viewModel.dailyStudy.verseRef },
                                onDeleteNote = viewModel::deleteNote
                            )
                        }
                    }
                }
            }
        }
    }

    LexiconSheetHost(
        viewModel = viewModel,
        onAskTheo = { entry ->
            viewModel.dismissLexiconDialog()
            viewModel.sendTheoPrompt(
                "Analise em profundidade o vocábulo ${entry.language.label} ${entry.originalWord} " +
                    "(${entry.transliteration}, Strong ${entry.strongId}), suas ocorrências nos livros bíblicos e sua sintaxe."
            )
            navigateToTab(ROUTE_THEO)
        },
        onJumpToPassage = { bookId, chapter ->
            viewModel.dismissLexiconDialog()
            viewModel.openChapterInterlinear(bookId, chapter)
            navigateToTab(ROUTE_BIBLE)
        }
    )

    if (noteDialogInitialVerse != null) {
        AddNoteDialog(
            initialVerseRef = noteDialogInitialVerse!!,
            onSave = { verseRef, title, content, tag ->
                viewModel.addNote(verseRef, title, content, tag)
            },
            onDismiss = { noteDialogInitialVerse = null }
        )
    }

    if (showProfileSheet) {
        ProfileSheetHost(
            viewModel = viewModel,
            userName = userName,
            isDarkMode = isDarkMode,
            onDismiss = { showProfileSheet = false }
        )
    }

    val pendingSqliteImport by viewModel.pendingSqliteImport.collectAsStateWithLifecycle()
    pendingSqliteImport?.let { pending ->
        ConfirmSqliteImportDialog(
            pending = pending,
            onConfirm = viewModel::confirmSqliteBibleImport,
            onDismiss = viewModel::cancelPendingSqliteImport
        )
    }
}

@Composable
private fun LexiconSheetHost(
    viewModel: VerboViewModel,
    onAskTheo: (com.example.model.LexiconEntry) -> Unit,
    onJumpToPassage: (bookId: String, chapter: Int) -> Unit
) {
    val selectedLexiconToken by viewModel.selectedLexiconToken.collectAsStateWithLifecycle()
    val wordModalAiState by viewModel.wordModalAiState.collectAsStateWithLifecycle()
    if (selectedLexiconToken != null) {
        LexiconDetailBottomSheet(
            study = selectedLexiconToken!!,
            wordModalAiState = wordModalAiState,
            onAnalyzeWordAi = viewModel::analyzeSelectedWordWithAi,
            onAskTheo = onAskTheo,
            onJumpToPassage = onJumpToPassage,
            onDismiss = viewModel::dismissLexiconDialog
        )
    }
}

@Composable
private fun ProfileSheetHost(
    viewModel: VerboViewModel,
    userName: String,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val plansState by viewModel.plansState.collectAsStateWithLifecycle()
    val profile = (profileState as? UiState.Success)?.data
    val plans = (plansState as? UiState.Success)?.data.orEmpty()
    val activePlan = plans.firstOrNull { it.active } ?: plans.firstOrNull()

    ProfileAndSettingsSheet(
        userName = userName,
        isDarkMode = isDarkMode,
        profile = profile,
        onToggleDarkMode = viewModel::toggleDarkMode,
        onSaveNotifications = { enabled, hour, minute ->
            viewModel.saveNotificationSettings(enabled, hour, minute)
            if (enabled) {
                StudyNotificationHelper.scheduleDailyReminder(
                    context = context,
                    hour = hour,
                    minute = minute,
                    planTitle = activePlan?.title ?: "Plano Exegético",
                    completedCount = activePlan?.completedDays?.size ?: 0,
                    totalDays = (activePlan?.totalDays ?: 7L).toInt()
                )
            } else {
                StudyNotificationHelper.cancelDailyReminder(context)
            }
        },
        onTestNotification = {
            val done = activePlan?.completedDays?.size ?: 0
            val tot = activePlan?.totalDays ?: 7L
            StudyNotificationHelper.showProgressNotification(
                context = context,
                title = "Progresso no Plano Berea",
                body = "${activePlan?.title ?: "Plano diário"}: $done de $tot dias concluídos!"
            )
        },
        onDismiss = onDismiss
    )
}
