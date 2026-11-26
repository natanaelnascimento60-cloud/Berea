package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScholarlyLibraryData
import com.example.model.AiStreamState
import com.example.model.LexiconEntry
import com.example.model.OriginalLanguage
import com.example.ui.theme.BiblicalScriptFontFamily
import com.example.ui.theme.LocalBereaColors
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.MorphologyMonoFontFamily

enum class LibrarySubTab(val label: String) {
    OCCURRENCES("Ocorrências"),
    LEXICON("Léxico G/H"),
    COMMENTARIES("Comentários"),
    DICTIONARY("Dicionário")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    initialSubTab: LibrarySubTab,
    libraryStreamState: AiStreamState,
    occurrenceStreamState: AiStreamState,
    onLexiconEntryClick: (LexiconEntry) -> Unit,
    onJumpToPassage: (bookId: String, chapter: Int) -> Unit,
    onQueryLibraryAi: (query: String, modeLabel: String) -> Unit,
    onAnalyzeOccurrencesAi: (query: String) -> Unit
) {
    var selectedTab by remember(initialSubTab) { mutableStateOf(initialSubTab) }
    LaunchedEffect(initialSubTab) {
        selectedTab = initialSubTab
    }

    var searchQuery by remember { mutableStateOf("") }
    var languageFilter by remember { mutableStateOf<OriginalLanguage?>(null) }
    val extended = LocalBereaColors.current

    val filteredLexicon = remember(searchQuery, languageFilter) {
        ScholarlyLibraryData.lexiconEntries.filter { entry ->
            val matchesLang = languageFilter == null || entry.language == languageFilter
            val q = searchQuery.trim()
            val matchesSearch = q.isBlank() ||
                entry.originalWord.contains(q, ignoreCase = true) ||
                entry.transliteration.contains(q, ignoreCase = true) ||
                entry.shortGloss.contains(q, ignoreCase = true) ||
                entry.strongId.contains(q, ignoreCase = true) ||
                entry.semanticDomains.any { it.contains(q, ignoreCase = true) }
            matchesLang && matchesSearch
        }
    }

    val filteredCommentaries = remember(searchQuery) {
        val q = searchQuery.trim()
        ScholarlyLibraryData.commentaries.filter {
            q.isBlank() ||
                it.bookName.contains(q, ignoreCase = true) ||
                it.passageRef.contains(q, ignoreCase = true) ||
                it.exegesis.contains(q, ignoreCase = true) ||
                it.literaryStructure.contains(q, ignoreCase = true)
        }
    }

    val filteredDictionary = remember(searchQuery) {
        val q = searchQuery.trim()
        ScholarlyLibraryData.dictionaryEntries.filter {
            q.isBlank() ||
                it.term.contains(q, ignoreCase = true) ||
                it.category.contains(q, ignoreCase = true) ||
                it.summary.contains(q, ignoreCase = true) ||
                it.originalTerm.contains(q, ignoreCase = true)
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
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Léxico & Concordância",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Ocorrências de palavras, léxicos Strong, comentários e dicionário",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4-Tab Bar: Ocorrências | Léxico G/H | Comentários | Dicionário
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
                    LibrarySubTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, extended.subtleBorder) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { selectedTab = tab }
                                .testTag("library_tab_${tab.name.lowercase()}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = tab.label,
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (selectedTab == LibrarySubTab.OCCURRENCES) {
                            "Buscar ocorrências por palavra, lema ou Strong (ex: λόγος, חֶסֶד, G3056)"
                        } else {
                            "Buscar termo Strong, passagem, aparato ou doutrina..."
                        }
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("library_search_input")
            )

            if (selectedTab == LibrarySubTab.LEXICON || selectedTab == LibrarySubTab.OCCURRENCES) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = languageFilter == null,
                        onClick = { languageFilter = null },
                        label = { Text("Todos") }
                    )
                    FilterChip(
                        selected = languageFilter == OriginalLanguage.GREEK,
                        onClick = { languageFilter = OriginalLanguage.GREEK },
                        label = { Text("Grego (NT)") }
                    )
                    FilterChip(
                        selected = languageFilter == OriginalLanguage.HEBREW,
                        onClick = { languageFilter = OriginalLanguage.HEBREW },
                        label = { Text("Hebraico (AT)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 28.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // On-demand Concordance / Library AI Lookup Card whenever user searches
                if (searchQuery.isNotBlank()) {
                    val activeStreamState = if (selectedTab == LibrarySubTab.OCCURRENCES) {
                        occurrenceStreamState
                    } else {
                        libraryStreamState
                    }
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedTab == LibrarySubTab.OCCURRENCES) {
                                            "Concordância IA: \"$searchQuery\""
                                        } else {
                                            "Pesquisa Exegética: \"$searchQuery\""
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (selectedTab == LibrarySubTab.OCCURRENCES) {
                                                onAnalyzeOccurrencesAi(searchQuery)
                                            } else {
                                                onQueryLibraryAi(searchQuery, selectedTab.label)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (selectedTab == LibrarySubTab.OCCURRENCES) "Mapear Ocorrências" else "Consultar IA")
                                    }
                                }

                                when (activeStreamState) {
                                    is AiStreamState.Loading -> {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    }
                                    is AiStreamState.Streaming -> {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(activeStreamState.text, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    is AiStreamState.Success -> {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(activeStreamState.text, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    is AiStreamState.Error -> {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            activeStreamState.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    else -> Unit
                                }
                            }
                        }
                    }
                }

                when (selectedTab) {
                    LibrarySubTab.OCCURRENCES -> {
                        items(filteredLexicon, key = { "occ_${it.strongId}" }) { entry ->
                            val maxCount = entry.bookDistribution.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, extended.subtleBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("occurrence_card_${entry.strongId}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = entry.originalWord,
                                                fontFamily = BiblicalScriptFontFamily,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = entry.transliteration,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = extended.originalScriptAccent
                                                )
                                                Text(
                                                    text = "${entry.strongId} · ${entry.shortGloss}",
                                                    fontFamily = MorphologyMonoFontFamily,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Analytics,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${entry.occurrences}x",
                                                    fontFamily = MorphologyMonoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }

                                    if (entry.bookDistribution.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "FREQUÊNCIA POR CORPUS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            entry.bookDistribution.forEach { stat ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = stat.bookName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = 12.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(0.45f)
                                                    )
                                                    LinearProgressIndicator(
                                                        progress = { (stat.count.toFloat() / maxCount.toFloat()).coerceIn(0.05f, 1f) },
                                                        modifier = Modifier
                                                            .weight(0.42f)
                                                            .height(6.dp)
                                                            .clip(RoundedCornerShape(100.dp)),
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        trackColor = extended.morphologyBg
                                                    )
                                                    Text(
                                                        text = "${stat.count}",
                                                        fontFamily = MorphologyMonoFontFamily,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.padding(start = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (entry.sampleOccurrences.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "PASSAGENS DE CONCORDÂNCIA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        entry.sampleOccurrences.forEach { hit ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = extended.folioHighlightBg,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 6.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { onJumpToPassage(hit.bookId, hit.chapter) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = hit.verseRef,
                                                                style = MaterialTheme.typography.labelLarge,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                            Text(
                                                                text = hit.grammarForm,
                                                                fontFamily = MorphologyMonoFontFamily,
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(3.dp))
                                                        Text(
                                                            text = hit.originalExcerpt,
                                                            fontFamily = BiblicalScriptFontFamily,
                                                            fontSize = 14.sp,
                                                            color = extended.originalScriptAccent
                                                        )
                                                        Text(
                                                            text = hit.portugueseText,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontSize = 12.sp,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = "Abrir passagem",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = { onLexiconEntryClick(entry) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Abrir Verbete Completo no Léxico")
                                    }
                                }
                            }
                        }
                    }

                    LibrarySubTab.LEXICON -> {
                        items(filteredLexicon, key = { it.strongId }) { entry ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, extended.subtleBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onLexiconEntryClick(entry) }
                                    .testTag("lexicon_card_${entry.strongId}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer
                                            ) {
                                                Text(
                                                    text = entry.strongId,
                                                    fontFamily = MorphologyMonoFontFamily,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = entry.language.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${entry.occurrences} ocorr.",
                                            fontFamily = MorphologyMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = entry.originalWord,
                                        fontFamily = BiblicalScriptFontFamily,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${entry.transliteration} /${entry.pronunciation}/ · ${entry.shortGloss}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = extended.originalScriptAccent
                                    )
                                    if (entry.etymology.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = entry.etymology,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (entry.semanticDomains.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            entry.semanticDomains.forEach { dom ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = extended.morphologyBg
                                                ) {
                                                    Text(
                                                        text = dom,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LibrarySubTab.COMMENTARIES -> {
                        items(filteredCommentaries, key = { it.id }) { comment ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, extended.subtleBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "${comment.passageRef.uppercase()} · ${comment.author.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Estrutura Literária",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = comment.literaryStructure,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Contexto Histórico",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = comment.historicalContext,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Análise Exegética",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = comment.exegesis,
                                        fontFamily = LoraFontFamily,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Síntese Teológica: ${comment.theologicalApplication}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    LibrarySubTab.DICTIONARY -> {
                        items(filteredDictionary, key = { it.id }) { entry ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, extended.subtleBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = entry.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = entry.term,
                                        fontFamily = LoraFontFamily,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    Text(
                                        text = entry.originalTerm,
                                        fontFamily = BiblicalScriptFontFamily,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = extended.originalScriptAccent
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = entry.article,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Referências: ${entry.relatedVerses.joinToString(" · ")}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
