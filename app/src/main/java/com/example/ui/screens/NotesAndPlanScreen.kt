package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ReadingPlan
import com.example.model.StudyNote
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.notifications.StudyNotificationHelper
import com.example.ui.theme.LocalBereaColors
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.MorphologyMonoFontFamily
import kotlin.math.roundToInt

enum class NotesPlanSubTab(val label: String) {
    PLAN("PLANO DE EXEGESE"),
    NOTES("NOTAS & ANOTAÇÕES")
}

@Composable
fun NotesAndPlanScreen(
    initialSubTab: NotesPlanSubTab,
    profileState: UiState<UserProfile?>,
    plansState: UiState<List<ReadingPlan>>,
    notesState: UiState<List<StudyNote>>,
    isGeneratingPlan: Boolean,
    onTogglePlanDay: (ReadingPlan, Long) -> Unit,
    onGenerateCustomPlan: (focusTopic: String, days: Int, pace: String) -> Unit,
    onSaveNotifications: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    onAddNoteClicked: () -> Unit,
    onDeleteNote: (String) -> Unit
) {
    val context = LocalContext.current
    val extended = LocalBereaColors.current
    var selectedTab by remember(initialSubTab) { mutableStateOf(initialSubTab) }
    var showCreatePlanDialog by remember { mutableStateOf(false) }

    val profile = (profileState as? UiState.Success)?.data
    val plans = (plansState as? UiState.Success)?.data.orEmpty()
    val activePlan = plans.firstOrNull { it.active } ?: plans.firstOrNull()

    var pendingNotificationHour by remember(profile) { mutableIntStateOf((profile?.notificationHour ?: 7L).toInt()) }
    var pendingNotificationMinute by remember(profile) { mutableIntStateOf((profile?.notificationMinute ?: 30L).toInt()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onSaveNotifications(true, pendingNotificationHour, pendingNotificationMinute)
            StudyNotificationHelper.scheduleDailyReminder(
                context = context,
                hour = pendingNotificationHour,
                minute = pendingNotificationMinute,
                planTitle = activePlan?.title ?: "Plano Exegético",
                completedCount = activePlan?.completedDays?.size ?: 0,
                totalDays = (activePlan?.totalDays ?: 7L).toInt()
            )
            StudyNotificationHelper.showProgressNotification(
                context = context,
                title = "Lembretes Diários Ativados",
                body = String.format(
                    "Você receberá seu progresso diário às %02d:%02d.",
                    pendingNotificationHour,
                    pendingNotificationMinute
                )
            )
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
                text = if (selectedTab == NotesPlanSubTab.PLAN) "Plano & Progresso" else "Caderno Exegético",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(12.dp))

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
                    NotesPlanSubTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, extended.subtleBorder) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .clickable { selectedTab = tab }
                                .testTag("subtab_${tab.name.lowercase()}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == NotesPlanSubTab.PLAN) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        val isNotifEnabled = profile?.notificationsEnabled ?: false
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, extended.subtleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                            Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                "Notificações Diárias de Progresso",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = String.format(
                                                    "Horário programado: %02d:%02d",
                                                    pendingNotificationHour,
                                                    pendingNotificationMinute
                                                ),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isNotifEnabled,
                                        onCheckedChange = { enable ->
                                            if (enable) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                                    !StudyNotificationHelper.hasNotificationPermission(context)
                                                ) {
                                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                } else {
                                                    onSaveNotifications(true, pendingNotificationHour, pendingNotificationMinute)
                                                    StudyNotificationHelper.scheduleDailyReminder(
                                                        context = context,
                                                        hour = pendingNotificationHour,
                                                        minute = pendingNotificationMinute,
                                                        planTitle = activePlan?.title ?: "Plano Exegético",
                                                        completedCount = activePlan?.completedDays?.size ?: 0,
                                                        totalDays = (activePlan?.totalDays ?: 7L).toInt()
                                                    )
                                                }
                                            } else {
                                                onSaveNotifications(false, pendingNotificationHour, pendingNotificationMinute)
                                                StudyNotificationHelper.cancelDailyReminder(context)
                                            }
                                        },
                                        modifier = Modifier.testTag("plan_notification_switch")
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    listOf(7 to 0, 12 to 30, 20 to 0).forEach { (h, m) ->
                                        val isCurrent = pendingNotificationHour == h && pendingNotificationMinute == m
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    pendingNotificationHour = h
                                                    pendingNotificationMinute = m
                                                    if (isNotifEnabled) {
                                                        onSaveNotifications(true, h, m)
                                                        StudyNotificationHelper.scheduleDailyReminder(
                                                            context = context,
                                                            hour = h,
                                                            minute = m,
                                                            planTitle = activePlan?.title ?: "Plano Exegético",
                                                            completedCount = activePlan?.completedDays?.size ?: 0,
                                                            totalDays = (activePlan?.totalDays ?: 7L).toInt()
                                                        )
                                                    }
                                                }
                                        ) {
                                            Text(
                                                text = String.format("%02d:%02d", h, m),
                                                fontFamily = MorphologyMonoFontFamily,
                                                fontSize = 12.sp,
                                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                            !StudyNotificationHelper.hasNotificationPermission(context)
                                        ) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            val done = activePlan?.completedDays?.size ?: 0
                                            val tot = activePlan?.totalDays ?: 7L
                                            StudyNotificationHelper.showProgressNotification(
                                                context = context,
                                                title = "Progresso no Plano Berea",
                                                body = "${activePlan?.title ?: "Plano diário"}: $done de $tot dias concluídos."
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("test_notification_button")
                                ) {
                                    Text("Testar Notificação de Progresso")
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { showCreatePlanDialog = true },
                            enabled = !isGeneratingPlan,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("create_custom_plan_button")
                        ) {
                            if (isGeneratingPlan) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gerando Plano Exegético com IA...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Criar Plano Exegético Personalizado (IA)")
                            }
                        }
                    }

                    when (plansState) {
                        is UiState.Loading -> {
                            item {
                                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is UiState.Error -> {
                            item {
                                Text(plansState.message, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        is UiState.Success -> {
                            if (activePlan != null) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(18.dp)) {
                                            Text(
                                                text = "PLANO EXEGÉTICO ATIVO",
                                                fontFamily = MorphologyMonoFontFamily,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = activePlan.title,
                                                fontFamily = LoraFontFamily,
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onSecondary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = activePlan.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            val total = activePlan.totalDays.coerceAtLeast(1L)
                                            val done = activePlan.completedDays.size.toLong()
                                            LinearProgressIndicator(
                                                progress = { (done.toFloat() / total.toFloat()).coerceIn(0f, 1f) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(100.dp)),
                                                color = extended.brassAccent,
                                                trackColor = Color.White.copy(alpha = 0.2f)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "$done de $total dias concluídos",
                                                fontFamily = MorphologyMonoFontFamily,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSecondary
                                            )
                                        }
                                    }
                                }

                                itemsIndexed(activePlan.readings) { index, readingText ->
                                    val dayNum = (index + 1).toLong()
                                    val isCompleted = activePlan.completedDays.contains(dayNum)
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, extended.subtleBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onTogglePlanDay(activePlan, dayNum) }
                                            .testTag("plan_day_row_$dayNum")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = if (isCompleted) "Dia concluído" else "Marcar como lido",
                                                tint = if (isCompleted) extended.successGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = readingText,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                when (notesState) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Error -> {
                        Text(notesState.message, color = MaterialTheme.colorScheme.error)
                    }
                    is UiState.Success -> {
                        val notes = notesState.data
                        if (notes.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, extended.subtleBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Nenhuma nota exegética ainda",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Toque no botão + ou selecione qualquer versículo em Texto & Originais para registrar observações sintáticas e léxicas.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 84.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(notes, key = { it.id }) { note ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, extended.subtleBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = note.verseRef,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                IconButton(onClick = { onDeleteNote(note.id) }) {
                                                    Icon(
                                                        Icons.Default.DeleteOutline,
                                                        contentDescription = "Excluir nota",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = note.title,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = note.content,
                                                style = MaterialTheme.typography.bodyLarge,
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

        if (selectedTab == NotesPlanSubTab.NOTES) {
            FloatingActionButton(
                onClick = onAddNoteClicked,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_note_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova nota")
            }
        }
    }

    if (showCreatePlanDialog) {
        CreateCustomPlanDialog(
            onGenerate = { topic, days, pace ->
                showCreatePlanDialog = false
                onGenerateCustomPlan(topic, days, pace)
            },
            onDismiss = { showCreatePlanDialog = false }
        )
    }
}

@Composable
private fun CreateCustomPlanDialog(
    onGenerate: (String, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var topic by remember { mutableStateOf("Epístola aos Romanos: Sintaxe e Justificação") }
    var daysFloat by remember { mutableStateOf(7f) }
    var pace by remember { mutableStateOf("15 minutos por dia com foco em léxico e sintaxe") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Plano Exegético com IA",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Defina o livro, tema ou léxico bíblico e receba um roteiro diário estruturado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Tema ou Livros Bíblicos") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plan_topic_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Duração: ${daysFloat.roundToInt()} dias",
                    style = MaterialTheme.typography.titleMedium
                )
                Slider(
                    value = daysFloat,
                    onValueChange = { daysFloat = it },
                    valueRange = 3f..21f,
                    steps = 17
                )

                OutlinedTextField(
                    value = pace,
                    onValueChange = { pace = it },
                    label = { Text("Ritmo diário") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onGenerate(topic, daysFloat.roundToInt(), pace) },
                        enabled = topic.isNotBlank(),
                        modifier = Modifier.testTag("confirm_generate_plan_button")
                    ) {
                        Text("Gerar Plano")
                    }
                }
            }
        }
    }
}
