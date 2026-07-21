package com.example.network

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val TAG = "GeminiClient"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Call Gemini to generate text content or chat response.
     */
    suspend fun generateResponse(
        prompt: String,
        systemInstruction: String? = null,
        model: String = "gemini-3.5-flash"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder.")
            return@withContext "Ralat: Kunci API Gemini (GEMINI_API_KEY) belum dikonfigurasikan di dalam panel Secrets AI Studio."
        }

        val contents = listOf(Content(parts = listOf(Part(text = prompt))))
        val systemContent = systemInstruction?.let {
            Content(parts = listOf(Part(text = it)))
        }

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = systemContent
        )

        try {
            val response = service.generateContent(model, apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Tiada maklum balas daripada AI."
        } catch (e: Exception) {
            Log.e(TAG, "Exception during generateResponse: ", e)
            "Ralat rangkaian atau konfigurasi: ${e.localizedMessage ?: "Sila cuba lagi."}"
        }
    }

    /**
     * Send multimodal input (Base64 JPEG image + Prompt text) to Gemini.
     */
    suspend fun analyzeImage(
        bitmapBase64: String,
        prompt: String,
        model: String = "gemini-3.5-flash",
        mimeType: String = "image/jpeg"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder.")
            return@withContext "Ralat: Kunci API Gemini belum dikonfigurasikan di panel Secrets."
        }

        val contents = listOf(
            Content(
                parts = listOf(
                    Part(text = prompt),
                    Part(inlineData = InlineData(mimeType = mimeType, data = bitmapBase64))
                )
            )
        )

        val request = GenerateContentRequest(contents = contents)

        try {
            val response = service.generateContent(model, apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Tiada maklum balas analisis."
        } catch (e: Exception) {
            Log.e(TAG, "Exception during analyzeImage: ", e)
            "Ralat analisis imej: ${e.localizedMessage ?: "Sila cuba lagi."}"
        }
    }

    /**
     * Generate an image using Gemini 3 Pro Image model.
     * Supports size choices (1K, 2K, 4K) and aspect ratios.
     */
    suspend fun generateImage(
        prompt: String,
        imageSize: String = "1K", // "1K", "2K", "4K"
        aspectRatio: String = "1:1",
        model: String = "gemini-3-pro-image-preview"
    ): Bitmap? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder.")
            return@withContext null
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                imageConfig = ImageConfig(aspectRatio = aspectRatio, imageSize = imageSize),
                responseModalities = listOf("TEXT", "IMAGE")
            )
        )

        try {
            val response = service.generateContent(model, apiKey, request)
            // Look for any part containing inlineData in candidates
            val candidates = response.candidates ?: return@withContext null
            for (cand in candidates) {
                val parts = cand.content?.parts ?: continue
                for (part in parts) {
                    val inlineData = part.inlineData ?: continue
                    if (inlineData.data.isNotEmpty()) {
                        val imageBytes = Base64.decode(inlineData.data, Base64.DEFAULT)
                        return@withContext android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error generating image: ", e)
            null
        }
    }
}
