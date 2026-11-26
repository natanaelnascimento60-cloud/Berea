package com.example.data

import android.content.Context
import com.example.model.ImportedBibleVersion
import com.example.model.ReadingPlan
import com.example.model.StudyNote
import com.example.model.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserStudyRepository(
    private val dao: StudyDao
) {
    constructor(context: Context) : this(
        LocalStudyDatabase.getInstance(context).studyDao()
    )

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        require(userId.isNotBlank()) { "userId must not be blank" }
        return dao.observeProfile(userId).map { it?.toDomain() }
    }

    suspend fun ensureUserProfileAndStarterPlan(userId: String, displayName: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val cleanName = displayName.trim().ifBlank { "Estudante" }.take(80)
            if (dao.getProfileOnce(userId) == null) {
                dao.upsertProfile(
                    UserProfileEntity(
                        userId = userId,
                        displayName = cleanName,
                        streakDays = 1L,
                        lastStudyDate = todayIsoString(),
                        preferredTranslation = "BLIVRE",
                        notificationsEnabled = false,
                        notificationHour = 7L,
                        notificationMinute = 30L,
                        createdAtMillis = now,
                        updatedAtMillis = now
                    )
                )
            }

            if (dao.countPlans(userId) == 0) {
                dao.upsertPlan(
                    ReadingPlanEntity(
                        id = "starter_plan",
                        userId = userId,
                        title = "Fundamentos da Esperança e do Verbo",
                        description = "Jornada de 7 dias unindo Evangelhos, Epístolas e léxico grego-hebraico.",
                        totalDays = 7L,
                        completedDays = emptyList(),
                        readings = listOf(
                            "Dia 1: 1 Pedro 1 — A Viva Esperança (ἀναγεννάω)",
                            "Dia 2: João 1 — O Verbo Eterno (λόγος)",
                            "Dia 3: Salmos 23 — O Pastor e o Amor Leal (חֶסֶד)",
                            "Dia 4: Romanos 8 — Vida no Espírito (πνεῦμα)",
                            "Dia 5: Efésios 2 — Salvos pela Graça (χάρις)",
                            "Dia 6: Filipenses 2 — A Humilhação de Cristo",
                            "Dia 7: 1 Coríntios 13 — A Excelência do Amor (ἀγάπη)"
                        ),
                        active = true,
                        createdAtMillis = now,
                        updatedAtMillis = now
                    )
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePreferredTranslation(userId: String, translationCode: String): Result<Unit> {
        return try {
            dao.updatePreferredTranslation(userId, translationCode, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotificationSettings(
        userId: String,
        enabled: Boolean,
        hour: Int,
        minute: Int
    ): Result<Unit> {
        return try {
            dao.updateNotificationSettings(
                userId = userId,
                enabled = enabled,
                hour = hour.coerceIn(0, 23).toLong(),
                minute = minute.coerceIn(0, 59).toLong(),
                updatedAt = System.currentTimeMillis()
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeNotes(userId: String): Flow<List<StudyNote>> {
        require(userId.isNotBlank())
        return dao.observeNotes(userId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun addNote(
        userId: String,
        verseRef: String,
        title: String,
        content: String,
        tag: String?
    ): Result<Unit> {
        return try {
            val cleanVerseRef = verseRef.trim().ifBlank { "Estudo Geral" }.take(100)
            val cleanTitle = title.trim().take(120)
            val cleanContent = content.trim().take(5000)
            require(cleanTitle.isNotEmpty()) { "Título da nota obrigatório." }
            require(cleanContent.isNotEmpty()) { "Conteúdo da nota obrigatório." }

            val now = System.currentTimeMillis()
            dao.upsertNote(
                StudyNoteEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    verseRef = cleanVerseRef,
                    title = cleanTitle,
                    content = cleanContent,
                    tag = tag?.trim()?.takeIf { it.isNotEmpty() }?.take(40),
                    createdAtMillis = now,
                    updatedAtMillis = now
                )
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(userId: String, noteId: String): Result<Unit> {
        return try {
            require(noteId.isNotBlank())
            dao.deleteNote(userId, noteId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeReadingPlans(userId: String): Flow<List<ReadingPlan>> {
        require(userId.isNotBlank())
        return dao.observeReadingPlans(userId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun createReadingPlan(
        userId: String,
        title: String,
        description: String,
        readings: List<String>,
        existingPlanIdsToDeactivate: List<String>
    ): Result<Unit> {
        return try {
            val cleanTitle = title.trim().ifBlank { "Plano Personalizado" }.take(120)
            val cleanDesc = description.trim().take(500)
            val cleanReadings = readings.map { it.trim().take(200) }.filter { it.isNotEmpty() }.take(365)
            require(cleanReadings.isNotEmpty()) { "O plano precisa ter ao menos uma leitura diária." }

            val now = System.currentTimeMillis()
            if (existingPlanIdsToDeactivate.isNotEmpty()) {
                dao.deactivatePlans(userId, existingPlanIdsToDeactivate, now)
            }

            dao.upsertPlan(
                ReadingPlanEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = cleanTitle,
                    description = cleanDesc,
                    totalDays = cleanReadings.size.toLong(),
                    completedDays = emptyList(),
                    readings = cleanReadings,
                    active = true,
                    createdAtMillis = now,
                    updatedAtMillis = now
                )
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun togglePlanDayCompletion(
        userId: String,
        plan: ReadingPlan,
        dayNumber: Long,
        currentProfile: UserProfile?
    ): Result<Unit> {
        return try {
            require(plan.id.isNotBlank())
            val alreadyDone = plan.completedDays.contains(dayNumber)
            val updatedDays = if (alreadyDone) {
                plan.completedDays - dayNumber
            } else {
                (plan.completedDays + dayNumber).distinct().sorted()
            }
            val now = System.currentTimeMillis()
            dao.updatePlanCompletedDays(userId, plan.id, updatedDays, now)

            val today = todayIsoString()
            if (!alreadyDone && currentProfile != null && currentProfile.lastStudyDate != today) {
                dao.updateStreak(userId, currentProfile.streakDays + 1L, today, now)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeImportedBibles(): Flow<List<ImportedBibleVersion>> {
        return dao.observeImportedBibles().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getImportedBiblesOnce(): List<ImportedBibleVersion> {
        return dao.getImportedBiblesOnce().map { it.toDomain() }
    }

    suspend fun saveImportedBible(version: ImportedBibleVersion): Result<Unit> {
        return try {
            dao.upsertImportedBible(
                ImportedBibleEntity(
                    id = version.id,
                    code = version.code,
                    fullName = version.fullName,
                    languageLabel = version.languageLabel,
                    localFilePath = version.localFilePath,
                    schemaType = version.schemaType,
                    versesCount = version.versesCount,
                    createdAtMillis = version.createdAtMillis
                )
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteImportedBible(id: String): Result<Unit> {
        return try {
            dao.deleteImportedBible(id)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun todayIsoString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
