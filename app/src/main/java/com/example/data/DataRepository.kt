package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataRepository(
    private val logDao: ObservationLogDao,
    private val cameraDao: CameraConfigDao,
    private val companionDao: AiCompanionDao
) {
    val allLogs: Flow<List<ObservationLog>> = logDao.getAllLogs()
    val allCameras: Flow<List<CameraConfig>> = cameraDao.getAllCameras()
    val allCompanions: Flow<List<AiCompanion>> = companionDao.getAllCompanions()

    suspend fun insertLog(log: ObservationLog) = withContext(Dispatchers.IO) {
        logDao.insertLog(log)
    }

    suspend fun deleteLog(id: Long) = withContext(Dispatchers.IO) {
        logDao.deleteLogById(id)
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        logDao.clearAllLogs()
    }

    suspend fun insertCamera(config: CameraConfig) = withContext(Dispatchers.IO) {
        cameraDao.insertCamera(config)
    }

    suspend fun deleteCamera(id: Long) = withContext(Dispatchers.IO) {
        cameraDao.deleteCameraById(id)
    }

    suspend fun updateCameraStatus(id: Long, isActive: Boolean) = withContext(Dispatchers.IO) {
        cameraDao.updateCameraStatus(id, isActive)
    }

    suspend fun insertCompanion(companion: AiCompanion) = withContext(Dispatchers.IO) {
        companionDao.insertCompanion(companion)
    }

    suspend fun updateCompanionMood(id: Long, mood: String) = withContext(Dispatchers.IO) {
        companionDao.updateCompanionMood(id, mood)
    }

    suspend fun updateRelationshipScore(id: Long, score: Int) = withContext(Dispatchers.IO) {
        companionDao.updateRelationshipScore(id, score)
    }

    suspend fun prepopulateCompanionsIfEmpty() = withContext(Dispatchers.IO) {
        if (companionDao.getCompanionCount() == 0) {
            val defaults = listOf(
                AiCompanion(
                    name = "Abe Wan",
                    dialect = "Kelantan",
                    role = "Bapa",
                    avatarUrl = "https://drive.google.com/drive/folders/1s2dPTg76Lj2qgUicmX_HoskvYzFjRgSf/avatar_father.png",
                    backgroundStory = "Seorang bapa penyayang, mesra, yang berasal dari Kota Bharu. Suka beri nasihat murni dan petua hidup.",
                    relationshipScore = 100,
                    currentMood = "Gembira"
                ),
                AiCompanion(
                    name = "Mek Nab",
                    dialect = "Kedah",
                    role = "Ibu",
                    avatarUrl = "https://drive.google.com/drive/folders/1s2dPTg76Lj2qgUicmX_HoskvYzFjRgSf/avatar_mother.png",
                    backgroundStory = "Seorang ibu yang sangat peramah, kuat bebel tapi penyayang dari Alor Setar. Suka memasak dan berkongsi cerita lucu.",
                    relationshipScore = 100,
                    currentMood = "Selesa"
                ),
                AiCompanion(
                    name = "Che Soh",
                    dialect = "Terengganu",
                    role = "Abang",
                    avatarUrl = "https://drive.google.com/drive/folders/1s2dPTg76Lj2qgUicmX_HoskvYzFjRgSf/avatar_brother.png",
                    backgroundStory = "Abang sulung yang ceria, santai dari Kuala Terengganu. Gemar memancing ikan, melawak dan suka tolong adik-adik.",
                    relationshipScore = 100,
                    currentMood = "Ceria"
                ),
                AiCompanion(
                    name = "Mek Na",
                    dialect = "Kelantan",
                    role = "Adik",
                    avatarUrl = "https://drive.google.com/drive/folders/1s2dPTg76Lj2qgUicmX_HoskvYzFjRgSf/avatar_sister.png",
                    backgroundStory = "Adik bongsu yang manja dan kreatif. Sentiasa ceria dan suka meminta pendapat tentang pelajaran serta kehidupan.",
                    relationshipScore = 100,
                    currentMood = "Manja"
                )
            )
            for (companion in defaults) {
                companionDao.insertCompanion(companion)
            }
        }
    }

    suspend fun prepopulateLogsIfEmpty() = withContext(Dispatchers.IO) {
        if (logDao.getLogCount() == 0) {
            val sampleLogs = listOf(
                ObservationLog(
                    type = "Emotion",
                    label = "Gembira / Ceria",
                    confidence = 95,
                    rawNotes = "Suasana keluarga sangat mesra dan riang semasa karaoke duet menyanyikan lagu klasik kegemaran Abe Wan."
                ),
                ObservationLog(
                    type = "Behavior",
                    label = "Makan Bersama & Bersembang",
                    confidence = 90,
                    rawNotes = "Sesi makan malam beramai-ramai. Pasukan bekerjasama menyusun meja hidangan dengan lancar."
                ),
                ObservationLog(
                    type = "Animal",
                    label = "Kucing Peliharaan (Comel)",
                    confidence = 88,
                    rawNotes = "Kucing oren bernama Jinny bermanja mencari perhatian di hadapan kamera semasa panggilan video kumpulan."
                )
            )
            for (log in sampleLogs) {
                logDao.insertLog(log)
            }
        }
    }
}
