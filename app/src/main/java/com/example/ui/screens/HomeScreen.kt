package com.example.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.R
import com.example.model.DailyVerseStudy
import com.example.model.ReadingPlan
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.ui.components.BereaWordmark
import com.example.ui.theme.BiblicalScriptFontFamily
import com.example.ui.theme.LocalBereaColors
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.MorphologyMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    dailyStudy: DailyVerseStudy,
    userName: String,
    profileState: UiState<UserProfile?>,
    plansState: UiState<List<ReadingPlan>>,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onOpenProfileSheet: () -> Unit,
    onStudyWithTheo: (DailyVerseStudy) -> Unit,
    onOpenDailyWordLexicon: (strongId: String, morphologyCode: String, verseRef: String) -> Unit,
    onOpenPassageInBible: (bookId: String, chapter: Int) -> Unit,
    onOpenWorkbenchOccurrences: () -> Unit,
    onOpenWorkbenchLexicon: () -> Unit,
    onOpenExegesisAiTab: () -> Unit,
    onCompleteTodayPlanDay: (ReadingPlan, Long) -> Unit,
    onOpenPlanManager: () -> Unit
) {
    val context = LocalContext.current
    val extended = LocalBereaColors.current
    val profile = (profileState as? UiState.Success)?.data
    val plans = (plansState as? UiState.Success)?.data.orEmpty()
    val activePlan = plans.firstOrNull { it.active } ?: plans.firstOrNull()

    val firstName = remember(userName, profile) {
        val raw = profile?.displayName?.takeIf { it.isNotBlank() } ?: userName
        raw.trim().split(" ").firstOrNull().orEmpty().ifBlank { "Estudante" }
    }
    val initials = remember(firstName) {
        firstName.take(2).uppercase()
    }
    val dateLabel = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
            .format(Date())
            .uppercase()
    }
    val streakCount = profile?.streakDays ?: 1L

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 660.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Archival Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BereaWordmark()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { onToggleDarkMode(!isDarkMode) },
                            modifier = Modifier.testTag("home_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Alternar tema",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, extended.subtleBorder),
                            modifier = Modifier.clickable { onOpenPlanManager() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Sequência",
                                    tint = extended.brassAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streakCount ${if (streakCount == 1L) "dia" else "dias"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable { onOpenProfileSheet() }
                                .testTag("home_profile_avatar")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Date & Workbench Header
            item {
                Column(modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = "$dateLabel · BANCADA EXEGÉTICA",
                        fontFamily = MorphologyMonoFontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Exame das Escrituras, $firstName",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Folio Card 1: Perícope & Original do Dia (Burgundy Left Rubric Accent — completely original Berea layout)
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, extended.subtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PERÍCOPE & ORIGINAL DO DIA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = dailyStudy.strongId,
                                            fontFamily = MorphologyMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = extended.morphologyBg
                                    ) {
                                        Text(
                                            text = "${dailyStudy.occurrencesCount} ocorr.",
                                            fontFamily = MorphologyMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Original Text Block
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = extended.folioHighlightBg,
                                border = BorderStroke(1.dp, extended.subtleBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = dailyStudy.originalExcerpt,
                                    fontFamily = BiblicalScriptFontFamily,
                                    fontSize = 18.sp,
                                    lineHeight = 27.sp,
                                    color = extended.originalScriptAccent,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "“${dailyStudy.verseText}”",
                                fontFamily = LoraFontFamily,
                                fontSize = 19.sp,
                                lineHeight = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable {
                                    onOpenPassageInBible(dailyStudy.bookId, dailyStudy.chapter)
                                }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dailyStudy.translationLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Original Word & Morphology Strip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = extended.folioHighlightBg,
                                border = BorderStroke(1.dp, extended.subtleBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onOpenDailyWordLexicon(
                                            dailyStudy.strongId,
                                            dailyStudy.morphology,
                                            dailyStudy.verseRef
                                        )
                                    }
                                    .testTag("home_original_word_card")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = dailyStudy.originalWord,
                                                fontFamily = BiblicalScriptFontFamily,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = extended.morphologyBg
                                            ) {
                                                Text(
                                                    text = dailyStudy.morphology,
                                                    fontFamily = MorphologyMonoFontFamily,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${dailyStudy.transliteration} — ${dailyStudy.originalGloss}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Léxico →",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onStudyWithTheo(dailyStudy) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("study_with_theo_button")
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Exegese do Verso",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        onOpenDailyWordLexicon(
                                            dailyStudy.strongId,
                                            dailyStudy.morphology,
                                            dailyStudy.verseRef
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Text(
                                        text = "Ver Ocorrências",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val shareText = "${dailyStudy.originalExcerpt}\n\n“${dailyStudy.verseText}”\n— ${dailyStudy.translationLabel}\n\nLema: ${dailyStudy.originalWord} (${dailyStudy.transliteration}, ${dailyStudy.strongId})\nEstudado no Berea Exegese"
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Compartilhar perícope"))
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("share_daily_verse_button")
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Compartilhar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Workbench Tools (2x2 Grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "FERRAMENTAS DE EXEGESE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WorkbenchShortcutCard(
                            title = "Originais & Interlinear",
                            subtitle = "Grego, Hebraico e morfologia",
                            icon = Icons.Default.Translate,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = { onOpenPassageInBible(dailyStudy.bookId, dailyStudy.chapter) },
                            modifier = Modifier.weight(1f)
                        )
                        WorkbenchShortcutCard(
                            title = "Ocorrências",
                            subtitle = "Concordância e frequência",
                            icon = Icons.Default.Analytics,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = onOpenWorkbenchOccurrences,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WorkbenchShortcutCard(
                            title = "Léxicos & Comentários",
                            subtitle = "Strong G/H e crítica textual",
                            icon = Icons.AutoMirrored.Filled.LibraryBooks,
                            accentColor = extended.brassAccent,
                            onClick = onOpenWorkbenchLexicon,
                            modifier = Modifier.weight(1f)
                        )
                        WorkbenchShortcutCard(
                            title = "Mesa Exegética IA",
                            subtitle = "Sintaxe e aparato textual",
                            icon = Icons.Default.AutoAwesome,
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = onOpenExegesisAiTab,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Reading Plan & Daily Exegesis Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, extended.subtleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val scholarBitmap by produceState<ImageBitmap?>(initialValue = null, context) {
                                value = withContext(Dispatchers.IO) {
                                    val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                                    BitmapFactory.decodeResource(
                                        context.resources,
                                        R.drawable.img_devotional_scholar,
                                        opts
                                    )?.asImageBitmap()
                                }
                            }
                            if (scholarBitmap != null) {
                                Image(
                                    bitmap = scholarBitmap!!,
                                    contentDescription = "Ilustração do estudo exegético diário",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(extended.folioHighlightBg)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NOTA EXEGÉTICA · ${dailyStudy.readingMinutes} MIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dailyStudy.devotionalTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dailyStudy.devotionalBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        if (activePlan != null) {
                            val total = activePlan.totalDays.coerceAtLeast(1L)
                            val completedCount = activePlan.completedDays.size.toLong()
                            val nextDay = (1L..total).firstOrNull { !activePlan.completedDays.contains(it) } ?: total
                            val nextReadingText = activePlan.readings.getOrNull((nextDay - 1).toInt())
                                ?: "Dia $nextDay concluído"
                            val isNextDone = activePlan.completedDays.contains(nextDay)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PLANO: ${activePlan.title.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$completedCount/$total dias",
                                    fontFamily = MorphologyMonoFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (completedCount.toFloat() / total.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(100.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = extended.morphologyBg
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = nextReadingText,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onOpenPassageInBible(dailyStudy.bookId, dailyStudy.chapter) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("home_read_devotional_button")
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Abrir no Texto", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }

                                OutlinedButton(
                                    onClick = { onCompleteTodayPlanDay(activePlan, nextDay) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("home_complete_day_button")
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isNextDone) extended.successGreen else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isNextDone) "Concluído" else "Marcar dia")
                                }
                            }
                        } else {
                            Button(
                                onClick = onOpenPlanManager,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Configurar Plano Exegético")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = extended.folioHighlightBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPlanManager() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val notifText = if (profile?.notificationsEnabled == true) {
                                        String.format(
                                            "Notificação diária ativa às %02d:%02d",
                                            profile.notificationHour,
                                            profile.notificationMinute
                                        )
                                    } else {
                                        "Ativar notificações diárias de progresso"
                                    }
                                    Text(
                                        text = notifText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Ajustar",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkbenchShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extended = LocalBereaColors.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extended.subtleBorder),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
