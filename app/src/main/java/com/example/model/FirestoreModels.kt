package com.example.model

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AiStreamState {
    data object Idle : AiStreamState
    data object Loading : AiStreamState
    data class Streaming(val text: String) : AiStreamState
    data class Success(val text: String) : AiStreamState
    data class Error(val message: String) : AiStreamState
}

data class UserProfile(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "Estudante",
    val streakDays: Long = 1L,
    val lastStudyDate: String? = null,
    val preferredTranslation: String = "BLIVRE",
    val notificationsEnabled: Boolean = false,
    val notificationHour: Long = 7L,
    val notificationMinute: Long = 30L,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
)

data class StudyNote(
    val id: String = "",
    val userId: String = "",
    val verseRef: String = "",
    val title: String = "",
    val content: String = "",
    val tag: String? = null,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
)

data class ReadingPlan(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val totalDays: Long = 7L,
    val completedDays: List<Long> = emptyList(),
    val readings: List<String> = emptyList(),
    val active: Boolean = true,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
)
