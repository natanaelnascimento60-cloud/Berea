package com.example.data

import android.content.Context
import android.util.Log
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.ImportedBibleVersion
import com.example.model.ReadingPlan
import com.example.model.StudyNote
import com.example.model.UserProfile
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val streakDays: Long,
    val lastStudyDate: String?,
    val preferredTranslation: String,
    val notificationsEnabled: Boolean,
    val notificationHour: Long,
    val notificationMinute: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomain(): UserProfile = UserProfile(
        id = userId,
        userId = userId,
        displayName = displayName,
        streakDays = streakDays,
        lastStudyDate = lastStudyDate,
        preferredTranslation = preferredTranslation,
        notificationsEnabled = notificationsEnabled,
        notificationHour = notificationHour,
        notificationMinute = notificationMinute,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val verseRef: String,
    val title: String,
    val content: String,
    val tag: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomain(): StudyNote = StudyNote(
        id = id,
        userId = userId,
        verseRef = verseRef,
        title = title,
        content = content,
        tag = tag,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

@Entity(tableName = "reading_plans")
data class ReadingPlanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val totalDays: Long,
    val completedDays: List<Long>,
    val readings: List<String>,
    val active: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toDomain(): ReadingPlan = ReadingPlan(
        id = id,
        userId = userId,
        title = title,
        description = description,
        totalDays = totalDays,
        completedDays = completedDays,
        readings = readings,
        active = active,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

@Entity(tableName = "imported_bibles")
data class ImportedBibleEntity(
    @PrimaryKey val id: String,
    val code: String,
    val fullName: String,
    val languageLabel: String,
    val localFilePath: String,
    val schemaType: String,
    val versesCount: Int,
    val createdAtMillis: Long
) {
    fun toDomain(): ImportedBibleVersion = ImportedBibleVersion(
        id = id,
        code = code,
        fullName = fullName,
        languageLabel = languageLabel,
        localFilePath = localFilePath,
        schemaType = schemaType,
        versesCount = versesCount,
        createdAtMillis = createdAtMillis
    )
}

class StudyTypeConverters {
    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        val arr = JSONArray()
        value.forEach { arr.put(it) }
        return arr.toString()
    }

    @TypeConverter
    fun toLongList(raw: String): List<Long> {
        if (raw.isBlank()) return emptyList()
        val arr = JSONArray(raw)
        return buildList(arr.length()) {
            for (i in 0 until arr.length()) {
                add(arr.optLong(i))
            }
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        val arr = JSONArray()
        value.forEach { arr.put(it) }
        return arr.toString()
    }

    @TypeConverter
    fun toStringList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val arr = JSONArray(raw)
        return buildList(arr.length()) {
            for (i in 0 until arr.length()) {
                val item = arr.optString(i)
                if (item.isNotBlank()) add(item)
            }
        }
    }
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun observeProfile(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfileOnce(userId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET preferredTranslation = :translationCode, updatedAtMillis = :updatedAt WHERE userId = :userId")
    suspend fun updatePreferredTranslation(userId: String, translationCode: String, updatedAt: Long)

    @Query("UPDATE user_profiles SET notificationsEnabled = :enabled, notificationHour = :hour, notificationMinute = :minute, updatedAtMillis = :updatedAt WHERE userId = :userId")
    suspend fun updateNotificationSettings(userId: String, enabled: Boolean, hour: Long, minute: Long, updatedAt: Long)

    @Query("UPDATE user_profiles SET streakDays = :streakDays, lastStudyDate = :lastStudyDate, updatedAtMillis = :updatedAt WHERE userId = :userId")
    suspend fun updateStreak(userId: String, streakDays: Long, lastStudyDate: String, updatedAt: Long)

    @Query("SELECT * FROM study_notes WHERE userId = :userId ORDER BY updatedAtMillis DESC, createdAtMillis DESC")
    fun observeNotes(userId: String): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: StudyNoteEntity)

    @Query("DELETE FROM study_notes WHERE userId = :userId AND id = :noteId")
    suspend fun deleteNote(userId: String, noteId: String)

    @Query("SELECT * FROM reading_plans WHERE userId = :userId ORDER BY active DESC, updatedAtMillis DESC")
    fun observeReadingPlans(userId: String): Flow<List<ReadingPlanEntity>>

    @Query("SELECT COUNT(*) FROM reading_plans WHERE userId = :userId")
    suspend fun countPlans(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlan(plan: ReadingPlanEntity)

    @Query("UPDATE reading_plans SET active = 0, updatedAtMillis = :updatedAt WHERE userId = :userId AND id IN (:planIds)")
    suspend fun deactivatePlans(userId: String, planIds: List<String>, updatedAt: Long)

    @Query("UPDATE reading_plans SET completedDays = :completedDays, updatedAtMillis = :updatedAt WHERE userId = :userId AND id = :planId")
    suspend fun updatePlanCompletedDays(userId: String, planId: String, completedDays: List<Long>, updatedAt: Long)

    @Query("SELECT * FROM imported_bibles ORDER BY createdAtMillis DESC")
    fun observeImportedBibles(): Flow<List<ImportedBibleEntity>>

    @Query("SELECT * FROM imported_bibles ORDER BY createdAtMillis DESC")
    suspend fun getImportedBiblesOnce(): List<ImportedBibleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImportedBible(bible: ImportedBibleEntity)

    @Query("DELETE FROM imported_bibles WHERE id = :id")
    suspend fun deleteImportedBible(id: String)
}

@Database(
    entities = [
        UserProfileEntity::class,
        StudyNoteEntity::class,
        ReadingPlanEntity::class,
        ImportedBibleEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(StudyTypeConverters::class)
abstract class LocalStudyDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: LocalStudyDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS imported_bibles (
                        id TEXT NOT NULL PRIMARY KEY,
                        code TEXT NOT NULL,
                        fullName TEXT NOT NULL,
                        languageLabel TEXT NOT NULL,
                        localFilePath TEXT NOT NULL,
                        schemaType TEXT NOT NULL,
                        versesCount INTEGER NOT NULL,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): LocalStudyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: runCatching {
                    Room.databaseBuilder(
                        context.applicationContext,
                        LocalStudyDatabase::class.java,
                        "berea_study.db"
                    )
                        .addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration()
                        .build()
                }.getOrElse { err ->
                    Log.e("LocalStudyDatabase", "Failed to build file database, falling back to in-memory", err)
                    Room.inMemoryDatabaseBuilder(
                        context.applicationContext,
                        LocalStudyDatabase::class.java
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }.also { INSTANCE = it }
            }
        }
    }
}
