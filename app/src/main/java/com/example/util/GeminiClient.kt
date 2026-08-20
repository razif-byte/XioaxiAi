package com.example.util

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"
    }

    /**
     * Ask Gemini to handle fallback conversations in selected language (Malay, English, Kelantan dialect).
     */
    suspend fun getConversationalResponse(
        userMessage: String,
        modelName: String = "gemini-3.5-flash",
        languageId: String = "ms"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is placeholder or missing!")
            return@withContext if (languageId == "en") "Error: Please configure your GEMINI_API_KEY in the Secrets panel." else "Ralat: Sila konfigurasikan GEMINI_API_KEY anda di panel Secrets."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        try {
            val systemPrompt = when (languageId.lowercase()) {
                "en" -> "You are Xiaoxi, a friendly, smart, and ultra-fast portable AI voice assistant speaking in natural, clear English. Provide spontaneous, concise, and helpful answers (maximum 2 sentences)."
                "kelantan" -> "Anda adalah Xiaoxi, asisten AI pintar portable yang fasih bertutur dalam loghat Kelantan (Baso Kelate) yang tulen, mesra, bersahaja dan menghiburkan. Selitkan kosa kata & partikel Kelantan seperti 'Boh', 'Beres', 'Molek', 'Ambo', 'Demo', 'Loni', 'Guano', 'Hok ni'. Berikan jawapan padat maksimum 2 ayat."
                else -> "Anda adalah Xiaoxi, asisten kecerdasan buatan (AI) portable yang bercakap dalam Bahasa Melayu yang mesra, ringkas, dan spontan. Berikan respons yang mesra, padat (maksimum 2 ayat) dan bersahaja kepada soalan pengguna."
            }

            // Build the system instruction part
            val systemInstructionJson = JSONObject().put("parts", JSONArray().put(
                JSONObject().put("text", systemPrompt)
            ))

            // Build the standard content structure
            val contentsJson = JSONArray().put(
                JSONObject().put("role", "user").put("parts", JSONArray().put(
                    JSONObject().put("text", userMessage)
                ))
            )

            val payload = JSONObject().apply {
                put("contents", contentsJson)
                put("systemInstruction", systemInstructionJson)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 120)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini API Error: Code ${response.code}, Body: $errorBody")
                    return@withContext "Maaf, Xiaoxi mengalami sedikit ralat rangkaian ($response.code)."
                }

                val responseBody = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "Tiada respons.")
                        }
                    }
                }
                "Maaf, saya tidak dapat memproses maklumat tersebut."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini Call: ${e.message}")
            "Maaf, sambungan rangkaian Xiaoxi terputus: ${e.localizedMessage}"
        }
    }
}
