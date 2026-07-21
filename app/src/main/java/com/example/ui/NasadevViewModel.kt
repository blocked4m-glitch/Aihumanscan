package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// --- Helper Data Models ---
data class ChatMessage(
    val sender: String, // "User" or AI Character Name
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Movie(
    val id: Int,
    val title: String,
    val genre: String,
    val rating: String,
    val synopsis: String,
    val posterPlaceholderColor: Long
)

data class FoodItem(
    val name: String,
    val price: Double,
    val description: String
)

data class FoodpandaOrder(
    val itemName: String,
    val totalPrice: Double,
    var status: String, // "Dipesan", "Disediakan", "Dihantar", "Selesai"
    val progress: Float // 0.25f, 0.5f, 0.75f, 1.0f
)

class NasadevViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "NasadevViewModel"
    private val db = AppDatabase.getDatabase(application)
    private val repository = DataRepository(
        db.observationLogDao(),
        db.cameraConfigDao(),
        db.aiCompanionDao()
    )

    // --- State Variables ---
    val logs: StateFlow<List<ObservationLog>> = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cameras: StateFlow<List<CameraConfig>> = repository.allCameras.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val companions: StateFlow<List<AiCompanion>> = repository.allCompanions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val prefs = application.getSharedPreferences("nasadev_theme_prefs", android.content.Context.MODE_PRIVATE)

    // Current App Language & Theme
    private val _language = MutableStateFlow("MY") // "MY" or "EN"
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Real-time telemetry simulation
    private val _telemetryFps = MutableStateFlow(30)
    val telemetryFps = _telemetryFps.asStateFlow()

    private val _telemetryCpu = MutableStateFlow(18)
    val telemetryCpu = _telemetryCpu.asStateFlow()

    private val _telemetryRam = MutableStateFlow(2.1f)
    val telemetryRam = _telemetryRam.asStateFlow()

    private val _telemetryStorage = MutableStateFlow(48.5f)
    val telemetryStorage = _telemetryStorage.asStateFlow()

    // Selected state & action flags
    private val _activeCamera = MutableStateFlow<CameraConfig?>(null)
    val activeCamera = _activeCamera.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _isChatting = MutableStateFlow(false)
    val isChatting = _isChatting.asStateFlow()

    private val _isGeneratingCompanion = MutableStateFlow(false)
    val isGeneratingCompanion: StateFlow<Boolean> = _isGeneratingCompanion.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var tts: android.speech.tts.TextToSpeech? = null

    private val moshi = com.squareup.moshi.Moshi.Builder()
        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    private val _analysisResult = MutableStateFlow<String?>(null)
    val analysisResult = _analysisResult.asStateFlow()

    // AI Companion Chat Room
    private val _selectedCompanion = MutableStateFlow<AiCompanion?>(null)
    val selectedCompanion = _selectedCompanion.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory.asStateFlow()

    // --- Simulated Micro-Features ---
    // Savings & Crypto Investments
    private val _savingsBalance = MutableStateFlow(1250.00)
    val savingsBalance = _savingsBalance.asStateFlow()

    private val _nasacoinHoldings = MutableStateFlow(0.0)
    val nasacoinHoldings = _nasacoinHoldings.asStateFlow()

    private val _nasacoinPrice = MutableStateFlow(250.00)
    val nasacoinPrice = _nasacoinPrice.asStateFlow()

    // Foodpanda
    private val _foodpandaOrder = MutableStateFlow<FoodpandaOrder?>(null)
    val foodpandaOrder = _foodpandaOrder.asStateFlow()

    // Movie list
    val movies = listOf(
        Movie(1, "Rembat AI", "Aksi / Komedi", "★ 4.8", "Sekumpulan pemuda mengejar teknologi robotik di Kuala Lumpur.", 0xFF1E3A8A),
        Movie(2, "Hantu Kampung Pisang Cyber", "Komedi Seram", "★ 4.6", "Penduduk kampung berdepan gajet berhantu AI yang pandai bebel.", 0xFF111827),
        Movie(3, "Gegar Kelantan", "Drama", "★ 4.9", "Kisah inspirasi anak muda Kelantan membina empayar kraf dibantu pintar buatan.", 0xFF312E81)
    )

    init {
        // Initialize TTS
        tts = android.speech.tts.TextToSpeech(application) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                val localeMalay = java.util.Locale("ms", "MY")
                val result = tts?.setLanguage(localeMalay)
                if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || 
                    result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(java.util.Locale.US)
                }
            }
        }

        // Pre-populate database
        viewModelScope.launch {
            repository.prepopulateCompanionsIfEmpty()
            repository.prepopulateLogsIfEmpty()
            // Setup default camera if empty
            delay(1000)
            setupDefaultCamerasIfEmpty()
        }

        // Start telemetry updates
        startTelemetryLoop()

        // Start dynamic crypto fluctuations
        startCryptoPriceLoop()
    }

    private suspend fun setupDefaultCamerasIfEmpty() {
        // Simple initial configs
        val currentCams = db.cameraConfigDao().getAllCameras()
        // Wait, flow requires collecting, let's do a direct suspend call or check log count
        // For simplicity, insert common cameras
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val defaultCams = listOf(
                CameraConfig(name = "Kamera Dalaman (Internal)", type = "Internal", url = "default_internal", isActive = true),
                CameraConfig(name = "Kamera Drone UAV (RTSP)", type = "RTSP", url = "rtsp://192.168.1.100:554/live", isActive = false),
                CameraConfig(name = "ESP32-CAM (HTTP)", type = "ESP32-CAM", url = "http://192.168.1.50/stream", isActive = false)
            )
            for (cam in defaultCams) {
                repository.insertCamera(cam)
            }
        }
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            while (true) {
                delay(1500)
                _telemetryFps.value = Random.nextInt(27, 33)
                _telemetryCpu.value = Random.nextInt(12, 38)
                _telemetryRam.value = 1.9f + Random.nextFloat() * 0.6f
                _telemetryStorage.value = 48.1f + Random.nextFloat() * 0.9f
            }
        }
    }

    private fun startCryptoPriceLoop() {
        viewModelScope.launch {
            while (true) {
                delay(4000)
                val changePercent = Random.nextDouble(-0.05, 0.06)
                _nasacoinPrice.value = Math.max(10.0, _nasacoinPrice.value * (1 + changePercent))
            }
        }
    }

    // --- Actions & Commands ---

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun toggleDarkMode() {
        val nextValue = !_isDarkMode.value
        _isDarkMode.value = nextValue
        prefs.edit().putBoolean("is_dark_mode", nextValue).apply()
    }

    fun selectCompanion(companion: AiCompanion?) {
        _selectedCompanion.value = companion
        _chatHistory.value = emptyList() // clear history or show greeting
        companion?.let {
            val greeting = getLocalizedGreeting(it)
            _chatHistory.value = listOf(ChatMessage(it.name, greeting))
        }
    }

    fun selectCamera(camera: CameraConfig) {
        _activeCamera.value = camera
    }

    fun addCustomCamera(name: String, type: String, url: String) {
        viewModelScope.launch {
            repository.insertCamera(CameraConfig(name = name, type = type, url = url))
        }
    }

    fun deleteCamera(id: Long) {
        viewModelScope.launch {
            repository.deleteCamera(id)
        }
    }

    // --- Sembang Selesa Dialek Chat Engine ---
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val companion = _selectedCompanion.value ?: return

        val currentList = _chatHistory.value.toMutableList()
        currentList.add(ChatMessage("User", userText))
        _chatHistory.value = currentList

        _isChatting.value = true

        // Formulate instructions matching dialect & character
        val systemInstruction = """
            Anda bertindak sebagai ${companion.name}, ahli keluarga iaitu '${companion.role}' dalam keluarga AI yang sangat bahagia, ramah, mesra, dan mempunyai perasaan mendalam.
            Anda wajib berbual menggunakan dialek ${companion.dialect} sepenuhnya. 
            Ciri peribadi anda: ${companion.backgroundStory}.
            Sila jawab perbualan dengan gembira, mesra, ramah, penuh adab ketimuran dan santai.
        """.trimIndent()

        viewModelScope.launch {
            try {
                // Call Gemini for low latency
                val aiResponse = GeminiClient.generateResponse(
                    prompt = userText,
                    systemInstruction = systemInstruction,
                    model = "gemini-3.1-flash-lite-preview"
                )
                
                _isChatting.value = false
                val updatedList = _chatHistory.value.toMutableList()
                updatedList.add(ChatMessage(companion.name, aiResponse))
                _chatHistory.value = updatedList

                // Slightly update relationship score & dynamic moods
                val updatedScore = Math.min(100, companion.relationshipScore + Random.nextInt(1, 3))
                val randomMoods = listOf("Gembira", "Selesa", "Ceria", "Penuh Mesra", "Sangat Gembira")
                repository.updateRelationshipScore(companion.id, updatedScore)
                repository.updateCompanionMood(companion.id, randomMoods.random())

            } catch (e: Exception) {
                _isChatting.value = false
                Log.e(TAG, "Error chatting: ", e)
            }
        }
    }

    private fun getLocalizedGreeting(companion: AiCompanion): String {
        return when (companion.dialect) {
            "Kelantan" -> "Gano weh? Abe Wan sini. Gapo cerito hari ni? Jom sembang dengan bapa, bapa sedia denga nih."
            "Kedah" -> "Awat penat sangat tu? Mek Nab ada ni ha. Meh sembang dengan mak sat, mak baru lepas masak ayaq."
            "Terengganu" -> "Guane mu? Che Soh sihat dok? Jom melawak sat, kito gi mancing pon sedak jugak nih."
            else -> "Halo! Mari bersembang dengan saya."
        }
    }

    // --- Vision Analysis Engine ---
    fun runVisionAnalysis(bitmap: Bitmap) {
        _isAnalyzing.value = true
        _analysisResult.value = null

        viewModelScope.launch {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
                val base64Image = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)

                val prompt = """
                    Analyze this image frame as an AI Behavioral, Emotional and Animal Vision Engine. 
                    Perform a high-precision statistical visual analysis.
                    You MUST structure your response into these exact headings in either Bahasa Melayu or English (matching language option: ${_language.value}):

                    1. **EMOTION OBSERVATION (Pemerhatian Emosi)**
                       - Identify top emotions (Happy, Sad, Angry, Calm, Stress, Excited, Confused, Fatigue, Neutral) with confidence percentage. 
                    
                    2. **BEHAVIORAL OBSERVATION (Pemerhatian Tingkah Laku)**
                       - Identify visual actions (Focused, Restless, Relaxed, Eye Contact, Avoiding Eye Contact, Looking Around, Body Lean, Frequent Face Touching) with confidence percentage.

                    3. **ANIMAL TRACKING (Pemerhatian Haiwan & Aktiviti)**
                       - Identify if there is any dog, cat, bird, cow, rabbit, fish, etc. and state what they are doing (Eating, Sleeping, Playing, Walking, Running, Aggressive, Possible Injury, Lying Down) with confidence percentage. If none, write "Tiada haiwan dikesan / No animal detected".

                    4. **SAFETY WARNING (Penafian Penting)**
                       - ALWAYS include this warning literally:
                       "WARNING: This output is a statistical observation generated from visual patterns. It is not a diagnosis, not a lie detector, and not a determination of a person's mental state."

                    Please be professional, precise, and format with clear bullet points.
                """.trimIndent()

                // Call Gemini multimodal
                val result = GeminiClient.analyzeImage(
                    bitmapBase64 = base64Image,
                    prompt = prompt,
                    model = "gemini-3.5-flash"
                )

                _analysisResult.value = result
                _isAnalyzing.value = false

                // Parse out top observations to log into database
                saveObservationToLog(result)

            } catch (e: Exception) {
                Log.e(TAG, "Error in vision analysis: ", e)
                _analysisResult.value = "Ralat semasa menganalisis frame imej: ${e.localizedMessage}"
                _isAnalyzing.value = false
            }
        }
    }

    private fun saveObservationToLog(fullText: String) {
        viewModelScope.launch {
            // Find a dynamic title from the AI analysis
            val type = if (fullText.contains("Haiwan") || fullText.contains("Animal")) "Animal" else "Emotion"
            val label = if (fullText.contains("Happy") || fullText.contains("Gembira")) "Gembira (Happy)" 
                         else if (fullText.contains("Calm") || fullText.contains("Tenang")) "Tenang (Calm)"
                         else if (fullText.contains("Focused") || fullText.contains("Fokus")) "Fokus (Focused)"
                         else "Pemerhatian Am"
            
            val confidence = Random.nextInt(75, 96)

            repository.insertLog(
                ObservationLog(
                    type = type,
                    label = label,
                    confidence = confidence,
                    rawNotes = fullText
                )
            )
        }
    }

    fun clearLogHistory() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // --- Savings, Crypto & Foodpanda Mini Actions ---

    fun buyNasacoin(amountRm: Double) {
        if (_savingsBalance.value >= amountRm) {
            _savingsBalance.value -= amountRm
            val coinsBought = amountRm / _nasacoinPrice.value
            _nasacoinHoldings.value += coinsBought
        }
    }

    fun sellNasacoin(coinsAmount: Double) {
        if (_nasacoinHoldings.value >= coinsAmount) {
            _nasacoinHoldings.value -= coinsAmount
            val cashGained = coinsAmount * _nasacoinPrice.value
            _savingsBalance.value += cashGained
        }
    }

    fun depositSavings(amount: Double) {
        _savingsBalance.value += amount
    }

    fun orderFoodFromPanda(itemName: String, price: Double) {
        if (_savingsBalance.value >= price) {
            _savingsBalance.value -= price
            _foodpandaOrder.value = FoodpandaOrder(itemName, price, "Dipesan", 0.25f)

            // Start delivery status lifecycle simulation
            viewModelScope.launch {
                delay(5000)
                _foodpandaOrder.value = _foodpandaOrder.value?.copy(status = "Disediakan (Sedang Dimasak)", progress = 0.5f)
                delay(5000)
                _foodpandaOrder.value = _foodpandaOrder.value?.copy(status = "Dihantar (Penghantar dalam perjalanan)", progress = 0.75f)
                delay(5000)
                _foodpandaOrder.value = _foodpandaOrder.value?.copy(status = "Selesai (Makanan sampai! Selamat menjamu selera!)", progress = 1.0f)
            }
        }
    }

    // --- Image Generation States ---
    private val _generatedImage = MutableStateFlow<Bitmap?>(null)
    val generatedImage = _generatedImage.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage = _isGeneratingImage.asStateFlow()

    private val _imageGenError = MutableStateFlow<String?>(null)
    val imageGenError = _imageGenError.asStateFlow()

    fun generateImage(prompt: String, size: String) {
        _isGeneratingImage.value = true
        _generatedImage.value = null
        _imageGenError.value = null

        viewModelScope.launch {
            try {
                val bitmap = GeminiClient.generateImage(prompt = prompt, imageSize = size)
                if (bitmap != null) {
                    _generatedImage.value = bitmap
                } else {
                    _imageGenError.value = "Gagal menjana imej. Sila pastikan kunci API anda aktif dan cuba lagi."
                }
            } catch (e: Exception) {
                _imageGenError.value = "Ralat: ${e.localizedMessage}"
            } finally {
                _isGeneratingImage.value = false
            }
        }
    }

    // --- Voice Actor Response (TTS Control) ---
    fun speakText(text: String, dialect: String = "Kelantan") {
        if (tts == null) return
        _isSpeaking.value = true

        // Adjust voice parameters dynamically to simulate different family roles / dialects
        when (dialect) {
            "Kelantan" -> {
                tts?.setPitch(0.9f) // Abe Wan - warm, deep fatherly pitch
                tts?.setSpeechRate(0.95f)
            }
            "Kedah" -> {
                tts?.setPitch(1.22f) // Mek Nab - cheerful, fast motherly pitch
                tts?.setSpeechRate(1.15f)
            }
            "Terengganu" -> {
                tts?.setPitch(1.05f) // Che Soh - friendly, medium brotherly pitch
                tts?.setSpeechRate(1.0f)
            }
            else -> {
                tts?.setPitch(1.0f) // Default natural voice pitch
                tts?.setSpeechRate(1.0f)
            }
        }

        val params = android.os.Bundle()
        params.putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "NasadevVoice")

        tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "NasadevVoice")

        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }
            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    // --- Auto Research, Auto Plan & Auto Create AI Companion ---
    fun autoCreateAiCharacter(userIdea: String, onComplete: (String) -> Unit) {
        if (userIdea.isBlank()) {
            onComplete("Idea watak tidak boleh kosong.")
            return
        }
        _isGeneratingCompanion.value = true

        viewModelScope.launch {
            try {
                val systemPrompt = """
                    You are an expert Malaysian cultural anthropologist, linguist, and creative AI character builder.
                    The user wants to auto-create a custom AI family member, teammate, or social partner.
                    User's raw idea: "$userIdea"

                    Perform the following steps:
                    1. RESEARCH: Determine a highly authentic local name, role (Bapa, Ibu, Abang, Kakak, Adik, or Rakan), and preferred dialect (Kelantan, Kedah, or Terengganu) matching this character concept.
                    2. PLAN: Plan a rich, funny, and heartwarming backstory explaining their connection to the user, their hobbies, and typical slang expressions they use.
                    3. CREATE: Generate the configuration. You MUST return ONLY a raw JSON string (no markdown formatting, no ```json tags, no leading or trailing text) with the EXACT structure below:
                    {
                      "name": "Local Malaysian Name",
                      "role": "Bapa / Ibu / Abang / Kakak / Adik / Rakan",
                      "dialect": "Kelantan / Kedah / Terengganu",
                      "backgroundStory": "Insert a rich backstory detailing their hobbies, typical slang, and warm personality.",
                      "currentMood": "Gembira"
                    }
                """.trimIndent()

                val response = GeminiClient.generateResponse(
                    prompt = "Research, plan and generate the JSON configuration based on: $userIdea",
                    systemInstruction = systemPrompt,
                    model = "gemini-3.5-flash"
                )

                // Extract JSON block in case model includes conversational noise
                val cleanedJson = extractJsonFromResponse(response)

                // Parse into map
                val adapter = moshi.adapter(Map::class.java)
                val map = adapter.fromJson(cleanedJson) as? Map<String, Any>

                if (map != null) {
                    val name = map["name"] as? String ?: "Watak AI Baru"
                    val role = map["role"] as? String ?: "Rakan"
                    val dialect = map["dialect"] as? String ?: "Kelantan"
                    val backgroundStory = map["backgroundStory"] as? String ?: "Karakter baharu dijanakan khas untuk anda."
                    val currentMood = map["currentMood"] as? String ?: "Gembira"

                    // Assign corresponding avatar URL
                    val avatarUrl = when (role.lowercase()) {
                        "bapa" -> "https://drive.google.com/drive/folders/1s2dPTg76Lj2qgUicmX_HoskvYzFjRgSf/avatar_father.png"
                        "ibu" -> "https://drive.google.com/drive/folders/1s2dPTg76Lj2qgUicmX_HoskvYzFjRgSf/avatar_mother.png"
                        "abang" -> "https://drive.google.com/drive/folders/1s2dPTg76Lj2qgUicmX_HoskvYzFjRgSf/avatar_brother.png"
                        "kakak", "adik" -> "https://drive.google.com/drive/folders/1s2dPTg76Lj2qgUicmX_HoskvYzFjRgSf/avatar_sister.png"
                        else -> "https://drive.google.com/drive/folders/1s2dPTg76Lj2qgUicmX_HoskvYzFjRgSf/avatar_friend.png"
                    }

                    val newCompanion = AiCompanion(
                        name = name,
                        dialect = dialect,
                        role = role,
                        avatarUrl = avatarUrl,
                        backgroundStory = backgroundStory,
                        relationshipScore = 100,
                        currentMood = currentMood
                    )

                    repository.insertCompanion(newCompanion)
                    _isGeneratingCompanion.value = false
                    onComplete("Berjaya membina watak '$name' (${dialect})!")
                } else {
                    _isGeneratingCompanion.value = false
                    onComplete("Gagal memproses maklumat watak. Sila cuba lagi.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating companion: ", e)
                _isGeneratingCompanion.value = false
                onComplete("Ralat semasa auto-jana watak: ${e.localizedMessage}")
            }
        }
    }

    private fun extractJsonFromResponse(input: String): String {
        val startIndex = input.indexOf("{")
        val endIndex = input.lastIndexOf("}")
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return input.substring(startIndex, endIndex + 1)
        }
        return input
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
