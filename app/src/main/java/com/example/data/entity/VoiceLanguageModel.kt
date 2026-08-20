package com.example.data.entity

enum class VoiceLanguage(
    val id: String,
    val displayName: String,
    val nativeName: String,
    val localeTag: String,
    val flag: String,
    val description: String,
    val defaultSampleCommand: String,
    val sampleCommands: List<String>,
    val modelSizeMb: Float
) {
    MALAY(
        id = "ms",
        displayName = "Bahasa Melayu",
        nativeName = "Bahasa Melayu (Standard)",
        localeTag = "ms-MY",
        flag = "🇲🇾",
        description = "Model standard Bahasa Melayu rasmi untuk pengiktirafan arahan suara dan perbualan AI pintar.",
        defaultSampleCommand = "buka VLC / mainkan muzik",
        sampleCommands = listOf("buka VLC", "buka whatsapp", "buka kamera", "mainkan lagu", "menyanyi", "apa khabar hari ini?"),
        modelSizeMb = 14.2f
    ),
    ENGLISH(
        id = "en",
        displayName = "English",
        nativeName = "English (US / Global)",
        localeTag = "en-US",
        flag = "🇬🇧",
        description = "Neural speech recognition and natural conversational AI engine for English commands and chat.",
        defaultSampleCommand = "open VLC / play music",
        sampleCommands = listOf("open VLC", "open whatsapp", "open camera", "play music", "sing a song", "what can you do?"),
        modelSizeMb = 28.5f
    ),
    KELANTAN(
        id = "kelantan",
        displayName = "Dialek Kelantan",
        nativeName = "Baso Kelate (Loghat Pantai Timur)",
        localeTag = "ms-MY",
        flag = "🌾",
        description = "Model AI khas loghat Kelantan (Baso Kelate) lengkap dengan nahu tempatan, kosa kata & loghat tulen.",
        defaultSampleCommand = "buko VLC / main lagu",
        sampleCommands = listOf("buko VLC", "buko wasak", "buko kemera", "main lagu", "nyanyi lagu sebutir", "guano cerito arini boh?"),
        modelSizeMb = 18.9f
    );

    companion object {
        fun fromId(id: String): VoiceLanguage {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: MALAY
        }
    }
}

data class VoiceModelPackage(
    val language: VoiceLanguage,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadStatusText: String = "",
    val version: String = "v2.5.0",
    val acousticEngine: String = "Neural Conformer Lite",
    val lastUpdated: String = "2026-08-19"
)
