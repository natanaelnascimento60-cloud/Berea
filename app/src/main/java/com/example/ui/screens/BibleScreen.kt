package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BibleCatalogData
import com.example.data.ScholarlyBibleVersesData
import com.example.model.BibleBook
import com.example.model.BibleTranslation
import com.example.model.BibleVerse
import com.example.model.CanonicalDivision
import com.example.model.ImportedBibleVersion
import com.example.model.InterlinearToken
import com.example.model.Testament
import com.example.ui.components.InteractiveOriginalTextBlock
import com.example.ui.theme.BiblicalScriptFontFamily
import com.example.ui.theme.LocalBereaColors
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.MorphologyMonoFontFamily
import com.example.ui.viewmodel.BibleReaderState
import com.example.ui.viewmodel.ReaderDisplayMode

@Composable
fun BibleScreen(
    bibleState: BibleReaderState,
    userInitials: String,
    isImportingSqlite: Boolean = false,
    pickerInterlinearChapters: Set<Int> = emptySet(),
    onSelectTestament: (Testament) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectTranslation: (BibleTranslation) -> Unit,
    onSelectImportedBible: (ImportedBibleVersion) -> Unit = {},
    onImportSqliteUri: (Uri) -> Unit = {},
    onDeleteImportedBible: (ImportedBibleVersion) -> Unit = {},
    onImportInterlinearUris: (List<Uri>) -> Unit = {},
    onDeleteInterlinearPack: (String) -> Unit = {},
    onClearInterlinearPacks: () -> Unit = {},
    onLoadBookInterlinearChapters: (String) -> Unit = {},
    onSelectDisplayMode: (ReaderDisplayMode) -> Unit,
    onShowBookSelector: (Boolean) -> Unit,
    onOpenChapter: (bookId: String, chapter: Int) -> Unit,
    onRetryChapter: () -> Unit,
    onEnrichChapterOriginals: () -> Unit,
    onOriginalWordClick: (rawWord: String, verse: BibleVerse) -> Unit,
    onTokenClick: (InterlinearToken, String) -> Unit,
    onOpenProfileSheet: () -> Unit,
    onAddNoteForVerse: (String) -> Unit,
    onAskTheoAboutVerse: (BibleVerse) -> Unit
) {
    var bookForChapterSheet by remember { mutableStateOf<BibleBook?>(null) }
    var selectedVerseForSheet by remember { mutableStateOf<BibleVerse?>(null) }
    var showInterlinearManagerSheet by remember { mutableStateOf(false) }

    if (!bibleState.isBookSelectorOpen) {
        BackHandler {
            onShowBookSelector(true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
        ) {
            if (bibleState.isBookSelectorOpen) {
                CanonicalDivisionBookBrowser(
                    bibleState = bibleState,
                    userInitials = userInitials,
                    isImportingSqlite = isImportingSqlite,
                    onOpenProfileSheet = onOpenProfileSheet,
                    onSelectTranslation = onSelectTranslation,
                    onSelectImportedBible = onSelectImportedBible,
                    onImportSqliteUri = onImportSqliteUri,
                    onDeleteImportedBible = onDeleteImportedBible,
                    onOpenInterlinearManager = { showInterlinearManagerSheet = true },
                    onSelectTestament = onSelectTestament,
                    onUpdateSearchQuery = onUpdateSearchQuery,
                    onBookClicked = { book ->
                        onLoadBookInterlinearChapters(book.id)
                        bookForChapterSheet = book
                    },
                    onQuickOpenChapter = { bookId, chapter ->
                        onSelectDisplayMode(ReaderDisplayMode.INTERLINEAR)
                        onOpenChapter(bookId, chapter)
                    },
                    onQuickOpenVerse = { verse ->
                        onSelectDisplayMode(ReaderDisplayMode.INTERLINEAR)
                        onOpenChapter(verse.bookId, verse.chapter)
                    }
                )
            } else {
                ExegesisChapterReaderView(
                    bibleState = bibleState,
                    isImportingSqlite = isImportingSqlite,
                    onBackToBooks = { onShowBookSelector(true) },
                    onSelectTranslation = onSelectTranslation,
                    onSelectImportedBible = onSelectImportedBible,
                    onImportSqliteUri = onImportSqliteUri,
                    onDeleteImportedBible = onDeleteImportedBible,
                    onOpenInterlinearManager = { showInterlinearManagerSheet = true },
                    onSelectDisplayMode = onSelectDisplayMode,
                    onOpenChapterSelector = {
                        onLoadBookInterlinearChapters(bibleState.selectedBook.id)
                        bookForChapterSheet = bibleState.selectedBook
                    },
                    onRetryChapter = onRetryChapter,
                    onEnrichChapterOriginals = onEnrichChapterOriginals,
                    onOriginalWordClick = onOriginalWordClick,
                    onTokenClick = onTokenClick,
                    onVerseClick = { selectedVerseForSheet = it }
                )
            }
        }
    }

    if (showInterlinearManagerSheet) {
        StepInterlinearManagerSheet(
            bibleState = bibleState,
            onImportUris = onImportInterlinearUris,
            onDeletePack = onDeleteInterlinearPack,
            onClearAll = onClearInterlinearPacks,
            onDismiss = { showInterlinearManagerSheet = false }
        )
    }

    if (bookForChapterSheet != null) {
        val targetBook = bookForChapterSheet!!
        ChapterPickerSheet(
            book = targetBook,
            importedInterlinearChapters = pickerInterlinearChapters,
            onSelectChapter = { chapter ->
                bookForChapterSheet = null
                onOpenChapter(targetBook.id, chapter)
            },
            onDismiss = { bookForChapterSheet = null }
        )
    }

    if (selectedVerseForSheet != null) {
        val currentVerse = selectedVerseForSheet!!
        val currentVerseRef = "${currentVerse.bookName} ${currentVerse.chapter}:${currentVerse.verse}"
        VerseStudyBottomSheet(
            verse = currentVerse,
            importedBibles = bibleState.importedBibles,
            onAddNote = {
                selectedVerseForSheet = null
                onAddNoteForVerse(currentVerseRef)
            },
            onAskTheo = {
                selectedVerseForSheet = null
                onAskTheoAboutVerse(currentVerse)
            },
            onTokenClick = { token ->
                selectedVerseForSheet = null
                onTokenClick(token, currentVerseRef)
            },
            onDismiss = { selectedVerseForSheet = null }
        )
    }
}

@Composable
private fun CanonicalDivisionBookBrowser(
    bibleState: BibleReaderState,
    userInitials: String,
    isImportingSqlite: Boolean,
    onOpenProfileSheet: () -> Unit,
    onSelectTranslation: (BibleTranslation) -> Unit,
    onSelectImportedBible: (ImportedBibleVersion) -> Unit,
    onImportSqliteUri: (Uri) -> Unit,
    onDeleteImportedBible: (ImportedBibleVersion) -> Unit,
    onOpenInterlinearManager: () -> Unit,
    onSelectTestament: (Testament) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onBookClicked: (BibleBook) -> Unit,
    onQuickOpenChapter: (bookId: String, chapter: Int) -> Unit,
    onQuickOpenVerse: (BibleVerse) -> Unit
) {
    val extended = LocalBereaColors.current
    val query = bibleState.searchQuery.trim()
    val filteredBooks = remember(bibleState.selectedTestament, query) {
        BibleCatalogData.allBooks.filter { book ->
            val matchesTestament = if (query.isBlank()) book.testament == bibleState.selectedTestament else true
            val matchesQuery = query.isBlank() ||
                book.name.contains(query, ignoreCase = true) ||
                book.shortName.contains(query, ignoreCase = true) ||
                book.theme.contains(query, ignoreCase = true) ||
                book.author.contains(query, ignoreCase = true)
            matchesTestament && matchesQuery
        }
    }
    val groupedBooks = remember(filteredBooks) {
        filteredBooks.groupBy { it.division }
    }
    val matchingVerses = remember(query) {
        if (query.length >= 2) ScholarlyBibleVersesData.searchVerses(query).take(5) else emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onOpenProfileSheet() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userInitials,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Texto & Originais",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (bibleState.selectedTestament == Testament.OLD) "Texto Massorético (HEB)" else "Nestle-Aland / Grego Koiné",
                        fontFamily = MorphologyMonoFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            TranslationDropdownPill(
                bibleState = bibleState,
                isImportingSqlite = isImportingSqlite,
                onSelectBuiltIn = onSelectTranslation,
                onSelectImported = onSelectImportedBible,
                onImportSqliteUri = onImportSqliteUri,
                onDeleteImported = onDeleteImportedBible,
                onOpenInterlinearManager = onOpenInterlinearManager
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Testament Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Testament.entries.forEach { testament ->
                    val isSelected = bibleState.selectedTestament == testament
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.dp, extended.subtleBorder) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { onSelectTestament(testament) }
                            .testTag("tab_${testament.name.lowercase()}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = testament.labelPt,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = bibleState.searchQuery,
            onValueChange = onUpdateSearchQuery,
            placeholder = { Text("Buscar livro, tema, palavra original ou Strong (ex: λόγος, G313)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bible_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Canonical Division Structured Index (Original Berea Layout)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (query.isBlank()) {
                item(key = "quick_interlinear_bar") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = extended.folioHighlightBg,
                            border = BorderStroke(1.dp, extended.subtleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onQuickOpenChapter(
                                        bibleState.selectedBook.id,
                                        bibleState.selectedChapter
                                    )
                                }
                                .testTag("quick_resume_interlinear_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "LEITURA INTERLINEAR ATIVA",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${bibleState.selectedBook.name} ${bibleState.selectedChapter}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "Abrir Interlinear",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "PASSAGENS INTERLINEARES PRONTAS (${ScholarlyBibleVersesData.bundledPassages.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                ScholarlyBibleVersesData.bundledPassages,
                                key = { "${it.bookId}_${it.chapter}" }
                            ) { passage ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    border = BorderStroke(1.dp, extended.subtleBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            onQuickOpenChapter(passage.bookId, passage.chapter)
                                        }
                                        .testTag("quick_passage_${passage.bookId}_${passage.chapter}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = passage.label,
                                            fontFamily = MorphologyMonoFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = if (passage.isOldTestament) "HEB" else "GRK",
                                            fontFamily = MorphologyMonoFontFamily,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // STEPBible TAHOT / TAGNT Offline Interlinear Importer Card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, extended.subtleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenInterlinearManager() }
                                .testTag("step_interlinear_import_card")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileOpen,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "INTERLINEAR OFFLINE · STEPBIBLE (.TXT / .JSON)",
                                                fontFamily = MorphologyMonoFontFamily,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            val packs = bibleState.importedInterlinearPacks
                                            val summaryText = when {
                                                bibleState.isImportingInterlinear -> {
                                                    val prog = bibleState.interlinearImportProgress
                                                    if (prog != null) {
                                                        "Importando ${prog.currentFileIndex}/${prog.totalFiles}: ${prog.currentFileName} (${prog.parsedVersesCount} vers.)"
                                                    } else {
                                                        "Lendo arquivos STEPBible..."
                                                    }
                                                }
                                                packs.isEmpty() ->
                                                    "Importe arquivos TAHOT (AT) e TAGNT (NT) em .txt, .tsv ou .json"
                                                else -> {
                                                    val totalVerses = packs.sumOf { it.versesCount }
                                                    "${packs.size} arquivo(s) · ${bibleState.interlinearBookIds.size} livros · $totalVerses vers."
                                                }
                                            }
                                            Text(
                                                text = summaryText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = if (bibleState.importedInterlinearPacks.isEmpty()) "Importar" else "Gerenciar",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                if (bibleState.isImportingInterlinear) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }

            if (matchingVerses.isNotEmpty()) {
                item {
                    Text(
                        text = "OCORRÊNCIAS NO TEXTO BÍBLICO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(matchingVerses, key = { "hit_${it.bookId}_${it.chapter}_${it.verse}" }) { v ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = extended.folioHighlightBg,
                        border = BorderStroke(1.dp, extended.subtleBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQuickOpenVerse(v) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${v.bookName} ${v.chapter}:${v.verse}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (v.originalTextLine.isNotBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = v.originalTextLine,
                                    fontFamily = BiblicalScriptFontFamily,
                                    fontSize = 14.sp,
                                    color = extended.originalScriptAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = v.translations[bibleState.selectedTranslation].orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            CanonicalDivision.entries.forEach { division ->
                val booksInDivision = groupedBooks[division].orEmpty()
                if (booksInDivision.isNotEmpty()) {
                    item(key = "header_${division.name}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = division.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${booksInDivision.size} livros",
                                fontFamily = MorphologyMonoFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(booksInDivision, key = { it.id }) { book ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, extended.subtleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onBookClicked(book) }
                                .testTag("book_card_${book.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (book.testament == Testament.OLD) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = book.shortName,
                                            fontFamily = LoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (book.testament == Testament.OLD) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = book.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "· ${book.author}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = book.theme,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${book.chaptersCount} cap",
                                        fontFamily = MorphologyMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    val hasInterlinear = ScholarlyBibleVersesData.hasBundledBook(book.id) ||
                                        book.id in bibleState.interlinearBookIds
                                    if (hasInterlinear) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Surface(
                                            shape = RoundedCornerShape(5.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = "Interlinear",
                                                fontFamily = MorphologyMonoFontFamily,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExegesisChapterReaderView(
    bibleState: BibleReaderState,
    isImportingSqlite: Boolean,
    onBackToBooks: () -> Unit,
    onSelectTranslation: (BibleTranslation) -> Unit,
    onSelectImportedBible: (ImportedBibleVersion) -> Unit,
    onImportSqliteUri: (Uri) -> Unit,
    onDeleteImportedBible: (ImportedBibleVersion) -> Unit,
    onOpenInterlinearManager: () -> Unit,
    onSelectDisplayMode: (ReaderDisplayMode) -> Unit,
    onOpenChapterSelector: () -> Unit,
    onRetryChapter: () -> Unit,
    onEnrichChapterOriginals: () -> Unit,
    onOriginalWordClick: (rawWord: String, verse: BibleVerse) -> Unit,
    onTokenClick: (InterlinearToken, String) -> Unit,
    onVerseClick: (BibleVerse) -> Unit
) {
    val extended = LocalBereaColors.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Exegesis Reader Top Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackToBooks,
                            modifier = Modifier.testTag("reader_back_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar aos livros"
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOpenChapterSelector() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${bibleState.selectedBook.name} ${bibleState.selectedChapter}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Escolher capítulo",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    TranslationDropdownPill(
                        bibleState = bibleState,
                        isImportingSqlite = isImportingSqlite,
                        onSelectBuiltIn = onSelectTranslation,
                        onSelectImported = onSelectImportedBible,
                        onImportSqliteUri = onImportSqliteUri,
                        onDeleteImported = onDeleteImportedBible,
                        onOpenInterlinearManager = onOpenInterlinearManager
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4-Mode Exegesis Switcher: Texto | Interlinear | Paralelo | Comentário
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp)
                    ) {
                        ReaderDisplayMode.entries.forEach { mode ->
                            val isSelected = bibleState.displayMode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                border = if (isSelected) BorderStroke(1.dp, extended.subtleBorder) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectDisplayMode(mode) }
                                    .testTag("mode_${mode.name.lowercase()}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (bibleState.isSyncingFullChapter) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        when {
            bibleState.isLoadingChapter -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Carregando capítulo completo em português...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            bibleState.chapterError != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = bibleState.chapterError,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onRetryChapter) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = extended.folioHighlightBg,
                            border = BorderStroke(1.dp, extended.subtleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "${bibleState.selectedBook.name.uppercase()} · ${bibleState.selectedBook.author.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = bibleState.selectedBook.overview,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (bibleState.verses.any { it.originalTextLine.isBlank() }) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = onEnrichChapterOriginals,
                                        enabled = !bibleState.isEnrichingOriginals,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("enrich_chapter_originals_button")
                                    ) {
                                        if (bibleState.isEnrichingOriginals) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Carregando originais e morfologia...")
                                        } else {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Carregar Originais & Interlinear (IA)")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(bibleState.verses, key = { "${it.bookId}_${it.chapter}_${it.verse}" }) { verse ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, extended.subtleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verse_card_${verse.verse}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${verse.bookName} ${verse.chapter}:${verse.verse}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = extended.morphologyBg,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onVerseClick(verse) }
                                            .testTag("verse_notes_button_${verse.verse}")
                                    ) {
                                        Text(
                                            text = "Aparato & Notas",
                                            fontFamily = MorphologyMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                // Interactive Original Greek/Hebrew Line anchored at the top of every verse
                                if (verse.originalTextLine.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    InteractiveOriginalTextBlock(
                                        originalTextLine = verse.originalTextLine,
                                        interlinearTokens = verse.interlinear,
                                        isHebrew = bibleState.selectedBook.testament == Testament.OLD,
                                        verseNumber = verse.verse,
                                        onWordClick = { rawWord ->
                                            onOriginalWordClick(rawWord, verse)
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (bibleState.displayMode == ReaderDisplayMode.PARALLEL) {
                                    BibleTranslation.entries.forEach { tr ->
                                        val trText = verse.translations[tr]
                                        if (!trText.isNullOrBlank()) {
                                            Text(
                                                text = "${tr.code} · ${tr.fullName}",
                                                fontFamily = MorphologyMonoFontFamily,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = trText,
                                                fontFamily = LoraFontFamily,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                                            )
                                        }
                                    }
                                    bibleState.importedBibles.forEach { imported ->
                                        val customText = verse.customTranslations[imported.id]
                                        if (!customText.isNullOrBlank()) {
                                            Text(
                                                text = "${imported.code} · ${imported.fullName} (Local)",
                                                fontFamily = MorphologyMonoFontFamily,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = customText,
                                                fontFamily = LoraFontFamily,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                                            )
                                        }
                                    }
                                } else {
                                    val customId = bibleState.selectedCustomVersionId
                                    val mainText = if (customId != null) {
                                        verse.customTranslations[customId]
                                            ?: verse.translations[bibleState.selectedTranslation]
                                            ?: verse.translations.values.firstOrNull()
                                            ?: ""
                                    } else {
                                        verse.translations[bibleState.selectedTranslation]
                                            ?: verse.translations.values.firstOrNull()
                                            ?: verse.customTranslations.values.firstOrNull()
                                            ?: ""
                                    }
                                    if (mainText.isNotBlank()) {
                                        Text(
                                            text = mainText,
                                            fontFamily = LoraFontFamily,
                                            fontSize = 17.sp,
                                            lineHeight = 26.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    } else if (verse.interlinear.isNotEmpty()) {
                                        Text(
                                            text = "Modo interlinear offline (importação STEPBible local)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (bibleState.displayMode == ReaderDisplayMode.INTERLINEAR && verse.interlinear.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "MORFOLOGIA INTERLINEAR (TOQUE NO TERMO PARA LÉXICO & OCORRÊNCIAS)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = extended.originalScriptAccent
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val verseRef = remember(verse.bookName, verse.chapter, verse.verse) {
                                        "${verse.bookName} ${verse.chapter}:${verse.verse}"
                                    }
                                    val isHebrewChapter = bibleState.selectedBook.testament == Testament.OLD
                                    val tokenShape = remember { RoundedCornerShape(10.dp) }
                                    val morphTagShape = remember { RoundedCornerShape(4.dp) }
                                    CompositionLocalProvider(
                                        LocalLayoutDirection provides if (isHebrewChapter) LayoutDirection.Rtl else LayoutDirection.Ltr
                                    ) {
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            verse.interlinear.forEachIndexed { tokenIdx, token ->
                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    Column(
                                                        modifier = Modifier
                                                            .clip(tokenShape)
                                                            .background(extended.folioHighlightBg)
                                                            .border(1.dp, extended.subtleBorder, tokenShape)
                                                            .clickable { onTokenClick(token, verseRef) }
                                                            .padding(horizontal = 10.dp, vertical = 7.dp)
                                                            .testTag("interlinear_token_${token.strongId}_$tokenIdx"),
                                                        horizontalAlignment = Alignment.Start
                                                    ) {
                                                        Text(
                                                            text = token.originalWord,
                                                            fontFamily = BiblicalScriptFontFamily,
                                                            fontSize = 17.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (token.transliteration.isNotBlank()) {
                                                            Text(
                                                                text = token.transliteration,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = extended.originalScriptAccent,
                                                                fontSize = 11.sp
                                                            )
                                                        }
                                                        if (token.portugueseGloss.isNotBlank()) {
                                                            Text(
                                                                text = token.portugueseGloss,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.Medium,
                                                                fontSize = 12.sp
                                                            )
                                                        }
                                                        val tagLabel = listOf(token.strongId, token.morphology)
                                                            .filter { it.isNotBlank() }
                                                            .joinToString(" · ")
                                                        if (tagLabel.isNotBlank()) {
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = tagLabel,
                                                                fontFamily = MorphologyMonoFontFamily,
                                                                fontSize = 9.sp,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier
                                                                    .background(extended.morphologyBg, morphTagShape)
                                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (bibleState.displayMode == ReaderDisplayMode.COMMENTARY) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "COMENTÁRIO EXEGÉTICO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = verse.commentary.ifBlank {
                                            "Toque em 'Notas, Traduções & IA' abaixo para gerar o comentário exegético deste versículo."
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (verse.commentary.isBlank()) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (verse.textualNote.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = extended.morphologyBg,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Aparato Crítico: ${verse.textualNote}",
                                                fontFamily = MorphologyMonoFontFamily,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(9.dp)
                                            )
                                        }
                                    }
                                    if (verse.crossReferences.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Paralelos: ${verse.crossReferences.joinToString(" · ")}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { onVerseClick(verse) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.EditNote,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Notas, Traduções & IA", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslationDropdownPill(
    bibleState: BibleReaderState,
    isImportingSqlite: Boolean,
    onSelectBuiltIn: (BibleTranslation) -> Unit,
    onSelectImported: (ImportedBibleVersion) -> Unit,
    onImportSqliteUri: (Uri) -> Unit,
    onDeleteImported: (ImportedBibleVersion) -> Unit,
    onOpenInterlinearManager: () -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val extended = LocalBereaColors.current

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.subtleBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { showSheet = true }
            .testTag("translation_selector_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = bibleState.activeTranslationCode,
                fontFamily = MorphologyMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Selecionar tradução",
                modifier = Modifier.size(16.dp)
            )
        }
    }

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        var isClosing by remember { mutableStateOf(false) }

        fun closeSheet(afterHide: () -> Unit = {}) {
            if (isClosing) return
            isClosing = true
            scope.launch {
                try {
                    sheetState.hide()
                } finally {
                    showSheet = false
                    afterHide()
                }
            }
        }

        val sqlitePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                closeSheet {
                    onImportSqliteUri(uri)
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Versões Bíblicas",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Traduções abertas em português ou arquivos locais (.sqlite, .db, .json, .txt).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PORTUGUÊS (LICENÇA ABERTA)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                BibleTranslation.entries.forEach { tr ->
                    val isSelected = bibleState.selectedCustomVersionId == null && tr == bibleState.selectedTranslation
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            extended.folioHighlightBg
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else extended.subtleBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                closeSheet {
                                    onSelectBuiltIn(tr)
                                }
                            }
                            .testTag("translation_option_${tr.code.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${tr.code} — ${tr.fullName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tr.languageLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Versão selecionada",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "SUAS VERSÕES LOCAIS IMPORTADAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (bibleState.importedBibles.isEmpty()) {
                    Text(
                        text = "Nenhuma Bíblia local importada ainda. Aceita SQLite (.sqlite, .db, .SQLite3, .bbl.mybible), JSON bíblico ou TXT estruturado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                } else {
                    bibleState.importedBibles.forEach { imported ->
                        val isSelected = bibleState.selectedCustomVersionId == imported.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                extended.folioHighlightBg
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else extended.subtleBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    closeSheet {
                                        onSelectImported(imported)
                                    }
                                }
                                .testTag("imported_bible_${imported.code.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${imported.code} — ${imported.fullName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = imported.languageLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Versão selecionada",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteImported(imported) },
                                    modifier = Modifier.testTag("delete_imported_${imported.code.lowercase()}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remover versão ${imported.code}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        sqlitePickerLauncher.launch(arrayOf("*/*"))
                    },
                    enabled = !isImportingSqlite,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("import_sqlite_bible_button")
                ) {
                    if (isImportingSqlite) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importar Bíblia (.sqlite / .json / .txt)")
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TEXTO INTERLINEAR OFFLINE (STEPBIBLE)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (bibleState.importedInterlinearPacks.isEmpty()) {
                        "Carregue os arquivos TAHOT (4 partes AT) e TAGNT (2 partes NT) em .txt, .tsv ou .json."
                    } else {
                        "${bibleState.importedInterlinearPacks.size} arquivo(s) importado(s) · ${bibleState.interlinearBookIds.size} livros cobertos."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        closeSheet {
                            onOpenInterlinearManager()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_interlinear_manager_from_versions")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (bibleState.importedInterlinearPacks.isEmpty()) {
                            "Importar Interlinear (.txt / .tsv / .json)"
                        } else {
                            "Gerenciar Interlinear STEPBible (${bibleState.importedInterlinearPacks.size})"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepInterlinearManagerSheet(
    bibleState: BibleReaderState,
    onImportUris: (List<Uri>) -> Unit,
    onDeletePack: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val extended = LocalBereaColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val multiTxtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportUris(uris)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("step_interlinear_manager_sheet")
        ) {
            Text(
                text = "Interlinear STEPBible (TAHOT / TAGNT)",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Selecione um ou vários arquivos (.txt, .tsv ou .json) do STEPBible para leitura interlinear offline — incluindo as 4 partes do Antigo Testamento (TAHOT) e as 2 partes do Novo Testamento (TAGNT). Você também pode abrir ou compartilhar esses arquivos direto de outro app para o Berea.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { multiTxtLauncher.launch(arrayOf("*/*")) },
                enabled = !bibleState.isImportingInterlinear,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("select_step_txt_files_button")
            ) {
                if (bibleState.isImportingInterlinear) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (bibleState.isImportingInterlinear) {
                        "Importando arquivos..."
                    } else {
                        "Selecionar Arquivos (.txt / .tsv / .json)"
                    }
                )
            }

            if (bibleState.isImportingInterlinear) {
                val prog = bibleState.interlinearImportProgress
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = extended.folioHighlightBg,
                    border = BorderStroke(1.dp, extended.subtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (prog != null) {
                                "Arquivo ${prog.currentFileIndex} de ${prog.totalFiles}: ${prog.currentFileName}"
                            } else {
                                "Preparando importação..."
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        if (prog != null && prog.parsedVersesCount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${prog.parsedVersesCount} versículos · ${prog.parsedTokensCount} palavras processadas",
                                fontFamily = MorphologyMonoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ARQUIVOS IMPORTADOS (${bibleState.importedInterlinearPacks.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (bibleState.importedInterlinearPacks.size > 1 && !bibleState.isImportingInterlinear) {
                    TextButton(
                        onClick = onClearAll,
                        modifier = Modifier.testTag("clear_all_interlinear_packs_button")
                    ) {
                        Text("Limpar todos")
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (bibleState.importedInterlinearPacks.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = extended.folioHighlightBg,
                    border = BorderStroke(1.dp, extended.subtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nenhum arquivo interlinear importado ainda. Após importar (.txt, .tsv ou .json), todos os livros presentes aparecerão com o selo 'Interlinear' e ficarão disponíveis offline na aba Interlinear.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                bibleState.importedInterlinearPacks.forEach { pack ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = extended.folioHighlightBg,
                        border = BorderStroke(1.dp, extended.subtleBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pack.fileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${pack.corpusType} · ${pack.booksSummary}",
                                    fontFamily = MorphologyMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${pack.versesCount} versículos · ${pack.tokensCount} palavras",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onDeletePack(pack.id) },
                                enabled = !bibleState.isImportingInterlinear,
                                modifier = Modifier.testTag("delete_interlinear_pack_${pack.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remover ${pack.fileName}",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ChapterPickerSheet(
    book: BibleBook,
    importedInterlinearChapters: Set<Int> = emptySet(),
    onSelectChapter: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isClosing by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = book.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "${book.division.label} · ${book.author} · ${book.chaptersCount} capítulos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Todos os 66 livros disponíveis em português (Bíblia Livre, Bíblia Livre TR, Almeida 1911) e em suas Bíblias locais importadas (.sqlite, .json, .txt).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..book.chaptersCount).forEach { cap ->
                    val isBundled = (ScholarlyBibleVersesData.hasBundledBook(book.id) &&
                        ScholarlyBibleVersesData.hasBundledChapter(book.id, cap)) ||
                        cap in importedInterlinearChapters
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isBundled) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = if (isBundled) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            null
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                if (!isClosing) {
                                    isClosing = true
                                    scope.launch {
                                        try {
                                            sheetState.hide()
                                        } finally {
                                            onSelectChapter(cap)
                                        }
                                    }
                                }
                            }
                            .testTag("chapter_button_$cap")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = cap.toString(),
                                fontFamily = MorphologyMonoFontFamily,
                                fontWeight = if (isBundled) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (isBundled) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseStudyBottomSheet(
    verse: BibleVerse,
    importedBibles: List<ImportedBibleVersion>,
    onAddNote: () -> Unit,
    onAskTheo: () -> Unit,
    onTokenClick: (InterlinearToken) -> Unit,
    onDismiss: () -> Unit
) {
    val extended = LocalBereaColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isClosing by remember { mutableStateOf(false) }

    fun closeThen(action: () -> Unit) {
        if (isClosing) return
        isClosing = true
        scope.launch {
            try {
                sheetState.hide()
            } finally {
                action()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${verse.bookName} ${verse.chapter}:${verse.verse}",
                style = MaterialTheme.typography.headlineMedium
            )
            if (verse.originalTextLine.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = verse.originalTextLine,
                    fontFamily = BiblicalScriptFontFamily,
                    fontSize = 17.sp,
                    color = extended.originalScriptAccent
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "COMENTÁRIO EXEGÉTICO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = verse.commentary.ifBlank {
                    "Toque em 'Exegese IA' abaixo para gerar o comentário histórico-gramatical e o texto original deste versículo."
                },
                style = MaterialTheme.typography.bodyLarge
            )

            if (verse.textualNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = extended.morphologyBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aparato Crítico: ${verse.textualNote}",
                        fontFamily = MorphologyMonoFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            if (verse.crossReferences.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "REFERÊNCIAS CRUZADAS: ${verse.crossReferences.joinToString(" · ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "COMPARAÇÃO DE TRADUÇÕES (PORTUGUÊS)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            BibleTranslation.entries.forEach { tr ->
                val text = verse.translations[tr]
                if (!text.isNullOrBlank()) {
                    Text(
                        text = "${tr.code}: $text",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            importedBibles.forEach { imported ->
                val customText = verse.customTranslations[imported.id]
                if (!customText.isNullOrBlank()) {
                    Text(
                        text = "${imported.code} (SQLite): $customText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            if (verse.interlinear.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "LÉXICO & MORFOLOGIA DO VERSÍCULO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                verse.interlinear.take(6).forEach { token ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = extended.folioHighlightBg,
                        border = BorderStroke(1.dp, extended.subtleBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clickable { closeThen { onTokenClick(token) } }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${token.originalWord} (${token.transliteration})",
                                    fontFamily = BiblicalScriptFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = token.portugueseGloss,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${token.strongId} · ${token.morphology}",
                                fontFamily = MorphologyMonoFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { closeThen(onAddNote) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Anotar")
                }

                Button(
                    onClick = { closeThen(onAskTheo) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exegese IA")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
