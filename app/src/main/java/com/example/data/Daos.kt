package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationLogDao {
    @Query("SELECT * FROM observation_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ObservationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ObservationLog)

    @Query("DELETE FROM observation_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM observation_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM observation_logs")
    suspend fun getLogCount(): Int
}

@Dao
interface CameraConfigDao {
    @Query("SELECT * FROM camera_configs ORDER BY id DESC")
    fun getAllCameras(): Flow<List<CameraConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCamera(config: CameraConfig)

    @Query("DELETE FROM camera_configs WHERE id = :id")
    suspend fun deleteCameraById(id: Long)

    @Query("UPDATE camera_configs SET isActive = :isActive WHERE id = :id")
    suspend fun updateCameraStatus(id: Long, isActive: Boolean)
}

@Dao
interface AiCompanionDao {
    @Query("SELECT * FROM ai_companions ORDER BY role ASC")
    fun getAllCompanions(): Flow<List<AiCompanion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanion(companion: AiCompanion)

    @Query("UPDATE ai_companions SET currentMood = :mood WHERE id = :id")
    suspend fun updateCompanionMood(id: Long, mood: String)

    @Query("UPDATE ai_companions SET relationshipScore = :score WHERE id = :id")
    suspend fun updateRelationshipScore(id: Long, score: Int)
    
    @Query("SELECT COUNT(*) FROM ai_companions")
    suspend fun getCompanionCount(): Int
}
