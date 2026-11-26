package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyStudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val planTitle = intent.getStringExtra(EXTRA_PLAN_TITLE) ?: "Plano de Leitura Diário"
        val progress = intent.getStringExtra(EXTRA_PROGRESS) ?: "Continue sua jornada"
        StudyNotificationHelper.showProgressNotification(
            context = context,
            title = "Bom estudo! $planTitle",
            body = "Progresso atual: $progress. Abra o Verbo para ler a passagem de hoje e estudar a palavra original."
        )
    }

    companion object {
        const val EXTRA_PLAN_TITLE = "extra_plan_title"
        const val EXTRA_PROGRESS = "extra_progress"
    }
}
