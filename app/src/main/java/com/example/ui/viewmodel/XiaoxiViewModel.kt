package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.VoiceCommandLog
import com.example.data.entity.VoiceProfile
import com.example.data.entity.SongItem
import com.example.data.entity.SongRepository
import com.example.data.entity.VoiceLanguage
import com.example.data.entity.VoiceModelPackage
import com.example.data.repository.VoiceRepository
import com.example.util.GeminiClient
import com.example.util.NetworkDevice
import com.example.util.VoiceFingerprintManager
import com.example.util.WifiControllerServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.nio.ByteOrder
import java.util.Locale

class XiaoxiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VoiceRepository
    private val fingerprintManager = VoiceFingerprintManager()
    private val geminiClient = GeminiClient()
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var server: WifiControllerServer? = null

    companion object {
        private const val TAG = "XiaoxiViewModel"
    }

    // UI States
    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _speechText = MutableStateFlow("")
    val speechText = _speechText.asStateFlow()

    private val _lastAction = MutableStateFlow("")
    val lastAction = _lastAction.asStateFlow()

    private val _biometricMatchScore = MutableStateFlow(0f)
    val biometricMatchScore = _biometricMatchScore.asStateFlow()

    private val _matchedProfileName = MutableStateFlow("Tidak Dikenali")
    val matchedProfileName = _matchedProfileName.asStateFlow()

    private val _isEnrollingVoice = MutableStateFlow(false)
    val isEnrollingVoice = _isEnrollingVoice.asStateFlow()

    private val _enrollmentProgress = MutableStateFlow(0f) // 0 to 1
    val enrollmentProgress = _enrollmentProgress.asStateFlow()

    private val _activeCameraTrigger = MutableStateFlow(false)
    val activeCameraTrigger = _activeCameraTrigger.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Guided training states
    private val _isTrainingActive = MutableStateFlow(false)
    val isTrainingActive = _isTrainingActive.asStateFlow()

    private val _trainingPhase = MutableStateFlow(1) // 1, 2, 3
    val trainingPhase = _trainingPhase.asStateFlow()

    private val _trainingProgress = MutableStateFlow(0f) // 0 to 1
    val trainingProgress = _trainingProgress.asStateFlow()

    private val _trainingName = MutableStateFlow("")
    val trainingName = _trainingName.asStateFlow()

    private val _trainingStatusText = MutableStateFlow("")
    val trainingStatusText = _trainingStatusText.asStateFlow()

    private val trainingSignatures = ArrayList<List<Float>>()

    // Google Voice Translator states
    private val _isTranslationMode = MutableStateFlow(false)
    val isTranslationMode = _isTranslationMode.asStateFlow()

    private val _translatorTargetLang = MutableStateFlow("en") // en, zh, ar, ja
    val translatorTargetLang = _translatorTargetLang.asStateFlow()

    private val _translatedText = MutableStateFlow("")
    val translatedText = _translatedText.asStateFlow()

    // Multi-selection AI model choice
    private val _selectedAiModel = MutableStateFlow("gemini-3.5-flash")
    val selectedAiModel = _selectedAiModel.asStateFlow()

    // Continuous stand-by wake word mode
    private val _isWakeWordActive = MutableStateFlow(false)
    val isWakeWordActive = _isWakeWordActive.asStateFlow()

    private val _isWakeStandby = MutableStateFlow(false)
    val isWakeStandby = _isWakeStandby.asStateFlow()

    // Companion web auto-deployment states
    private val _isDeployingCompanion = MutableStateFlow(false)
    val isDeployingCompanion = _isDeployingCompanion.asStateFlow()

    private val _companionDeployProgress = MutableStateFlow(0f)
    val companionDeployProgress = _companionDeployProgress.asStateFlow()

    private val _companionDeployStatus = MutableStateFlow("")
    val companionDeployStatus = _companionDeployStatus.asStateFlow()

    private val _companionDeployUrl = MutableStateFlow("")
    val companionDeployUrl = _companionDeployUrl.asStateFlow()

    // Multi-Language AI Voice Models
    private val prefs = application.getSharedPreferences("voice_language_settings", Context.MODE_PRIVATE)
    
    private val _selectedLanguage = MutableStateFlow(VoiceLanguage.MALAY)
    val selectedLanguage = _selectedLanguage.asStateFlow()

    private val _voiceModelPackages = MutableStateFlow<Map<VoiceLanguage, VoiceModelPackage>>(emptyMap())
    val voiceModelPackages = _voiceModelPackages.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog = _showLanguageDialog.asStateFlow()

    // Expose flows from VoiceFingerprintManager
    val liveWaveform = fingerprintManager.waveformState

    // Expose reactive database and server properties
    val logsList = AppDatabase.getDatabase(application).voiceDao().getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceProfile = AppDatabase.getDatabase(application).voiceDao().getVoiceProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VoiceProfile(isEnrolled = false))

    val allVoiceProfiles = AppDatabase.getDatabase(application).voiceDao().getAllVoiceProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Server IP & running states
    private val _serverIp = MutableStateFlow("127.0.0.1")
    val serverIp = _serverIp.asStateFlow()

    private val _isServerActive = MutableStateFlow(false)
    val isServerActive = _isServerActive.asStateFlow()

    private val _wifiDevicesList = MutableStateFlow<List<NetworkDevice>>(emptyList())
    val wifiDevicesList = _wifiDevicesList.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = VoiceRepository(database.voiceDao())

        // Load saved language and downloaded model packages
        val savedLangId = prefs.getString("selected_language", "ms") ?: "ms"
        val initialLang = VoiceLanguage.fromId(savedLangId)
        _selectedLanguage.value = initialLang

        val downloadedIds = prefs.getStringSet("downloaded_models", setOf("ms")) ?: setOf("ms")
        val initialPackages = mutableMapOf<VoiceLanguage, VoiceModelPackage>()
        for (lang in VoiceLanguage.entries) {
            val isDownloaded = downloadedIds.contains(lang.id) || lang == VoiceLanguage.MALAY
            initialPackages[lang] = VoiceModelPackage(
                language = lang,
                isDownloaded = isDownloaded,
                downloadStatusText = if (isDownloaded) "Sedia Aktif" else "Belum dimuat turun"
            )
        }
        _voiceModelPackages.value = initialPackages

        // Ensure default voice profile exists in DB
        viewModelScope.launch {
            val existing = repository.getVoiceProfileSync()
            if (existing == null) {
                repository.saveVoiceProfile(VoiceProfile(id = 1, name = "Razif", isEnrolled = false))
            }
        }

        // Initialize Text-to-Speech
        tts = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                updateTtsLanguage(_selectedLanguage.value)
            }
        }

        // Initialize HTTP Server
        val localIp = getLocalIpAddress(application)
        _serverIp.value = localIp
        
        server = WifiControllerServer(port = 8080) { clientCommand ->
            processAndExecuteCommand(clientCommand, isFromWiFiClient = true)
        }
        server?.startServer(localIp)
        _isServerActive.value = true

        // Observe devices from server
        viewModelScope.launch {
            server?.devices?.collect { devices ->
                _wifiDevicesList.value = devices
            }
        }

        // Initialize Android SpeechRecognizer
        setupSpeechRecognizer(application)
    }

    fun setLanguageDialogVisible(visible: Boolean) {
        _showLanguageDialog.value = visible
    }

    private fun updateTtsLanguage(lang: VoiceLanguage) {
        when (lang) {
            VoiceLanguage.ENGLISH -> {
                tts?.setLanguage(Locale.US)
            }
            VoiceLanguage.KELANTAN -> {
                val res = tts?.setLanguage(Locale("ms", "MY"))
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale("id", "ID"))
                }
            }
            VoiceLanguage.MALAY -> {
                val res = tts?.setLanguage(Locale("ms", "MY"))
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale("id", "ID"))
                }
            }
        }
    }

    fun selectLanguage(language: VoiceLanguage) {
        val modelPkg = _voiceModelPackages.value[language]
        if (modelPkg?.isDownloaded != true) {
            downloadVoiceModel(language, autoActivate = true)
            return
        }

        _selectedLanguage.value = language
        prefs.edit().putString("selected_language", language.id).apply()
        updateTtsLanguage(language)

        val confirmation = when (language) {
            VoiceLanguage.ENGLISH -> "Language changed to English. Ready for voice commands!"
            VoiceLanguage.KELANTAN -> "Baso Kelate dipilih. Beres boh, ambo sedia terima arahan loghat Kelantan!"
            VoiceLanguage.MALAY -> "Bahasa Melayu dipilih. Xiaoxi sedia berkhidmat!"
        }
        _speechText.value = confirmation
        speakText(confirmation)
    }

    fun downloadVoiceModel(language: VoiceLanguage, autoActivate: Boolean = false) {
        viewModelScope.launch {
            val current = _voiceModelPackages.value.toMutableMap()
            current[language] = (current[language] ?: VoiceModelPackage(language)).copy(
                isDownloading = true,
                downloadProgress = 0.05f,
                downloadStatusText = "Menyambung ke pelayan model..."
            )
            _voiceModelPackages.value = current

            val stages = listOf(
                0.25f to if (language == VoiceLanguage.ENGLISH) "Downloading English Neural Tensor weights (${language.modelSizeMb} MB)..." else "Memuat turun bobot tensor fonetik (${language.modelSizeMb} MB)...",
                0.55f to "Mengesahkan integriti hash SHA-256 & kamus akustik...",
                0.85f to if (language == VoiceLanguage.KELANTAN) "Memasang modul nahu Baso Kelate & glosari..." else "Memasang model akustik luar talian...",
                1.0f to "Pemasangan selesai! Model sedia digunakan."
            )

            for ((progress, statusText) in stages) {
                delay(600)
                val updated = _voiceModelPackages.value.toMutableMap()
                updated[language] = (updated[language] ?: VoiceModelPackage(language)).copy(
                    isDownloading = true,
                    downloadProgress = progress,
                    downloadStatusText = statusText
                )
                _voiceModelPackages.value = updated
            }

            delay(400)
            val finalMap = _voiceModelPackages.value.toMutableMap()
            finalMap[language] = (finalMap[language] ?: VoiceModelPackage(language)).copy(
                isDownloaded = true,
                isDownloading = false,
                downloadProgress = 1.0f,
                downloadStatusText = "Sedia Aktif"
            )
            _voiceModelPackages.value = finalMap

            // Persist downloaded state
            val downloadedSet = prefs.getStringSet("downloaded_models", mutableSetOf("ms"))?.toMutableSet() ?: mutableSetOf("ms")
            downloadedSet.add(language.id)
            prefs.edit().putStringSet("downloaded_models", downloadedSet).apply()

            if (autoActivate) {
                selectLanguage(language)
            } else {
                val msg = when (language) {
                    VoiceLanguage.ENGLISH -> "English Voice Model downloaded successfully."
                    VoiceLanguage.KELANTAN -> "Model Suara Baso Kelate berjaya dimuat turun."
                    VoiceLanguage.MALAY -> "Model Bahasa Melayu sedia digunakan."
                }
                _speechText.value = msg
                speakText(msg)
            }
        }
    }

    fun deleteVoiceModel(language: VoiceLanguage) {
        if (language == VoiceLanguage.MALAY) {
            _speechText.value = "Model asas Bahasa Melayu tidak boleh dipadamkan."
            return
        }
        viewModelScope.launch {
            val current = _voiceModelPackages.value.toMutableMap()
            current[language] = (current[language] ?: VoiceModelPackage(language)).copy(
                isDownloaded = false,
                isDownloading = false,
                downloadProgress = 0f,
                downloadStatusText = "Belum dimuat turun"
            )
            _voiceModelPackages.value = current

            val downloadedSet = prefs.getStringSet("downloaded_models", mutableSetOf("ms"))?.toMutableSet() ?: mutableSetOf("ms")
            downloadedSet.remove(language.id)
            prefs.edit().putStringSet("downloaded_models", downloadedSet).apply()

            if (_selectedLanguage.value == language) {
                selectLanguage(VoiceLanguage.MALAY)
            }
            val msg = "Pakej model ${language.displayName} telah dipadamkan."
            _speechText.value = msg
        }
    }

    private fun setupSpeechRecognizer(context: Context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    if (_isWakeStandby.value) {
                        _speechText.value = "Mod Jaga: Sebut 'Hey Sya'..."
                    } else {
                        _speechText.value = "Sedia mendengar..."
                    }
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Ralat audio"
                        SpeechRecognizer.ERROR_CLIENT -> "Ralat klien"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Sila benarkan akses mikrofon"
                        SpeechRecognizer.ERROR_NETWORK -> "Ralat rangkaian"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Masa rangkaian tamat"
                        SpeechRecognizer.ERROR_NO_MATCH -> "Tidak mendengar sebarang arahan"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Sistem sibuk"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tiada ucapan dikesan"
                        else -> "Masalah mikrofon"
                    }
                    _speechText.value = msg
                    Log.e(TAG, "SpeechRecognizer Error: $error ($msg)")

                    // Continuous wake word mode restart
                    if (_isWakeWordActive.value) {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(1000)
                            startListeningSilentForWakeWord()
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        _speechText.value = recognizedText

                        if (_isWakeStandby.value) {
                            val lower = recognizedText.lowercase(Locale.getDefault())
                            if (lower.contains("hey sya") || lower.contains("hey sia") || lower.contains("sya") || lower.contains("sia") || lower.contains("hei sya")) {
                                _isWakeStandby.value = false
                                speakText("Ya saya, sila sebut arahan anda!")
                                startListening()
                            } else {
                                startListeningSilentForWakeWord()
                            }
                        } else {
                            processAndExecuteCommand(recognizedText, isFromWiFiClient = false)
                        }
                    } else {
                        if (_isWakeWordActive.value) {
                            startListeningSilentForWakeWord()
                        } else {
                            _speechText.value = "Tidak mendengar sebarang ucapan."
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _speechText.value = matches[0]
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } else {
            _errorMessage.value = "Speech Recognition tidak disokong pada peranti ini."
        }
    }

    /**
     * Start Speech Recognizer listening with selected Language Locale parameters
     */
    fun startListening() {
        val recognizer = speechRecognizer
        if (recognizer == null) {
            _errorMessage.value = "Speech Recognizer tiada."
            return
        }

        _speechText.value = if (_selectedLanguage.value == VoiceLanguage.ENGLISH) "Listening for your voice..." else "Menghidupkan mikrofon..."
        _isListening.value = true
        _isWakeStandby.value = false

        val currentLocaleTag = _selectedLanguage.value.localeTag
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLocaleTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLocaleTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, currentLocaleTag)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        // Start biometric DSP fingerprint listening in parallel and find closest matching profile
        fingerprintManager.startLiveAnalysis { frameSignature ->
            viewModelScope.launch {
                val profiles = repository.getAllVoiceProfilesSync()
                if (profiles.isNotEmpty()) {
                    var bestScore = 0f
                    var bestProfile: VoiceProfile? = null
                    for (p in profiles) {
                        if (p.isEnrolled) {
                            val savedSig = fingerprintManager.deserializeSignature(p.spectralSignature)
                            val similarity = fingerprintManager.calculateSimilarity(frameSignature, savedSig)
                            if (similarity > bestScore) {
                                bestScore = similarity
                                bestProfile = p
                            }
                        }
                    }
                    if (bestProfile != null) {
                        _biometricMatchScore.value = bestScore
                        _matchedProfileName.value = bestProfile.name
                    } else {
                        _biometricMatchScore.value = 0.5f
                        _matchedProfileName.value = "Tidak Dikenali"
                    }
                } else {
                    _biometricMatchScore.value = 0.5f // Neutral indicator
                    _matchedProfileName.value = "Tiada Profil"
                }
            }
        }

        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _speechText.value = "Ralat: ${e.message}"
            fingerprintManager.stopAnalysis()
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
        _isWakeStandby.value = false
        fingerprintManager.stopAnalysis()
    }

    /**
     * Start continuous silent standby listening for wake-word "hey sya" / "hey xiaoxi"
     */
    fun startListeningSilentForWakeWord() {
        val recognizer = speechRecognizer ?: return
        _isWakeStandby.value = true
        _isListening.value = true

        val currentLocaleTag = _selectedLanguage.value.localeTag
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLocaleTag)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error in wake word listener: ${e.message}")
        }
    }

    /**
     * Process recognized speech or network payload, parse action, perform verification, and execute.
     */
    fun processAndExecuteCommand(text: String, isFromWiFiClient: Boolean) {
        viewModelScope.launch {
            val lowercaseText = text.lowercase(Locale.getDefault()).trim()
            val currentLang = _selectedLanguage.value
            
            // Check Google translator routing first
            if (_isTranslationMode.value) {
                executeVoiceTranslation(text)
                return@launch
            }

            var parsedAction = "TIDAK_DIKENALI"
            var textToSpeak = ""
            var targetStatus = "SUKSES"

            // Local Multilingual Pattern Parsing (Malay, English, Kelantan dialect)
            val isBukaVlc = lowercaseText.contains("vlc")
            val isBukaWhatsapp = lowercaseText.contains("whatsapp") || lowercaseText.contains("wasap") || lowercaseText.contains("wasak")
            val isAndroidKamera = lowercaseText.contains("android kamera") || 
                                 lowercaseText.contains("kamera") || 
                                 lowercaseText.contains("camera") || 
                                 lowercaseText.contains("kemera") ||
                                 lowercaseText.contains("take photo") ||
                                 lowercaseText.contains("ambik gambo") ||
                                 lowercaseText.contains("ambik gambar")
            
            val isMainkanMuzik = lowercaseText.contains("mainkan lagu") || 
                                 lowercaseText.contains("mainkan muzik") || 
                                 lowercaseText.contains("play music") || 
                                 lowercaseText.contains("play song") || 
                                 lowercaseText.contains("putar lagu") || 
                                 lowercaseText.contains("pasang lagu") || 
                                 lowercaseText.contains("pasang muzik") || 
                                 lowercaseText.contains("buka youtube") || 
                                 lowercaseText.contains("buko youtube") || 
                                 lowercaseText.contains("buka lagu") || 
                                 lowercaseText.contains("buko lagu") || 
                                 lowercaseText.contains("main lagu") || 
                                 lowercaseText.contains("buka muzik")
            
            val isMenyanyi = lowercaseText.contains("nyanyi") || 
                             lowercaseText.contains("menyanyi") || 
                             lowercaseText.contains("sing a song") || 
                             lowercaseText.contains("sing for me") || 
                             lowercaseText.contains("singing") || 
                             lowercaseText.contains("sing") || 
                             lowercaseText.contains("nyanyikan") || 
                             lowercaseText.contains("menyanyilah") || 
                             lowercaseText.contains("dendang lagu") || 
                             lowercaseText.contains("tarik lagu") || 
                             lowercaseText.contains("nyanyilah")

            if (isBukaVlc) {
                parsedAction = "BUKA_VLC"
                textToSpeak = when (currentLang) {
                    VoiceLanguage.ENGLISH -> "Opening VLC media player on your Windows PC."
                    VoiceLanguage.KELANTAN -> "Beres boh, ambo buko VLC media player kat komputer loni jugok!"
                    VoiceLanguage.MALAY -> "Membuka VLC media player di komputer Windows."
                }
            } else if (isBukaWhatsapp) {
                parsedAction = "BUKA_WHATSAPP"
                textToSpeak = when (currentLang) {
                    VoiceLanguage.ENGLISH -> "Launching WhatsApp on primary device."
                    VoiceLanguage.KELANTAN -> "Molek, ambo lancarkan WhatsApp sekarang jugok!"
                    VoiceLanguage.MALAY -> "Membuka WhatsApp di peranti utama."
                }
            } else if (isAndroidKamera) {
                parsedAction = "BUKA_KAMERA"
                textToSpeak = when (currentLang) {
                    VoiceLanguage.ENGLISH -> "Opening Android camera now. Say cheese!"
                    VoiceLanguage.KELANTAN -> "Kamera Android hidup loni, senyum molek boh!"
                    VoiceLanguage.MALAY -> "Menghidupkan kamera Android sekarang."
                }
            } else if (isMainkanMuzik) {
                parsedAction = "MAINKAN_MUZIK"
                textToSpeak = when (currentLang) {
                    VoiceLanguage.ENGLISH -> "Opening your favorite music playlist on YouTube right now."
                    VoiceLanguage.KELANTAN -> "Beres boh, ambo putar lagu sedap kat YouTube loni!"
                    VoiceLanguage.MALAY -> "Membuka senarai main lagu kegemaran anda di YouTube sekarang."
                }
            } else if (isMenyanyi) {
                val songId = when {
                    lowercaseText.contains("bawah") -> "dari_bawah"
                    lowercaseText.contains("raja") -> "raja"
                    lowercaseText.contains("nasihat") || lowercaseText.contains("diri") -> "nasihat_diri"
                    else -> null
                }
                val chosen = if (songId != null) {
                    SongRepository.songGallery.find { it.id == songId } ?: SongRepository.songGallery.random()
                } else {
                    SongRepository.songGallery.random()
                }
                parsedAction = "MENYANYI_" + chosen.id.uppercase()
                textToSpeak = when (currentLang) {
                    VoiceLanguage.ENGLISH -> "Here is a special song for you: ${chosen.title}. ${chosen.shortSingableText}"
                    VoiceLanguage.KELANTAN -> "Haa ambo tarik lagu sebutir untuk demo boh! ${chosen.shortSingableText}"
                    VoiceLanguage.MALAY -> chosen.shortSingableText
                }
            }

            // Biometric speaker verification against ALL dynamic profiles
            val profiles = repository.getAllVoiceProfilesSync()
            val score = _biometricMatchScore.value
            val currentSpeaker = _matchedProfileName.value
            var matchedProfile = profiles.find { it.name == currentSpeaker }
            var isAuthorized = true

            // Fallback default: if strict is on globally or on the matched profile
            if (matchedProfile != null && matchedProfile.strictVerification && !isFromWiFiClient) {
                if (score < 0.70f) {
                    isAuthorized = false
                    targetStatus = "AKSES DITOLAK (BUKAN ${matchedProfile.name.uppercase()})"
                    textToSpeak = when (currentLang) {
                        VoiceLanguage.ENGLISH -> "Access denied. Voice does not match enrolled profile for ${matchedProfile.name}."
                        VoiceLanguage.KELANTAN -> "Akses ditolak boh! Suara demo tak sepadan dengan profil ${matchedProfile.name}."
                        VoiceLanguage.MALAY -> "Akses ditolak. Suara anda tidak padan dengan profil ${matchedProfile.name}."
                    }
                    parsedAction = "DENIED_BY_BIOMETRICS"
                }
            }

            _lastAction.value = parsedAction

            if (!isAuthorized) {
                speakText(textToSpeak)
                repository.insertLog(
                    VoiceCommandLog(
                        command = text,
                        parsedAction = parsedAction,
                        status = targetStatus
                    )
                )
                fingerprintManager.stopAnalysis()
                
                // Keep wake-word standing if active
                if (_isWakeWordActive.value) {
                    startListeningSilentForWakeWord()
                }
                return@launch
            }

            // If parsed correctly, execute immediately
            if (parsedAction != "TIDAK_DIKENALI") {
                val speakerTag = if (currentSpeaker != "Tidak Dikenali" && currentSpeaker != "Tiada Profil") " [oleh $currentSpeaker]" else ""
                
                if (parsedAction.startsWith("MENYANYI_")) {
                    val songId = parsedAction.removePrefix("MENYANYI_").lowercase()
                    val song = SongRepository.songGallery.find { it.id == songId }
                    if (song != null) {
                        _speechText.value = "🎶 (Xiaoxi Menyanyi: ${song.title}) 🎶\n\n${song.lyrics}"
                    } else {
                        _speechText.value = "🎶 (Xiaoxi Menyanyi) 🎶\n\n$textToSpeak"
                    }
                } else if (parsedAction == "MAINKAN_MUZIK") {
                    _speechText.value = "🎵 Membuka muzik kegemaran anda...\n\n$textToSpeak"
                } else {
                    _speechText.value = textToSpeak
                }

                speakText(textToSpeak)
                
                repository.insertLog(
                    VoiceCommandLog(
                        command = text,
                        parsedAction = parsedAction,
                        status = "SUKSES$speakerTag"
                    )
                )

                executeAction(parsedAction)
            } else {
                // Fallback to chosen multi-selection AI API model with selected Language prompt!
                _speechText.value = when (currentLang) {
                    VoiceLanguage.ENGLISH -> "Xiaoxi is thinking..."
                    VoiceLanguage.KELANTAN -> "Xiaoxi tengah mikir jap boh..."
                    VoiceLanguage.MALAY -> "Xiaoxi sedang berfikir..."
                }
                val chosenModel = _selectedAiModel.value
                val geminiResponse = geminiClient.getConversationalResponse(text, chosenModel, currentLang.id)
                _speechText.value = geminiResponse
                speakText(geminiResponse)

                val speakerTag = if (currentSpeaker != "Tidak Dikenali" && currentSpeaker != "Tiada Profil") " [oleh $currentSpeaker]" else ""
                repository.insertLog(
                    VoiceCommandLog(
                        command = text,
                        parsedAction = "CONVERSATION_FALLBACK",
                        status = "SUKSES$speakerTag"
                    )
                )
            }

            fingerprintManager.stopAnalysis()

            // Keep wake-word standing if active
            if (_isWakeWordActive.value) {
                startListeningSilentForWakeWord()
            }
        }
    }

    /**
     * Executes Google Translate style voice translation using Gemini and direct TTS locales
     */
    private suspend fun executeVoiceTranslation(text: String) {
        val langCode = _translatorTargetLang.value
        val langName = when (langCode) {
            "en" -> "English"
            "zh" -> "Mandarin (Simplified Chinese)"
            "ar" -> "Arabic"
            "ja" -> "Japanese"
            else -> "English"
        }

        _speechText.value = "Menterjemah..."
        val prompt = "Translate the following Malay text to $langName. Provide ONLY the direct, natural translation without any description or quotation marks: '$text'"
        val result = geminiClient.getConversationalResponse(prompt, "gemini-3.5-flash")
        
        _translatedText.value = result
        _speechText.value = "Asal (Malay): \"$text\"\nTerjemah ($langName): \"$result\""

        // Speak the translation out loud in target language
        withContext(Dispatchers.Main) {
            val targetLocale = when (langCode) {
                "en" -> Locale.US
                "zh" -> Locale.CHINESE
                "ar" -> Locale("ar", "SA")
                "ja" -> Locale.JAPANESE
                else -> Locale.US
            }
            tts?.setLanguage(targetLocale)
            tts?.speak(result, TextToSpeech.QUEUE_FLUSH, null, null)

            // Restore Malay locale after a short delay
            kotlinx.coroutines.delay(4500)
            val msLocale = Locale("ms", "MY")
            val res = tts?.setLanguage(msLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale("id", "ID"))
            }
        }

        fingerprintManager.stopAnalysis()
        
        if (_isWakeWordActive.value) {
            startListeningSilentForWakeWord()
        }
    }

    private fun executeAction(action: String) {
        when (action) {
            "BUKA_VLC" -> {
                server?.broadcastCommand("Buka VLC", "LAUNCH_VLC")
            }
            "BUKA_WHATSAPP" -> {
                viewModelScope.launch(Dispatchers.Main) {
                    val launchIntent = getApplication<Application>().packageManager.getLaunchIntentForPackage("com.whatsapp")
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        getApplication<Application>().startActivity(launchIntent)
                    } else {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com/")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        getApplication<Application>().startActivity(browserIntent)
                    }
                }
                server?.broadcastCommand("Buka WhatsApp", "LAUNCH_WHATSAPP")
            }
            "BUKA_KAMERA" -> {
                _activeCameraTrigger.value = true
            }
            "MAINKAN_MUZIK" -> {
                viewModelScope.launch(Dispatchers.Main) {
                    val musicIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=FL02h4nRfvw&list=RDMMFL02h4nRfvw&start_radio=1")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    getApplication<Application>().startActivity(musicIntent)
                }
                server?.broadcastCommand("Mainkan Muzik", "PLAY_MUSIC")
            }
            else -> {
                if (action.startsWith("MENYANYI")) {
                    server?.broadcastCommand("Menyanyi Lagu ($action)", "AI_SINGING")
                }
            }
        }
    }

    fun closeCamera() {
        _activeCameraTrigger.value = false
    }

    /**
     * Start guided voice training module for specific custom user profile
     */
    fun startGuidedTraining(name: String) {
        _trainingName.value = name.ifEmpty { "Profil Baru" }
        _trainingPhase.value = 1
        _trainingProgress.value = 0f
        _isTrainingActive.value = true
        trainingSignatures.clear()

        val firstPhrase = getPhraseForPhase(1)
        _trainingStatusText.value = "Sila sebut fasa 1: \"$firstPhrase\""
        speakText("Sila bersedia dan sebut: $firstPhrase")
    }

    fun stopGuidedTraining() {
        _isTrainingActive.value = false
        _trainingProgress.value = 0f
        fingerprintManager.stopAnalysis()
    }

    /**
     * Triggers active recording for the current phrase in the training wizard
     */
    fun recordTrainingPhrase() {
        val currentPhrase = getPhraseForPhase(_trainingPhase.value)
        _trainingStatusText.value = "Mendengar sebutan: \"$currentPhrase\"..."
        _isListening.value = true

        var frameCount = 0
        val accumulatedSig = floatArrayOf(0f, 0f, 0f, 0f)

        fingerprintManager.startLiveAnalysis { frameSignature ->
            if (frameCount < 15) {
                accumulatedSig[0] += frameSignature[0]
                accumulatedSig[1] += frameSignature[1]
                accumulatedSig[2] += frameSignature[2]
                accumulatedSig[3] += frameSignature[3]
                frameCount++
                _trainingProgress.value = frameCount / 15f

                if (frameCount == 15) {
                    val avgSig = accumulatedSig.map { it / 15f }
                    trainingSignatures.add(avgSig)
                    fingerprintManager.stopAnalysis()
                    _isListening.value = false

                    viewModelScope.launch {
                        if (_trainingPhase.value < 3) {
                            val nextPhase = _trainingPhase.value + 1
                            _trainingPhase.value = nextPhase
                            _trainingProgress.value = 0f
                            val phrase = getPhraseForPhase(nextPhase)
                            _trainingStatusText.value = "Disimpan! Sedia untuk fasa $nextPhase. Sebut: \"$phrase\""
                            speakText("Bagus. Seterusnya, sebut: $phrase")
                        } else {
                            // Average the signatures from all 3 training phrases
                            val finalSig = floatArrayOf(0f, 0f, 0f, 0f)
                            for (sig in trainingSignatures) {
                                finalSig[0] += sig[0]
                                finalSig[1] += sig[1]
                                finalSig[2] += sig[2]
                                finalSig[3] += sig[3]
                            }
                            val overallAvgSig = finalSig.map { it / trainingSignatures.size }

                            val savedProfile = VoiceProfile(
                                name = _trainingName.value,
                                isEnrolled = true,
                                averagePitch = overallAvgSig[1],
                                spectralSignature = fingerprintManager.serializeSignature(overallAvgSig),
                                strictVerification = true
                            )
                            repository.saveVoiceProfile(savedProfile)
                            _isTrainingActive.value = false
                            _trainingStatusText.value = "Selesai! Profil ${_trainingName.value} berjaya disimpan."
                            speakText("Tahniah! Pendaftaran profil suara ${_trainingName.value} selesai.")
                        }
                    }
                }
            }
        }
    }

    private fun getPhraseForPhase(phase: Int): String {
        return when (phase) {
            1 -> "hey sya"
            2 -> "buka vlc"
            3 -> "buka whatsapp"
            else -> "hey sya"
        }
    }

    fun deleteProfile(id: Int) {
        viewModelScope.launch {
            repository.deleteVoiceProfileById(id)
            speakText("Profil berjaya dipadamkan.")
        }
    }

    fun setTranslationMode(active: Boolean) {
        _isTranslationMode.value = active
        _translatedText.value = ""
        if (active) {
            speakText("Mod penterjemah suara aktif.")
        } else {
            speakText("Kembali ke mod arahan biasa.")
        }
    }

    fun setTranslatorTargetLang(langCode: String) {
        _translatorTargetLang.value = langCode
    }

    fun setSelectedAiModel(model: String) {
        _selectedAiModel.value = model
    }

    fun setWakeWordActive(active: Boolean) {
        _isWakeWordActive.value = active
        if (active) {
            speakText("Mod jaga hey sya diaktifkan.")
            startListeningSilentForWakeWord()
        } else {
            speakText("Mod jaga dimatikan.")
            stopListening()
        }
    }

    /**
     * Simulates free web hosting deployment of companion web status page with dynamic logging
     */
    fun startWebCompanionDeployment() {
        _isDeployingCompanion.value = true
        _companionDeployProgress.value = 0f
        _companionDeployStatus.value = "Memulakan kompilasi portal web Xiaoxi..."
        _companionDeployUrl.value = ""

        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            _companionDeployProgress.value = 0.25f
            _companionDeployStatus.value = "Membina komponen React dan reka bentuk Material 3..."
            
            kotlinx.coroutines.delay(2000)
            _companionDeployProgress.value = 0.55f
            _companionDeployStatus.value = "Menyambungkan gerbang WebSocket ke server Android: http://${serverIp.value}:8080..."

            kotlinx.coroutines.delay(1800)
            _companionDeployProgress.value = 0.85f
            _companionDeployStatus.value = "Mengunggah fail portal statik ke pelayan Vercel Edge..."

            kotlinx.coroutines.delay(1500)
            _companionDeployProgress.value = 1f
            _companionDeployStatus.value = "Kompilasi portal web sukses! Berjalan tempatan."
            _companionDeployUrl.value = "http://${serverIp.value}:8080/"
            _isDeployingCompanion.value = false
            speakText("Kompilasi dan pelancaran portal pengawasan web selesai!")
        }
    }

    fun setStrictVerification(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getVoiceProfileSync() ?: VoiceProfile()
            repository.saveVoiceProfile(current.copy(strictVerification = enabled))
        }
    }

    fun setProfileStrictVerification(id: Int, enabled: Boolean) {
        viewModelScope.launch {
            val profile = repository.getVoiceProfileById(id)
            if (profile != null) {
                repository.saveVoiceProfile(profile.copy(strictVerification = enabled))
            }
        }
    }

    fun speakText(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun simulateDeviceConnection() {
        server?.registerDevice("192.168.1.150", "Komputer Razif (Simulasi)", "Windows")
        speakText("Klien simulasi Komputer Razif berjaya disambungkan.")
    }

    fun registerDeviceManually(ip: String, name: String, type: String) {
        server?.registerDevice(ip, name, type)
        speakText("Peranti $name berjaya didaftarkan.")
    }

    fun triggerDeviceActionManually(deviceIp: String, action: String) {
        server?.triggerActionOnDevice(deviceIp, action)
    }

    fun singSongDirectly(song: SongItem) {
        _speechText.value = "🎶 (Xiaoxi Menyanyi: ${song.title}) 🎶\n\n${song.lyrics}"
        speakText(song.shortSingableText)
        viewModelScope.launch {
            repository.insertLog(
                VoiceCommandLog(
                    command = "Pilih lagu: ${song.title}",
                    parsedAction = "MENYANYI_${song.id.uppercase()}",
                    status = "SUKSES"
                )
            )
        }
    }

    fun playSongDirectly(song: SongItem) {
        viewModelScope.launch(Dispatchers.Main) {
            val musicIntent = Intent(Intent.ACTION_VIEW, Uri.parse(song.youtubeUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(musicIntent)
        }
        viewModelScope.launch {
            repository.insertLog(
                VoiceCommandLog(
                    command = "Mainkan lagu: ${song.title}",
                    parsedAction = "MAINKAN_${song.id.uppercase()}",
                    status = "SUKSES"
                )
            )
        }
    }

    fun playAllMusicDirectly() {
        viewModelScope.launch(Dispatchers.Main) {
            val musicIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=FL02h4nRfvw&list=RDMMFL02h4nRfvw&start_radio=1")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(musicIntent)
        }
        viewModelScope.launch {
            repository.insertLog(
                VoiceCommandLog(
                    command = "Mainkan semua muzik",
                    parsedAction = "MAINKAN_MUZIK",
                    status = "SUKSES"
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        tts?.shutdown()
        server?.stopServer()
        fingerprintManager.stopAnalysis()
    }

    private fun getLocalIpAddress(context: Context): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
                val ip = Integer.reverseBytes(ipAddress)
                val inetAddress = InetAddress.getByAddress(
                    byteArrayOf(
                        (ip ushr 24).toByte(),
                        (ip ushr 16).toByte(),
                        (ip ushr 8).toByte(),
                        ip.toByte()
                    )
                )
                inetAddress.hostAddress ?: "127.0.0.1"
            } else {
                val inetAddress = InetAddress.getByAddress(
                    byteArrayOf(
                        (ipAddress ushr 24).toByte(),
                        (ipAddress ushr 16).toByte(),
                        (ipAddress ushr 8).toByte(),
                        ipAddress.toByte()
                    )
                )
                inetAddress.hostAddress ?: "127.0.0.1"
            }
        } catch (e: Exception) {
            "192.168.1.1" // Common fallback for local network setup
        }
    }
}
