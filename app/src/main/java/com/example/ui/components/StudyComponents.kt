package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.PendingSqliteBible
import com.example.data.ScholarlyLibraryData
import com.example.model.AiStreamState
import com.example.model.InterlinearToken
import com.example.model.LexiconEntry
import com.example.model.MorphologyDecoder
import com.example.model.SelectedWordStudy
import com.example.model.UserProfile
import com.example.ui.theme.BiblicalScriptFontFamily
import com.example.ui.theme.LocalBereaColors
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.MorphologyMonoFontFamily

@Composable
fun BereaWordmark(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "BEREA",
            fontFamily = LoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            letterSpacing = 1.6.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "EXEGESE",
            fontFamily = MorphologyMonoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 1.4.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private data class OriginalWordChipData(
    val rawWord: String,
    val isClickable: Boolean,
    val hasIndex: Boolean
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveOriginalTextBlock(
    originalTextLine: String,
    interlinearTokens: List<InterlinearToken>,
    isHebrew: Boolean,
    verseNumber: Int,
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LocalBereaColors.current
    val wordChips = remember(originalTextLine, interlinearTokens, isHebrew) {
        originalTextLine
            .trim()
            .split(Regex("\\s+"))
            .flatMap { segment ->
                if (segment.contains('־')) {
                    val parts = segment.split('־')
                    parts.mapIndexedNotNull { idx, p ->
                        if (p.isBlank()) null else if (idx < parts.lastIndex) "$p־" else p
                    }
                } else {
                    listOf(segment)
                }
            }
            .filter { it.isNotBlank() }
            .map { rawWord ->
                val clean = MorphologyDecoder.stripPunctuation(rawWord)
                val isClickable = clean.isNotBlank()
                val hasIndex = if (isClickable) {
                    MorphologyDecoder.findMatchingToken(clean, interlinearTokens) != null ||
                        ScholarlyLibraryData.findFallbackToken(clean, isHebrew) != null
                } else {
                    false
                }
                OriginalWordChipData(
                    rawWord = rawWord,
                    isClickable = isClickable,
                    hasIndex = hasIndex
                )
            }
    }

    val chipShape = remember { RoundedCornerShape(8.dp) }
    val indexedBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
    val defaultBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
    val indexedBorder = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
    val defaultBorder = extended.subtleBorder.copy(alpha = 0.6f)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = extended.folioHighlightBg,
        border = BorderStroke(1.dp, extended.subtleBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHebrew) "TEXTO MASSORÉTICO (HEB)" else "GREGO KOINÉ (NA28)",
                    fontFamily = MorphologyMonoFontFamily,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Toque na palavra · morfologia e Strong",
                        fontFamily = MorphologyMonoFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val layoutDirection = if (isHebrew) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    wordChips.forEachIndexed { index, chip ->
                        Box(
                            modifier = Modifier
                                .clip(chipShape)
                                .background(if (chip.hasIndex) indexedBg else defaultBg)
                                .border(
                                    width = if (chip.hasIndex) 1.dp else 0.75.dp,
                                    color = if (chip.hasIndex) indexedBorder else defaultBorder,
                                    shape = chipShape
                                )
                                .clickable(enabled = chip.isClickable) {
                                    onWordClick(chip.rawWord)
                                }
                                .testTag("original_word_${verseNumber}_$index")
                        ) {
                            Text(
                                text = chip.rawWord,
                                fontFamily = BiblicalScriptFontFamily,
                                fontSize = 19.sp,
                                lineHeight = 27.sp,
                                fontWeight = if (chip.hasIndex) FontWeight.SemiBold else FontWeight.Normal,
                                textDecoration = if (chip.hasIndex) TextDecoration.Underline else TextDecoration.None,
                                color = if (chip.hasIndex) {
                                    extended.originalScriptAccent
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LexiconDetailBottomSheet(
    study: SelectedWordStudy,
    wordModalAiState: AiStreamState,
    onAnalyzeWordAi: (SelectedWordStudy) -> Unit,
    onAskTheo: (LexiconEntry) -> Unit,
    onJumpToPassage: (bookId: String, chapter: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val entry = study.lexiconEntry
    val extended = LocalBereaColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isClosing by remember { mutableStateOf(false) }

    fun closeThen(afterHide: () -> Unit = onDismiss) {
        if (isClosing) return
        isClosing = true
        scope.launch {
            try {
                sheetState.hide()
            } finally {
                afterHide()
            }
        }
    }

    val maxDistCount = remember(entry) {
        entry.bookDistribution.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    }
    val decodedMorphology = remember(study.morphologyCode, entry.language) {
        MorphologyDecoder.decode(study.morphologyCode, entry.language)
    }
    val inferredAttributes = remember(entry, decodedMorphology) {
        if (decodedMorphology.isEmpty()) MorphologyDecoder.inferLexicalAttributes(entry) else emptyList()
    }
    val displayAttributes = if (decodedMorphology.isNotEmpty()) decodedMorphology else inferredAttributes

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
                .padding(horizontal = 22.dp, vertical = 6.dp)
                .testTag("original_word_strong_modal")
        ) {
            // Top Header: Language + Strong Code + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${entry.language.label.uppercase()} · STRONG ${entry.strongId}",
                            fontFamily = MorphologyMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    if (entry.occurrences > 1) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${entry.occurrences}x",
                                fontFamily = MorphologyMonoFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { closeThen(onDismiss) },
                    modifier = Modifier.testTag("close_word_modal_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar modal")
                }
            }

            if (study.verseRef.isNotBlank()) {
                Text(
                    text = "OCORRÊNCIA EM ${study.verseRef.uppercase()}",
                    fontFamily = MorphologyMonoFontFamily,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Clicked Surface Word
            Text(
                text = study.clickedWord,
                fontFamily = BiblicalScriptFontFamily,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val translitDisplay = study.clickedTransliteration.ifBlank { entry.transliteration }
            if (translitDisplay.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$translitDisplay  •  /${entry.pronunciation}/",
                    style = MaterialTheme.typography.titleMedium,
                    color = extended.originalScriptAccent
                )
            }

            if (study.contextualGloss.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Glosa no contexto: ${study.contextualGloss}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 1: Análise Morfológica
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = extended.folioHighlightBg,
                border = BorderStroke(1.dp, extended.subtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ANÁLISE MORFOLÓGICA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (study.morphologyCode.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = extended.morphologyBg
                            ) {
                                Text(
                                    text = study.morphologyCode,
                                    fontFamily = MorphologyMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (decodedMorphology.isNotEmpty() || wordModalAiState !is AiStreamState.Loading) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            displayAttributes.forEach { attr ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, extended.subtleBorder)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Text(
                                            text = attr.label.uppercase(),
                                            fontFamily = MorphologyMonoFontFamily,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = attr.value,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                        if (decodedMorphology.isEmpty() && wordModalAiState is AiStreamState.Idle) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Classificação lexical base · toque em 'Parsing Detalhado' abaixo para flexão completa no contexto.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Decodificando morfologia contextual e Strong...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 2: Raiz & Lema (Léxico Strong)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, extended.subtleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "RAIZ & LEMA LEXICAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = study.lemma.ifBlank { entry.originalWord },
                            fontFamily = BiblicalScriptFontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = extended.originalScriptAccent
                        )
                        Text(
                            text = "(${entry.transliteration})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (entry.etymology.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entry.etymology,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(14.dp))

            // Section 3: Definição conforme o Léxico Strong
            Text(
                text = "DEFINIÇÃO CONFORME O LÉXICO STRONG (${entry.strongId})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.shortGloss,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.definition,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (entry.semanticDomains.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entry.semanticDomains.forEach { domain ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = extended.morphologyBg
                        ) {
                            Text(
                                text = domain,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "USO TEOLÓGICO & SINTÁTICO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.theologicalUsage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Live AI Strong & Morphological Enrichment (automatically runs for unindexed words, or on tap)
            if (wordModalAiState !is AiStreamState.Idle) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CONSULTA LÉXICA STRONG & MORFOLOGIA (IA)",
                            fontFamily = MorphologyMonoFontFamily,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        when (wordModalAiState) {
                            is AiStreamState.Loading -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Identificando código Strong, raiz e morfologia...")
                                }
                            }
                            is AiStreamState.Streaming -> {
                                Text(wordModalAiState.text, style = MaterialTheme.typography.bodyMedium)
                            }
                            is AiStreamState.Success -> {
                                Text(wordModalAiState.text, style = MaterialTheme.typography.bodyMedium)
                            }
                            is AiStreamState.Error -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        wordModalAiState.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    TextButton(
                                        onClick = { onAnalyzeWordAi(study) },
                                        modifier = Modifier.testTag("retry_word_ai_button")
                                    ) {
                                        Text("Tentar novamente")
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }
                }
            }

            if (entry.bookDistribution.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "DISTRIBUIÇÃO DE OCORRÊNCIAS POR CORPUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    entry.bookDistribution.forEach { stat ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stat.bookName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${stat.count}x",
                                    fontFamily = MorphologyMonoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { (stat.count.toFloat() / maxDistCount.toFloat()).coerceIn(0.05f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(100.dp)),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = extended.morphologyBg
                            )
                        }
                    }
                }
            }

            if (entry.sampleOccurrences.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "OCORRÊNCIAS NO TEXTO (TOQUE PARA ABRIR)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    entry.sampleOccurrences.forEach { hit ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = extended.folioHighlightBg,
                            border = BorderStroke(1.dp, extended.subtleBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { closeThen { onJumpToPassage(hit.bookId, hit.chapter) } }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = hit.verseRef,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = extended.morphologyBg
                                        ) {
                                            Text(
                                                text = hit.grammarForm,
                                                fontFamily = MorphologyMonoFontFamily,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Abrir passagem",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = hit.originalExcerpt,
                                    fontFamily = BiblicalScriptFontFamily,
                                    fontSize = 16.sp,
                                    color = extended.originalScriptAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = hit.portugueseText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else if (entry.keyVerses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "PASSAGENS-CHAVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.keyVerses.forEach { ref ->
                        AssistChip(
                            onClick = {},
                            label = { Text(ref, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (wordModalAiState is AiStreamState.Idle) {
                    OutlinedButton(
                        onClick = { onAnalyzeWordAi(study) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Parsing Detalhado")
                    }
                }
                Button(
                    onClick = { closeThen { onAskTheo(entry) } },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("lexicon_ask_theo_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exegese & Ocorrências")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AddNoteDialog(
    initialVerseRef: String,
    onSave: (verseRef: String, title: String, content: String, tag: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var verseRef by remember { mutableStateOf(initialVerseRef) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("Exegese") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nova Nota Exegética",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = verseRef,
                    onValueChange = { verseRef = it },
                    label = { Text("Referência (ex: 1 Pedro 1:3)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_verse_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da anotação") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Categoria (ex: Morfologia, Crítica Textual)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Observações sintáticas, léxicas ou históricas") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_content_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onSave(verseRef, title, content, tag)
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Text("Salvar Nota")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndSettingsSheet(
    userName: String,
    isDarkMode: Boolean,
    profile: UserProfile?,
    onToggleDarkMode: (Boolean) -> Unit,
    onSaveNotifications: (Boolean, Int, Int) -> Unit,
    onTestNotification: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var notifEnabled by remember(profile) { mutableStateOf(profile?.notificationsEnabled ?: false) }
    val notifHour by remember(profile) { mutableIntStateOf((profile?.notificationHour ?: 7L).toInt()) }
    val notifMinute by remember(profile) { mutableIntStateOf((profile?.notificationMinute ?: 30L).toInt()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = userName.ifBlank { "Estudante de Exegese" },
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Bancada local no dispositivo (sem login)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Aparência da Bancada", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (isDarkMode) "Estúdio Noturno" else "Pergaminho Editorial",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode,
                    modifier = Modifier.testTag("sheet_theme_switch")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Lembretes Diários", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = String.format("Horário: %02d:%02d", notifHour, notifMinute),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = notifEnabled,
                    onCheckedChange = {
                        notifEnabled = it
                        onSaveNotifications(it, notifHour, notifMinute)
                    },
                    modifier = Modifier.testTag("sheet_notification_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onTestNotification,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar Notificação de Progresso Agora")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ConfirmSqliteImportDialog(
    pending: PendingSqliteBible,
    onConfirm: (code: String, fullName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember(pending.id) { mutableStateOf(pending.suggestedCode) }
    var fullName by remember(pending.id) { mutableStateOf(pending.suggestedName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Importar Bíblia Local",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar importação")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${pending.versesCount} versículos detectados (${pending.detectedLanguage}). Disponível offline nos livros presentes no arquivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase().take(8) },
                    label = { Text("Sigla da versão (ex: NVI, ARA, ACF)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sqlite_code_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it.take(60) },
                    label = { Text("Nome completo da versão") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sqlite_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(code, fullName) },
                        enabled = code.isNotBlank() && fullName.isNotBlank(),
                        modifier = Modifier.testTag("confirm_sqlite_import_button")
                    ) {
                        Text("Adicionar Versão")
                    }
                }
            }
        }
    }
}

