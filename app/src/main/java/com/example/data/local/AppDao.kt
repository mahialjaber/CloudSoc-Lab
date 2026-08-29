package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Reports
    @Query("SELECT * FROM saved_reports ORDER BY submissionTimestamp DESC")
    fun getAllReports(): Flow<List<SavedReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: SavedReportEntity)

    @Query("DELETE FROM saved_reports WHERE reportId = :reportId")
    suspend fun deleteReport(reportId: String)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // Incident Progress
    @Query("SELECT * FROM incident_progress WHERE incidentId = :incidentId")
    suspend fun getIncidentProgress(incidentId: String): IncidentProgressEntity?

    @Query("SELECT * FROM incident_progress")
    fun getAllIncidentProgress(): Flow<List<IncidentProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveIncidentProgress(progress: IncidentProgressEntity)

    @Query("DELETE FROM incident_progress WHERE incidentId = :incidentId")
    suspend fun deleteIncidentProgress(incidentId: String)

    // Mentor Messages
    @Query("SELECT * FROM mentor_messages WHERE incidentId = :incidentId OR (incidentId IS NULL AND :incidentId IS NULL) ORDER BY timestamp ASC")
    fun getMentorMessages(incidentId: String?): Flow<List<MentorMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMentorMessage(message: MentorMessageEntity)

    @Query("DELETE FROM mentor_messages WHERE incidentId = :incidentId")
    suspend fun clearMentorHistory(incidentId: String?)

    @Query("DELETE FROM incident_progress")
    suspend fun clearAllProgress()
}
