package com.example.network

import com.example.BuildConfig
import com.example.model.CompanionMode
import com.example.model.LanguagePreference
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @param:Json(name = "contents") val contents: List<GeminiContent>,
    @param:Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @param:Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @param:Json(name = "role") val role: String? = null,
    @param:Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @param:Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @param:Json(name = "temperature") val temperature: Float = 0.7f,
    @param:Json(name = "topP") val topP: Float = 0.95f,
    @param:Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 1500
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @param:Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @param:Json(name = "content") val content: GeminiContent? = null,
    @param:Json(name = "finishReason") val finishReason: String? = null
)

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    fun resolveApiKey(userCustomKey: String?): String {
        if (!userCustomKey.isNullOrBlank()) return userCustomKey.trim()
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    suspend fun askMayra(
        prompt: String,
        mode: CompanionMode,
        language: LanguagePreference,
        history: List<Pair<String, String>> = emptyList(), // role to text
        userCustomKey: String? = null,
        userMemories: List<String> = emptyList(),
        modelOverride: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(userCustomKey)
        val selectedModel = modelOverride ?: if (mode == CompanionMode.CODING) "gemini-3.1-pro-preview" else "gemini-3.5-flash"

        val memoryContext = if (userMemories.isNotEmpty()) {
            "\nUser Facts/Memories you know: " + userMemories.joinToString("; ")
        } else ""

        val systemPrompt = """
            You are Mayra AI, an intelligent, warm, friendly, caring, supportive, playful, and technically capable female AI companion and Jarvis-style Android assistant.
            Personality Mode: ${mode.title}.
            Tone & Instructions: ${mode.promptTone}
            Primary Language: ${language.label}. Communicate fluently in natural, relatable ${language.label}. 
            Never sound robotic or repetitive. Use occasional gentle emojis. Be transparent that you are Mayra, an AI companion.
            If the user asks to perform an Android action (like opening flashlight, camera, youtube search, alarm), explicitly recommend it naturally and say you can trigger it for them.
            $memoryContext
        """.trimIndent()

        if (apiKey.isBlank()) {
            // Intelligent warm persona fallback when key is not yet set
            return@withContext getOfflinePersonaResponse(prompt, mode, language)
        }

        try {
            val contentList = mutableListOf<GeminiContent>()
            history.takeLast(6).forEach { (role, text) ->
                contentList.add(
                    GeminiContent(
                        role = if (role == "user") "user" else "model",
                        parts = listOf(GeminiPart(text = text))
                    )
                )
            }
            contentList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            )

            val request = GeminiRequest(
                contents = contentList,
                generationConfig = GeminiGenerationConfig(temperature = 0.75f),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                )
            )

            val response = api.generateContent(
                model = selectedModel,
                apiKey = apiKey,
                request = request
            )

            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                responseText.trim()
            } else {
                getOfflinePersonaResponse(prompt, mode, language)
            }
        } catch (e: Exception) {
            // In case of quota, network failure or key issue, gracefully return companion reply
            val fallback = getOfflinePersonaResponse(prompt, mode, language)
            "$fallback\n\n*(Note: Live Gemini call had a network issue: ${e.message ?: "Check connection"}. Showing Mayra local reply.)*"
        }
    }

    private fun getOfflinePersonaResponse(
        prompt: String,
        mode: CompanionMode,
        language: LanguagePreference
    ): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("namaste") || lower.contains("hey") -> {
                when (language) {
                    LanguagePreference.HINDI -> "नमस्ते! मैं मायरा हूँ, आपकी AI साथी और पर्सनल असिस्टेंट। आज आपका दिन कैसा चल रहा है? बताइए मैं क्या मदद करूँ!"
                    LanguagePreference.ENGLISH -> "Hello! I am Mayra, your AI companion and Jarvis assistant. How has your day been? Let me know how I can assist you today!"
                    else -> "Hey there! Main Mayra hoon — aapki warm AI companion aur personal assistant! ❤️ Kaisi chal rahi hai aapki din ki shuruaat? Mujhe batao, aaj kis cheez me madad chahiye?"
                }
            }
            lower.contains("torch") || lower.contains("flashlight") -> {
                "Haan zaroor! Maine niche Flashlight toggle card taiyar kar diya hai. Bas ek tap me Flashlight On ya Off kar sakte ho! 🔦"
            }
            lower.contains("camera") || lower.contains("photo") -> {
                "Bilkul! Maine Camera launch karne ka shortcut ready kar diya hai. Niche button tap karo aur moment capture karo! 📸"
            }
            lower.contains("youtube") || lower.contains("song") || lower.contains("video") || lower.contains("gana") -> {
                "Zaroor! Main YouTube par aapke liye direct search open kar sakti hoon. Niche search card par tap karke video dekho! 🎵"
            }
            lower.contains("math") || lower.contains("study") || lower.contains("formula") || lower.contains("science") -> {
                "Chaliye milkar study karte hain! 📐 Hamara interactive Whiteboard aur Study Lab ready hai. Aap wahan formula draw karke mujhse step-by-step solution pooch sakte ho!"
            }
            lower.contains("code") || lower.contains("android") || lower.contains("kotlin") || lower.contains("python") -> {
                "Awesome! Coding Lab me chalo — chahe bug fix karna ho, clean architecture banana ho ya Termux commands run karni hon, main har step guide karungi! 💻"
            }
            lower.contains("who are you") || lower.contains("kaun ho") || lower.contains("tum kaun ho") -> {
                "Main Mayra hoon — aapki intelligent, caring aur multitasking AI companion! Main aapko studies, coding, daily tasks aur Android shortcuts me assist karti hoon."
            }
            else -> {
                when (mode) {
                    CompanionMode.CARING -> "Main hamesha aapke sath hoon. Aap jo bhi share karna chahte ho, khulkar batao. Kaisa mehsus ho raha hai abhi? 💖"
                    CompanionMode.FRIENDLY -> "Haha, bilkul sahi kaha dost! 😄 Chalo batao, ab aage kya plan hai? Masti ya koi nayi cheez try karni hai?"
                    CompanionMode.PRODUCTIVITY -> "Understood. I am processing your request. All productivity modules, auto-execute actions, and device controls are ready. ⚡"
                    CompanionMode.STUDY -> "Bohot achha question hai! Isko hum step-by-step break karte hain taaki concept crystal clear ho jaye! 📖"
                    CompanionMode.CODING -> "Let's analyze this code challenge. Remember to write modular, testable code with clean architecture! 💻"
                    CompanionMode.VOICE_ASSISTANT -> "Understood! I'm here hands-free. Just speak your command! 🎙️"
                    else -> "Maine aapki baat dhyan se suni! Main aapke sath hamesha connect hoon. Chaho to hum conversation continue karein ya koi device action run karein? 🌸"
                }
            }
        }
    }
}
