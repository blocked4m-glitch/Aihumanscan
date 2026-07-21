package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observation_logs")
data class ObservationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "Emotion", "Behavior", "Animal"
    val label: String,
    val confidence: Int,
    val rawNotes: String,
    val imageUri: String? = null
)

@Entity(tableName = "camera_configs")
data class CameraConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "Internal", "USB", "RTSP", "ESP32-CAM"
    val url: String,
    val isActive: Boolean = false
)

@Entity(tableName = "ai_companions")
data class AiCompanion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dialect: String, // "Kelantan", "Kedah", "Terengganu"
    val role: String, // "Bapa", "Ibu", "Abang", "Kakak", "Adik", "Rakan"
    val avatarUrl: String,
    val backgroundStory: String,
    val relationshipScore: Int = 100,
    val currentMood: String = "Gembira" // "Gembira", "Selesa", "Ceria", "Manja"
)
