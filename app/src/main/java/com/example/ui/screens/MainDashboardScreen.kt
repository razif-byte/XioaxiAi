package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Camera
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import com.example.data.entity.VoiceLanguage
import com.example.data.entity.VoiceModelPackage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CameraPreviewView
import com.example.ui.viewmodel.XiaoxiViewModel
import com.example.data.entity.VoiceProfile
import com.example.data.entity.VoiceCommandLog
import com.example.data.entity.SongItem
import com.example.data.entity.SongRepository
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.OutlinedButton
import androidx.compose.animation.animateContentSize
import com.example.util.NetworkDevice
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainDashboardScreen(
    viewModel: XiaoxiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isListening by viewModel.isListening.collectAsState()
    val speechText by viewModel.speechText.collectAsState()
    val biometricMatchScore by viewModel.biometricMatchScore.collectAsState()
    val liveWaveform by viewModel.liveWaveform.collectAsState()
    
    val logs by viewModel.logsList.collectAsState()
    val wifiDevices by viewModel.wifiDevicesList.collectAsState()
    val serverIp by viewModel.serverIp.collectAsState()
    val isServerActive by viewModel.isServerActive.collectAsState()
    val activeCameraTrigger by viewModel.activeCameraTrigger.collectAsState()

    val matchedProfileName by viewModel.matchedProfileName.collectAsState()
    val isTrainingActive by viewModel.isTrainingActive.collectAsState()
    val trainingPhase by viewModel.trainingPhase.collectAsState()
    val trainingProgress by viewModel.trainingProgress.collectAsState()
    val trainingName by viewModel.trainingName.collectAsState()
    val trainingStatusText by viewModel.trainingStatusText.collectAsState()
    val allProfiles by viewModel.allVoiceProfiles.collectAsState()

    val isTranslationMode by viewModel.isTranslationMode.collectAsState()
    val translatorTargetLang by viewModel.translatorTargetLang.collectAsState()
    val translatedText by viewModel.translatedText.collectAsState()
    val selectedAiModel by viewModel.selectedAiModel.collectAsState()
    val isWakeWordActive by viewModel.isWakeWordActive.collectAsState()
    val isWakeStandby by viewModel.isWakeStandby.collectAsState()

    val isDeployingCompanion by viewModel.isDeployingCompanion.collectAsState()
    val companionDeployProgress by viewModel.companionDeployProgress.collectAsState()
    val companionDeployStatus by viewModel.companionDeployStatus.collectAsState()
    val companionDeployUrl by viewModel.companionDeployUrl.collectAsState()

    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val voiceModelPackages by viewModel.voiceModelPackages.collectAsState()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Permissions
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Ensure permissions on launch
    LaunchedEffect(Unit) {
        if (!recordAudioPermissionState.status.isGranted) {
            recordAudioPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Utama") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.Mic else Icons.Outlined.Mic,
                                contentDescription = "Tab Utama"
                            )
                        },
                        modifier = Modifier.testTag("tab_mic")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Peranti WiFi") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.Router else Icons.Outlined.Devices,
                                contentDescription = "Tab WiFi"
                            )
                        },
                        modifier = Modifier.testTag("tab_wifi")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("Profil Suara") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Filled.Fingerprint else Icons.Outlined.Fingerprint,
                                contentDescription = "Tab Profil"
                            )
                        },
                        modifier = Modifier.testTag("tab_fingerprint")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        label = { Text("Log") },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 3) Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = "Tab Log"
                            )
                        },
                        modifier = Modifier.testTag("tab_logs")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        label = { Text("Galeri Lagu") },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = "Tab Galeri Lagu"
                            )
                        },
                        modifier = Modifier.testTag("tab_music_gallery")
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .padding(paddingValues)
            ) {
                // Header Banner
                HeaderBannerSection(serverIp = serverIp, isServerActive = isServerActive)

                // Page Renderers
                when (selectedTab) {
                    0 -> UtamaTabScreen(
                        isListening = isListening,
                        speechText = speechText,
                        biometricMatchScore = biometricMatchScore,
                        matchedProfileName = matchedProfileName,
                        liveWaveform = liveWaveform,
                        isTranslationMode = isTranslationMode,
                        translatorTargetLang = translatorTargetLang,
                        translatedText = translatedText,
                        selectedAiModel = selectedAiModel,
                        isWakeWordActive = isWakeWordActive,
                        isWakeStandby = isWakeStandby,
                        selectedLanguage = selectedLanguage,
                        voiceModelPackages = voiceModelPackages,
                        onOpenLanguageManager = { viewModel.setLanguageDialogVisible(true) },
                        onSelectLanguage = { viewModel.selectLanguage(it) },
                        onListeningToggle = {
                            if (recordAudioPermissionState.status.isGranted) {
                                if (isListening) viewModel.stopListening() else viewModel.startListening()
                            } else {
                                recordAudioPermissionState.launchPermissionRequest()
                            }
                        },
                        onManualTrigger = { command ->
                            viewModel.processAndExecuteCommand(command, isFromWiFiClient = false)
                        },
                        onTranslationModeToggle = { viewModel.setTranslationMode(it) },
                        onTargetLanguageSelect = { viewModel.setTranslatorTargetLang(it) },
                        onAiModelSelect = { viewModel.setSelectedAiModel(it) },
                        onWakeWordToggle = { viewModel.setWakeWordActive(it) }
                    )
                    1 -> WifiDevicesTabScreen(
                        devices = wifiDevices,
                        serverIp = serverIp,
                        isDeployingCompanion = isDeployingCompanion,
                        companionDeployProgress = companionDeployProgress,
                        companionDeployStatus = companionDeployStatus,
                        companionDeployUrl = companionDeployUrl,
                        onTriggerAction = { ip, action -> viewModel.triggerDeviceActionManually(ip, action) },
                        onSimulateDevice = {
                            viewModel.simulateDeviceConnection()
                        },
                        onDeployWebCompanion = { viewModel.startWebCompanionDeployment() },
                        onAddDeviceManually = { ip, name, type -> viewModel.registerDeviceManually(ip, name, type) }
                    )
                    2 -> VoiceProfileTabScreen(
                        isTrainingActive = isTrainingActive,
                        trainingPhase = trainingPhase,
                        trainingProgress = trainingProgress,
                        trainingName = trainingName,
                        trainingStatusText = trainingStatusText,
                        allProfiles = allProfiles,
                        liveWaveform = liveWaveform,
                        onStartGuidedTraining = { name -> viewModel.startGuidedTraining(name) },
                        onStopGuidedTraining = { viewModel.stopGuidedTraining() },
                        onRecordPhrase = { viewModel.recordTrainingPhrase() },
                        onDeleteProfile = { id -> viewModel.deleteProfile(id) },
                        onProfileStrictToggle = { id, enabled -> viewModel.setProfileStrictVerification(id, enabled) }
                    )
                    3 -> LogTabScreen(
                        logs = logs,
                        onClearLogs = { viewModel.clearAllLogs() }
                    )
                    4 -> SongGalleryTabScreen(
                        songs = SongRepository.songGallery,
                        onSingSong = { viewModel.singSongDirectly(it) },
                        onPlaySong = { viewModel.playSongDirectly(it) },
                        onPlayAllMusic = { viewModel.playAllMusicDirectly() }
                    )
                }
            }
        }

        // Camera Preview Overlay
        AnimatedVisibility(
            visible = activeCameraTrigger,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                if (cameraPermissionState.status.isGranted) {
                    CameraPreviewView()
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "No Camera Permission",
                                tint = Color.Yellow,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Benarkan akses Kamera untuk fungsi ini",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                                Text("Benarkan Akses")
                            }
                        }
                    }
                }

                // Close Button
                IconButton(
                    onClick = { viewModel.closeCamera() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .testTag("close_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Tutup Kamera",
                        tint = Color.White
                    )
                }

                // Overlay Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        "Kamera Peranti Aktif (Sebut 'tutup' atau klik pangkah)",
                        color = Color.Green,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Voice Language & Model Download Manager Dialog
        if (showLanguageDialog) {
            VoiceLanguageManagerDialog(
                selectedLanguage = selectedLanguage,
                voiceModelPackages = voiceModelPackages,
                onSelectLanguage = { viewModel.selectLanguage(it) },
                onDownloadModel = { viewModel.downloadVoiceModel(it) },
                onDeleteModel = { viewModel.deleteVoiceModel(it) },
                onTestCommand = { cmd -> 
                    viewModel.setLanguageDialogVisible(false)
                    viewModel.processAndExecuteCommand(cmd, isFromWiFiClient = false)
                },
                onDismiss = { viewModel.setLanguageDialogVisible(false) }
            )
        }
    }
}

@Composable
fun HeaderBannerSection(serverIp: String, isServerActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pulsing/Glowing status indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isServerActive) MaterialTheme.colorScheme.primary else Color.Red,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "XIAOXI AI PORTAL",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "v1.0.4-portable • IP: http://$serverIp:8080",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "PORTABLE MODE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun UtamaTabScreen(
    isListening: Boolean,
    speechText: String,
    biometricMatchScore: Float,
    matchedProfileName: String,
    liveWaveform: List<Float>,
    isTranslationMode: Boolean,
    translatorTargetLang: String,
    translatedText: String,
    selectedAiModel: String,
    isWakeWordActive: Boolean,
    isWakeStandby: Boolean,
    selectedLanguage: VoiceLanguage,
    voiceModelPackages: Map<VoiceLanguage, VoiceModelPackage>,
    onOpenLanguageManager: () -> Unit,
    onSelectLanguage: (VoiceLanguage) -> Unit,
    onListeningToggle: () -> Unit,
    onManualTrigger: (String) -> Unit,
    onTranslationModeToggle: (Boolean) -> Unit,
    onTargetLanguageSelect: (String) -> Unit,
    onAiModelSelect: (String) -> Unit,
    onWakeWordToggle: (Boolean) -> Unit
) {
    val scorePercentage = (biometricMatchScore * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Model Selection & Language Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // AI Model Dropdown
                        var showAiMenu by remember { mutableStateOf(false) }
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .clickable { showAiMenu = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI: ${selectedAiModel.uppercase()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showAiMenu,
                                onDismissRequest = { showAiMenu = false }
                            ) {
                                listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-1.5-flash", "gemini-1.5-pro").forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model, fontSize = 12.sp) },
                                        onClick = {
                                            onAiModelSelect(model)
                                            showAiMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Standby Wake Status
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isWakeStandby) Color.Green.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isWakeStandby) "SIAGA 'HEY SYA'" else "MOD MANUAL",
                                color = if (isWakeStandby) Color.Green else MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Language Quick Selector & Download Manager Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            VoiceLanguage.entries.forEach { lang ->
                                val isSelected = selectedLanguage == lang
                                val pkg = voiceModelPackages[lang]
                                val isDownloaded = pkg?.isDownloaded == true
                                val isDownloading = pkg?.isDownloading == true

                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary 
                                            else if (isDownloaded) MaterialTheme.colorScheme.surface 
                                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSelectLanguage(lang) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .testTag("chip_lang_${lang.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = lang.flag,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when(lang) {
                                                VoiceLanguage.MALAY -> "Melayu"
                                                VoiceLanguage.ENGLISH -> "English"
                                                VoiceLanguage.KELANTAN -> "Kelate"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isDownloading) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(10.dp),
                                                strokeWidth = 2.dp,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Model Manager Button
                        IconButton(
                            onClick = onOpenLanguageManager,
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), CircleShape)
                                .testTag("btn_open_language_manager")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Language,
                                contentDescription = "Model Suara & Bahasa",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Large Visualizer Area
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Biometric match or user profile recognized
                    if (matchedProfileName.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "PENGGUNA: ${matchedProfileName.uppercase()} (${scorePercentage}% MATCH)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (biometricMatchScore >= 0.7f) Color.Green else Color.Red, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    } else {
                        // Default general badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "MENANTI PROFIL SUARA DIKENALPASTI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Waveform drawing with Glowing effect
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = 8f
                            val spacing = 6f
                            val centerY = size.height / 2f
                            val brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFD0BCFF), Color(0xFF381E72))
                            )

                            liveWaveform.forEachIndexed { index, level ->
                                val height = level * size.height * (if (isListening) 1.0f else 0.15f)
                                val x = index * (barWidth + spacing) + (size.width - liveWaveform.size * (barWidth + spacing)) / 2f
                                
                                drawRoundRect(
                                    brush = brush,
                                    topLeft = Offset(x, centerY - height / 2f),
                                    size = Size(barWidth, height),
                                    cornerRadius = CornerRadius(4f, 4f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Transcribed Speech Text Bubble
                    val placeholderHint = when(selectedLanguage) {
                        VoiceLanguage.ENGLISH -> "Speak a command in English...\n(e.g., \"open VLC\", \"sing a song\", \"play music\")"
                        VoiceLanguage.KELANTAN -> "Kecek dalam Baso Kelate boh...\n(cth: \"buko VLC\", \"main lagu\", \"nyanyi lagu\")"
                        VoiceLanguage.MALAY -> "Sebut sesuatu dalam Bahasa Melayu...\n(cth: \"buka VLC\", \"mainkan lagu\", \"buka whatsapp\")"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (speechText.isEmpty()) placeholderHint else "\"$speechText\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // MIC BUTTON & STANDBY STATUS ROW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Mic Button
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Color.Red.copy(alpha = 0.15f) 
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) Color.Red 
                                else MaterialTheme.colorScheme.primary
                            )
                            .clickable { onListeningToggle() }
                            .testTag("mic_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = "Mic Toggle Button",
                            tint = if (isListening) Color.White else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = if (isListening) "Mendengar..." else "Tekan Mikrofon untuk Mula",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isListening) Color.Red else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // GOOGLE VOICE TRANSLATOR COMPONENT
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Google Voice Translator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Switch(
                            checked = isTranslationMode,
                            onCheckedChange = { onTranslationModeToggle(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("translation_toggle_switch")
                        )
                    }

                    if (isTranslationMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Terjemah Bahasa Melayu ke:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val languages = listOf("en" to "Inggeris (EN)", "zh" to "Cina (ZH)", "ar" to "Arab (AR)", "ja" to "Jepun (JA)")
                            languages.forEach { (code, label) ->
                                val isSelected = translatorTargetLang == code
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary 
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { onTargetLanguageSelect(code) }
                                        .border(
                                            1.dp, 
                                            if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline, 
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label.split(" ")[0],
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (translatedText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "TERJEMAHAN (${translatorTargetLang.uppercase()}):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = translatedText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // CONTINUOUS BACKGROUND WAKE WORD "hey sya"
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isWakeStandby) Color.Green else Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Mod Siaga 'Hey Sya' Selalu Aktif",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Benarkan peranti bangun & dengar arahan jika sebut 'hey sya' walaupun sedang sleep.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = isWakeWordActive,
                        onCheckedChange = { onWakeWordToggle(it) },
                        modifier = Modifier.testTag("wake_word_active_switch")
                    )
                }
            }
        }

        // QUICK CONTROL KONSOL PANTAS
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "KONSOL KAWALAN PANTAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // VLC Quick Button (Custom outline style)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .clickable { onManualTrigger("buka vlc") }
                            .testTag("btn_trigger_vlc"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BUKA VLC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // WhatsApp Quick Button (Custom outline style)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .clickable { onManualTrigger("buka whatsapp") }
                            .testTag("btn_trigger_whatsapp"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Message,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WHATSAPP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Camera Android Quick Button (Custom primaryContainer style)
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .clickable { onManualTrigger("android kamera") }
                            .testTag("btn_trigger_camera"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KAMERA ANDROID",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WifiDevicesTabScreen(
    devices: List<NetworkDevice>,
    serverIp: String,
    isDeployingCompanion: Boolean,
    companionDeployProgress: Float,
    companionDeployStatus: String,
    companionDeployUrl: String,
    onTriggerAction: (String, String) -> Unit,
    onSimulateDevice: () -> Unit,
    onDeployWebCompanion: () -> Unit,
    onAddDeviceManually: (String, String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var inputIp by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Windows") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Peranti Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputIp,
                        onValueChange = { inputIp = it },
                        label = { Text("Alamat IP Peranti") },
                        placeholder = { Text("cth: 192.168.1.100") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_device_ip_input")
                    )
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Nama Peranti / PC") },
                        placeholder = { Text("cth: PC Utama") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_device_name_input")
                    )
                    Column {
                        Text("Sistem Operasi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == "Windows", onClick = { selectedType = "Windows" })
                                Text("Windows", fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == "Android", onClick = { selectedType = "Android" })
                                Text("Android", fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedType == "Linux", onClick = { selectedType = "Linux" })
                                Text("Linux", fontSize = 13.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputIp.isNotEmpty() && inputName.isNotEmpty()) {
                            onAddDeviceManually(inputIp, inputName, selectedType)
                            showAddDialog = false
                            inputIp = ""
                            inputName = ""
                        }
                    },
                    modifier = Modifier.testTag("btn_add_device_confirm")
                ) {
                    Text("Tambah")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER ROW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Peranti WiFi Terhubung",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("btn_add_device_dialog")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah")
                    }
                    TextButton(
                        onClick = { onSimulateDevice() },
                        modifier = Modifier.testTag("btn_simulate_device")
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulasi Peranti")
                    }
                }
            }
        }

        // COMPANION WEB REMOTE DEPLOYER CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.OpenInBrowser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Auto-Deploy Portal Companion",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Hantar dashboard kawalan ke web hosting percuma untuk kawalan jauh dari mana sahaja.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isDeployingCompanion) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = companionDeployStatus,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${(companionDeployProgress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = companionDeployProgress,
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                            )
                        }
                    } else {
                        Button(
                            onClick = { onDeployWebCompanion() },
                            modifier = Modifier.fillMaxWidth().testTag("btn_deploy_companion"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deploy ke Web Hosting Percuma (Vercel/Netlify)")
                        }
                    }

                    if (companionDeployUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Green.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Selesai di-deploy!",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Green
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = companionDeployUrl,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // WiFi DEVICES CONTAINER
        if (devices.isEmpty()) {
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Computer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tiada Peranti Terhubung",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pastikan komputer Windows anda terhubung ke WiFi yang sama.\nPOST to http://$serverIp:8080/register",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { onSimulateDevice() }) {
                            Text("Sambungkan Klien Simulasi (Razif PC)")
                        }
                    }
                }
            }
        } else {
            items(devices) { device ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Computer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = device.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "IP: ${device.ip}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Online Status Badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (device.isOnline) Color.Green.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (device.isOnline) "ONLINE" else "OFFLINE",
                                    color = if (device.isOnline) Color.Green else Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Triggers for this PC
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButtonSmall(
                                text = "Buka VLC",
                                onClick = { onTriggerAction(device.ip, "LAUNCH_VLC") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButtonSmall(
                                text = "Buka WhatsApp",
                                onClick = { onTriggerAction(device.ip, "LAUNCH_WHATSAPP") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutlinedButtonSmall(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun VoiceProfileTabScreen(
    isTrainingActive: Boolean,
    trainingPhase: Int,
    trainingProgress: Float,
    trainingName: String,
    trainingStatusText: String,
    allProfiles: List<VoiceProfile>,
    liveWaveform: List<Float>,
    onStartGuidedTraining: (String) -> Unit,
    onStopGuidedTraining: () -> Unit,
    onRecordPhrase: () -> Unit,
    onDeleteProfile: (Int) -> Unit,
    onProfileStrictToggle: (Int, Boolean) -> Unit
) {
    var inputName by remember { mutableStateOf("") }
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MODULE TITLE
        item {
            Text(
                text = "Modul Latihan Profil Suara AI",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Daftar dan urus cap suara biometrik unik untuk pengesahan akses kawalan suara.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // GUIDED VOICE TRAINING WIZARD / REGISTER CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isTrainingActive) {
                        // Training Wizard Active State
                        Text(
                            text = "LATIHAN AKTIF: PROFIL ${trainingName.uppercase()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive steps indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (step in 1..3) {
                                val stepDone = trainingPhase >= step
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(
                                            color = if (stepDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Prompt phrase to speak
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val phraseToSpeak = when (trainingPhase) {
                                1 -> "Sebut: \"BAIK XIAOXI\""
                                2 -> "Sebut: \"BUKA VLC SEKARANG\""
                                else -> "Sebut: \"BUKA WHATSAPP SEKARANG\""
                            }
                            Text(
                                text = phraseToSpeak,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live visualizer inside wizard
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val barWidth = 6f
                                val spacing = 4f
                                val centerY = size.height / 2f
                                val brush = Brush.linearGradient(
                                    colors = listOf(primaryColor, tertiaryColor)
                                )
                                liveWaveform.take(24).forEachIndexed { index, level ->
                                    val height = level * size.height
                                    val x = index * (barWidth + spacing) + (size.width - 24 * (barWidth + spacing)) / 2f
                                    drawRoundRect(
                                        brush = brush,
                                        topLeft = Offset(x, centerY - height / 2f),
                                        size = Size(barWidth, height),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = trainingStatusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onRecordPhrase() },
                                modifier = Modifier.weight(1.5f).testTag("btn_wizard_record"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rakam Frasa")
                            }

                            Button(
                                onClick = { onStopGuidedTraining() },
                                modifier = Modifier.weight(1f).testTag("btn_wizard_cancel"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                            ) {
                                Text("Batal", color = Color.White)
                            }
                        }
                    } else {
                        // New Voice Profile Enrollment Form
                        Text(
                            text = "Daftar Profil Suara Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Nama Pengguna (cth: Razif)") },
                            modifier = Modifier.fillMaxWidth().testTag("training_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (inputName.isNotBlank()) {
                                    onStartGuidedTraining(inputName.trim())
                                    inputName = ""
                                }
                            },
                            enabled = inputName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("btn_start_training_wizard")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mula Wizard Latihan")
                        }
                    }
                }
            }
        }

        // ENROLLED PROFILES LIST CONTAINER
        item {
            Text(
                text = "Senarai Profil Suara Berdaftar",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (allProfiles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tiada profil suara berdaftar. Sila daftarkan suara anda (cth: Razif) di atas.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(allProfiles) { profile ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Fingerprint,
                                    contentDescription = null,
                                    tint = if (profile.isEnrolled) Color.Green else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = profile.name.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Pitch: ${profile.averagePitch.toInt()}Hz • Sampel: ${if (profile.isEnrolled) "3/3" else "0/3"}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Delete Profile
                            IconButton(
                                onClick = { onDeleteProfile(profile.id) },
                                modifier = Modifier.testTag("btn_delete_profile_${profile.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Hapus Profil",
                                    tint = Color.Red.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Strict verification toggle for this individual profile
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pengesahan Biometrik Suara Tegas",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Hanya terima arahan daripada ${profile.name} jika fingerprint padan (>70%).",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Switch(
                                checked = profile.strictVerification,
                                onCheckedChange = { onProfileStrictToggle(profile.id, it) },
                                modifier = Modifier.testTag("strict_toggle_${profile.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogTabScreen(
    logs: List<VoiceCommandLog>,
    onClearLogs: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Log Aktiviti Xiaoxi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = { onClearLogs() },
                modifier = Modifier.testTag("btn_clear_logs")
            ) {
                Icon(
                    imageVector = Icons.Filled.ClearAll,
                    contentDescription = "Clear Logs",
                    tint = Color.Red
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Tiada log aktiviti dikesan",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "\"${log.command}\"",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Aksi: ${log.parsedAction} • " + SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(log.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = when {
                                            log.status == "SUKSES" -> Color.Green.copy(alpha = 0.15f)
                                            log.status.contains("REJECTED") || log.status.contains("AKSES DITOLAK") -> Color.Red.copy(alpha = 0.15f)
                                            else -> Color.Yellow.copy(alpha = 0.15f)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = log.status,
                                    color = when {
                                        log.status == "SUKSES" -> Color.Green
                                        log.status.contains("REJECTED") || log.status.contains("AKSES DITOLAK") -> Color.Red
                                        else -> Color.DarkGray
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongGalleryTabScreen(
    songs: List<SongItem>,
    onSingSong: (SongItem) -> Unit,
    onPlaySong: (SongItem) -> Unit,
    onPlayAllMusic: () -> Unit
) {
    var expandedSongId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Playlist Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayAllMusic() }
                    .testTag("playlist_hero_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Senarai Main Utama YouTube",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mainkan semua lagu & muzik kegemaran anda secara automatik di YouTube.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(
                            onClick = onPlayAllMusic,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play All",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Galeri Lagu Xiaoxi AI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Individual Songs List
        items(songs) { song ->
            val isExpanded = expandedSongId == song.id
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = song.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Hip-Hop Melayu",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Text(
                                text = "oleh ${song.artist}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { expandedSongId = if (isExpanded) null else song.id }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = "Toggle Lyrics"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = song.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSingSong(song) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_sing_${song.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nyanyikan (AI)", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onPlaySong(song) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_play_${song.id}"),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tonton Video", fontSize = 12.sp)
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Lirik Lagu:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = song.lyrics,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to manage Voice Recognition AI Language Models, Download Packages & Test Sample Commands
 */
@Composable
fun VoiceLanguageManagerDialog(
    selectedLanguage: VoiceLanguage,
    voiceModelPackages: Map<VoiceLanguage, VoiceModelPackage>,
    onSelectLanguage: (VoiceLanguage) -> Unit,
    onDownloadModel: (VoiceLanguage) -> Unit,
    onDeleteModel: (VoiceLanguage) -> Unit,
    onTestCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_close_language_dialog")
            ) {
                Text("Tutup")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Model Suara & Pakej Bahasa AI",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Muat turun model neural luar talian & tukar bahasa",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Pilih bahasa utama untuk pengiktirafan suara, sintesis pertuturan (TTS), dan pemprosesan Gemini AI:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(VoiceLanguage.entries.size) { index ->
                    val lang = VoiceLanguage.entries[index]
                    val isSelected = selectedLanguage == lang
                    val pkg = voiceModelPackages[lang] ?: VoiceModelPackage(lang)

                    VoiceModelItemCard(
                        language = lang,
                        packageInfo = pkg,
                        isSelected = isSelected,
                        onSelect = { onSelectLanguage(lang) },
                        onDownload = { onDownloadModel(lang) },
                        onDelete = { onDeleteModel(lang) },
                        onTestCommand = onTestCommand
                    )
                }
            }
        }
    )
}

@Composable
fun VoiceModelItemCard(
    language: VoiceLanguage,
    packageInfo: VoiceModelPackage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onTestCommand: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("model_card_${language.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = language.flag,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = language.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AKTIF",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${packageInfo.acousticEngine} • ${language.modelSizeMb} MB",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Download / Select Button
                if (packageInfo.isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (packageInfo.isDownloaded) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Sedang Aktif",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Button(
                            onClick = onSelect,
                            modifier = Modifier.testTag("btn_select_lang_${language.id}"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Gunakan", fontSize = 11.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.testTag("btn_download_lang_${language.id}"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Muat Turun", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = language.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Progress bar if downloading
            if (packageInfo.isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = packageInfo.downloadProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = packageInfo.downloadStatusText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(6.dp))

            // Sample commands & details expander
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Sembunyikan Contoh Arahan ▲" else "Lihat Contoh Arahan Suara ▼",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (packageInfo.isDownloaded && language != VoiceLanguage.MALAY && !isSelected) {
                    TextButton(
                        onClick = onDelete,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Padam Model", fontSize = 10.sp, color = Color.Red)
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Contoh Arahan (Klik untuk uji terus):",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                val sampleCommands = when (language) {
                    VoiceLanguage.MALAY -> listOf("buka VLC", "buka whatsapp", "buka kamera", "mainkan lagu", "menyanyi lagu", "apa khabar hari ini?")
                    VoiceLanguage.ENGLISH -> listOf("open VLC", "open whatsapp", "open camera", "play music", "sing a song", "what is your name?")
                    VoiceLanguage.KELANTAN -> listOf("buko VLC", "buko wasak", "buko kemera", "main lagu", "tarik lagu sebutir", "guano cerito arini?")
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sampleCommands.chunked(2).forEach { rowCmds ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            rowCmds.forEach { cmd ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .clickable { 
                                            if (!isSelected && packageInfo.isDownloaded) {
                                                onSelect()
                                            }
                                            onTestCommand(cmd) 
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🗣️ \"$cmd\"",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
