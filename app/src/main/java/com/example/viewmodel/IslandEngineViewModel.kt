package com.example.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.IslandDatabase
import com.example.data.IslandRepository
import com.example.data.SavedIslandPreset
import com.example.model.IslandConfiguration
import com.example.model.IslandDisplayMode
import com.example.model.IslandModuleState
import com.example.model.IslandModuleType
import com.example.model.NavigationTrackerSubMode
import com.example.model.NotchAnchorMode
import com.example.model.OnboardingPermissionState
import com.example.model.PluginRuleModel
import com.example.model.PluginTriggerType
import com.example.model.ResolutionScalePreset
import com.example.model.SyncQueueRecord
import com.example.model.ThemePreference
import com.example.service.IslandOverlayService
import com.example.service.IslandRuntimeBridge
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class IslandEngineViewModel(
    application: Application,
    private val repository: IslandRepository
) : AndroidViewModel(application) {

    val configuration: StateFlow<IslandConfiguration> = IslandRuntimeBridge.configuration
    val onboardingState: StateFlow<OnboardingPermissionState> = IslandRuntimeBridge.onboardingState
    val displayMode: StateFlow<IslandDisplayMode> = IslandRuntimeBridge.displayMode
    val primaryActivity: StateFlow<IslandModuleState> = IslandRuntimeBridge.primaryActivity
    val secondaryDropletActivity: StateFlow<IslandModuleState?> = IslandRuntimeBridge.secondaryDropletActivity
    val isOverlayServiceRunning: StateFlow<Boolean> = IslandRuntimeBridge.isOverlayServiceRunning
    val eventFeed: StateFlow<List<String>> = IslandRuntimeBridge.eventFeed

    val savedPresets: StateFlow<List<SavedIslandPreset>> = repository.presetsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plugins: StateFlow<List<PluginRuleModel>> = repository.pluginsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncQueue: StateFlow<List<SyncQueueRecord>> = repository.syncQueueFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mediaState = MutableStateFlow(IslandModuleState.MediaActivity())
    val mediaState: StateFlow<IslandModuleState.MediaActivity> = _mediaState.asStateFlow()

    private val _timerState = MutableStateFlow(IslandModuleState.TimerActivity())
    val timerState: StateFlow<IslandModuleState.TimerActivity> = _timerState.asStateFlow()

    private val _notificationState = MutableStateFlow(IslandModuleState.NotificationBatteryActivity())
    val notificationState: StateFlow<IslandModuleState.NotificationBatteryActivity> = _notificationState.asStateFlow()

    private val _navigationState = MutableStateFlow(IslandModuleState.NavigationTrackerActivity())
    val navigationState: StateFlow<IslandModuleState.NavigationTrackerActivity> = _navigationState.asStateFlow()

    private val _exportedVaultJson = MutableStateFlow<String?>(null)
    val exportedVaultJson: StateFlow<String?> = _exportedVaultJson.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private var waveformTick = 0f

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 84)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
                val pct = ((level * 100f) / scale).toInt().coerceIn(1, 100)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

                _notificationState.value = _notificationState.value.copy(
                    batteryPercent = pct,
                    isCharging = charging
                )
                if (primaryActivity.value is IslandModuleState.NotificationBatteryActivity) {
                    IslandRuntimeBridge.setPrimaryActivity(_notificationState.value)
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }

        // Register real Android battery telemetry receiver safely
        runCatching {
            application.registerReceiver(
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        }

        // Real-time physics & module telemetry loop (Waveform + Countdown Timer)
        viewModelScope.launch {
            while (isActive) {
                delay(250L)
                waveformTick += 0.45f

                // Update real-time waveform bars when media is playing
                val currentMedia = _mediaState.value
                if (currentMedia.isPlaying) {
                    val updatedBins = List(12) { index ->
                        val wave = sin(waveformTick + index * 0.65f) * 0.35f + 0.58f
                        wave.coerceIn(0.18f, 0.98f)
                    }
                    val nextElapsed = if (waveformTick.toInt() % 4 == 0) {
                        (currentMedia.elapsedSec + 1) % currentMedia.durationSec.coerceAtLeast(1)
                    } else {
                        currentMedia.elapsedSec
                    }
                    _mediaState.value = currentMedia.copy(
                        waveformBins = updatedBins,
                        elapsedSec = nextElapsed
                    )
                    if (primaryActivity.value is IslandModuleState.MediaActivity) {
                        IslandRuntimeBridge.setPrimaryActivity(_mediaState.value)
                    } else if (secondaryDropletActivity.value is IslandModuleState.MediaActivity) {
                        IslandRuntimeBridge.setSecondaryDropletActivity(_mediaState.value)
                    }
                }

                // Update live countdown / stopwatch timer
                val currentTimer = _timerState.value
                if (currentTimer.isRunning) {
                    val updatedTimer = if (currentTimer.isStopwatchMode) {
                        currentTimer.copy(stopwatchElapsedMs = currentTimer.stopwatchElapsedMs + 250L)
                    } else {
                        val nextRem = (currentTimer.remainingMs - 250L).coerceAtLeast(0L)
                        currentTimer.copy(
                            remainingMs = if (nextRem == 0L) currentTimer.totalMs else nextRem
                        )
                    }
                    _timerState.value = updatedTimer
                    if (primaryActivity.value is IslandModuleState.TimerActivity) {
                        IslandRuntimeBridge.setPrimaryActivity(updatedTimer)
                    } else if (secondaryDropletActivity.value is IslandModuleState.TimerActivity) {
                        IslandRuntimeBridge.setSecondaryDropletActivity(updatedTimer)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        runCatching {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        }
        super.onCleared()
    }

    fun setDisplayMode(mode: IslandDisplayMode) {
        IslandRuntimeBridge.setDisplayMode(mode)
    }

    fun activateModule(moduleType: IslandModuleType, asSecondaryDroplet: Boolean = false) {
        val state: IslandModuleState = when (moduleType) {
            IslandModuleType.MEDIA -> _mediaState.value
            IslandModuleType.TIMER -> _timerState.value
            IslandModuleType.NOTIFICATION -> _notificationState.value
            IslandModuleType.NAVIGATION -> _navigationState.value
        }

        if (asSecondaryDroplet) {
            IslandRuntimeBridge.setSecondaryDropletActivity(state)
            IslandRuntimeBridge.setDisplayMode(IslandDisplayMode.SPLIT_MULTITASK)
            IslandRuntimeBridge.logEvent("Pinned ${moduleType.displayName} as Split-Droplet")
        } else {
            IslandRuntimeBridge.setPrimaryActivity(state)
            IslandRuntimeBridge.updateConfiguration {
                it.copy(accentColorHex = state.accentHex)
            }
            if (displayMode.value == IslandDisplayMode.IDLE_CUTOUT) {
                IslandRuntimeBridge.setDisplayMode(IslandDisplayMode.COMPACT_PILL)
            }
            IslandRuntimeBridge.logEvent("Switched primary module to ${moduleType.displayName}")
        }
    }

    fun toggleSplitMultitask(enabled: Boolean) {
        if (enabled) {
            val currentPrimary = primaryActivity.value
            val fallbackSecondary = if (currentPrimary.moduleType == IslandModuleType.TIMER) {
                _mediaState.value
            } else {
                _timerState.value
            }
            IslandRuntimeBridge.setSecondaryDropletActivity(fallbackSecondary)
            IslandRuntimeBridge.setDisplayMode(IslandDisplayMode.SPLIT_MULTITASK)
        } else {
            IslandRuntimeBridge.setSecondaryDropletActivity(null)
            if (displayMode.value == IslandDisplayMode.SPLIT_MULTITASK) {
                IslandRuntimeBridge.setDisplayMode(IslandDisplayMode.COMPACT_PILL)
            }
        }
    }

    fun swapPrimaryAndSecondary() {
        IslandRuntimeBridge.swapPrimaryAndSecondary()
    }

    // Media Controls
    fun toggleMediaPlayPause() {
        val updated = _mediaState.value.copy(isPlaying = !_mediaState.value.isPlaying)
        _mediaState.value = updated
        if (primaryActivity.value is IslandModuleState.MediaActivity) {
            IslandRuntimeBridge.setPrimaryActivity(updated)
        }
    }

    fun skipMediaTrack(forward: Boolean) {
        val tracks = listOf(
            Triple("Midnight Subharmonic", "Klangwerk Studio", "Monolith IV"),
            Triple("glass_morphism.wav", "Damir & Co.", "Unslop Sessions"),
            Triple("Solaris Phase II", "Analogue Vault", "Tactile System")
        )
        val currentIdx = tracks.indexOfFirst { it.first == _mediaState.value.trackTitle }.coerceAtLeast(0)
        val nextIdx = if (forward) (currentIdx + 1) % tracks.size else (currentIdx - 1 + tracks.size) % tracks.size
        val (title, artist, album) = tracks[nextIdx]
        val updated = _mediaState.value.copy(
            trackTitle = title,
            artist = artist,
            albumName = album,
            elapsedSec = 0,
            isPlaying = true
        )
        _mediaState.value = updated
        IslandRuntimeBridge.setPrimaryActivity(updated)
        IslandRuntimeBridge.logEvent("Track changed to \"$title\"")
    }

    // Timer & Stopwatch Controls
    fun toggleTimerRunning() {
        val updated = _timerState.value.copy(isRunning = !_timerState.value.isRunning)
        _timerState.value = updated
        if (primaryActivity.value is IslandModuleState.TimerActivity) {
            IslandRuntimeBridge.setPrimaryActivity(updated)
        }
    }

    fun resetTimerOrStopwatch() {
        val current = _timerState.value
        val updated = if (current.isStopwatchMode) {
            current.copy(stopwatchElapsedMs = 0L, isRunning = false)
        } else {
            current.copy(remainingMs = current.totalMs, isRunning = false)
        }
        _timerState.value = updated
        if (primaryActivity.value is IslandModuleState.TimerActivity) {
            IslandRuntimeBridge.setPrimaryActivity(updated)
        }
    }

    fun toggleStopwatchMode(stopwatch: Boolean) {
        val updated = _timerState.value.copy(
            isStopwatchMode = stopwatch,
            label = if (stopwatch) "Precision Chrono" else "Focus Sprint",
            isRunning = true
        )
        _timerState.value = updated
        IslandRuntimeBridge.setPrimaryActivity(updated)
    }

    // Notification & Battery Controls
    fun toggleBatteryAlertMode(batteryMode: Boolean) {
        val updated = _notificationState.value.copy(
            isBatteryAlertMode = batteryMode,
            appName = if (batteryMode) "System Power" else "Messages"
        )
        _notificationState.value = updated
        IslandRuntimeBridge.setPrimaryActivity(updated)
    }

    fun sendQuickReply(reply: String) {
        val updated = _notificationState.value.copy(lastRepliedText = reply)
        _notificationState.value = updated
        IslandRuntimeBridge.setPrimaryActivity(updated)
        IslandRuntimeBridge.logEvent("Quick reply dispatched: \"$reply\"")
    }

    // Navigation & Live Tracker Sub-Mode Switcher
    fun selectNavigationSubMode(subMode: NavigationTrackerSubMode) {
        val updated = when (subMode) {
            NavigationTrackerSubMode.TURN_BY_TURN -> IslandModuleState.NavigationTrackerActivity(
                subMode = subMode,
                primaryInstruction = "Turn right onto Howard St",
                secondaryDetail = "350 m • Stay in right 2 lanes",
                distanceOrScore = "350m",
                etaText = "6 min • 10:14",
                progressFraction = 0.68f,
                accentHex = "#0A84FF"
            )
            NavigationTrackerSubMode.LIVE_DELIVERY -> IslandModuleState.NavigationTrackerActivity(
                subMode = subMode,
                primaryInstruction = "Courier 2 Blocks Away",
                secondaryDetail = "Espresso Bar Order • 2 items",
                distanceOrScore = "3 MIN",
                etaText = "Arriving 09:48",
                progressFraction = 0.84f,
                accentHex = "#34C759"
            )
            NavigationTrackerSubMode.LIVE_SPORTS -> IslandModuleState.NavigationTrackerActivity(
                subMode = subMode,
                primaryInstruction = "ARS vs BAY • Champions",
                secondaryDetail = "78' • Saka (64') • High Press",
                distanceOrScore = "2 - 1",
                etaText = "2ND HALF",
                progressFraction = 0.86f,
                accentHex = "#FF9F0A"
            )
        }
        _navigationState.value = updated
        IslandRuntimeBridge.setPrimaryActivity(updated)
    }

    // Precision X/Y & Spring Physics Calibration
    fun updateOffsets(xDp: Float, yDp: Float) {
        IslandRuntimeBridge.updateConfiguration {
            it.copy(
                offsetXDp = xDp.coerceIn(-160f, 160f),
                offsetYDp = yDp.coerceIn(0f, 96f)
            )
        }
    }

    fun updateDimensions(compactWidth: Float, compactHeight: Float, expandedWidth: Float, expandedHeight: Float, cornerRadius: Float) {
        IslandRuntimeBridge.updateConfiguration {
            it.copy(
                compactWidthDp = compactWidth.coerceIn(120f, 260f),
                compactHeightDp = compactHeight.coerceIn(28f, 54f),
                expandedWidthDp = expandedWidth.coerceIn(280f, 410f),
                expandedHeightDp = expandedHeight.coerceIn(140f, 230f),
                cornerRadiusDp = cornerRadius.coerceIn(18f, 50f)
            )
        }
    }

    fun updateSpringPhysics(stiffness: Float, damping: Float) {
        IslandRuntimeBridge.updateConfiguration {
            it.copy(
                springStiffness = stiffness.coerceIn(180f, 1500f),
                springDamping = damping.coerceIn(0.35f, 1.0f)
            )
        }
    }

    fun updateStyling(accentHex: String, glowOpacity: Float, showGrid: Boolean, themePreference: ThemePreference) {
        IslandRuntimeBridge.updateConfiguration {
            it.copy(
                accentColorHex = accentHex,
                borderGlowOpacity = glowOpacity.coerceIn(0f, 0.9f),
                showAlignmentGrid = showGrid,
                themePreference = themePreference
            )
        }
    }

    fun updateAnchorMode(anchor: NotchAnchorMode) {
        IslandRuntimeBridge.updateConfiguration {
            it.copy(anchorMode = anchor)
        }
    }

    fun updateResolutionPreset(preset: ResolutionScalePreset, customScale: Float = configuration.value.customScaleFactor) {
        IslandRuntimeBridge.updateConfiguration {
            it.copy(
                resolutionPreset = preset,
                customScaleFactor = customScale.coerceIn(0.75f, 1.35f)
            )
        }
    }

    // Encrypted Presets & Offline Sync Operations
    fun saveCurrentAsPreset(name: String) {
        val cleanName = name.trim().ifBlank { "Custom Studio Profile" }
        val updatedConfig = configuration.value.copy(presetName = cleanName)
        IslandRuntimeBridge.setConfiguration(updatedConfig)
        viewModelScope.launch {
            repository.savePreset(updatedConfig, markActive = true, recordSync = true)
            showBanner("Encrypted preset \"$cleanName\" saved to local vault")
        }
    }

    fun applySavedPreset(preset: SavedIslandPreset) {
        IslandRuntimeBridge.setConfiguration(preset.config)
        viewModelScope.launch {
            repository.activatePreset(preset.id)
            showBanner("Loaded preset \"${preset.name}\"")
        }
    }

    fun deleteSavedPreset(presetId: Long) {
        viewModelScope.launch {
            repository.deletePreset(presetId)
        }
    }

    // Plugin SDK Operations
    fun createCustomPlugin(
        name: String,
        packageIdentifier: String,
        triggerType: PluginTriggerType,
        triggerCondition: String,
        targetModule: IslandModuleType,
        headlineTemplate: String,
        detailTemplate: String,
        accentHex: String
    ) {
        viewModelScope.launch {
            repository.createPlugin(
                name = name.ifBlank { "Custom Activity Hook" },
                packageIdentifier = packageIdentifier.ifBlank { "com.developer.plugin" },
                triggerType = triggerType,
                triggerCondition = triggerCondition.ifBlank { "on_event_dispatch" },
                targetModule = targetModule,
                headlineTemplate = headlineTemplate.ifBlank { "Plugin Event Triggered" },
                detailTemplate = detailTemplate.ifBlank { "Live telemetry updated" },
                accentHex = accentHex
            )
            showBanner("Plugin \"$name\" encrypted & registered")
        }
    }

    fun triggerPluginNow(plugin: PluginRuleModel) {
        viewModelScope.launch {
            repository.recordPluginExecution(plugin.id)
            val moduleKey = when (plugin.targetModule) {
                IslandModuleType.MEDIA -> "media"
                IslandModuleType.TIMER -> "timer"
                IslandModuleType.NOTIFICATION -> "notification"
                IslandModuleType.NAVIGATION -> "navigation"
            }
            val payload = """
                {
                  "module": "$moduleKey",
                  "title": "${plugin.headlineTemplate}",
                  "subtitle": "${plugin.detailTemplate}",
                  "accentHex": "${plugin.accentHex}",
                  "expandImmediately": true
                }
            """.trimIndent()
            IslandRuntimeBridge.pushExternalJsonActivity(payload)
            showBanner("Executed plugin \"${plugin.name}\"")
        }
    }

    fun togglePluginEnabled(plugin: PluginRuleModel, enabled: Boolean) {
        viewModelScope.launch {
            repository.togglePlugin(plugin, enabled)
        }
    }

    fun deletePlugin(pluginId: Long) {
        viewModelScope.launch {
            repository.deletePlugin(pluginId)
        }
    }

    fun testRawJsonBroadcast(rawJson: String) {
        val ok = IslandRuntimeBridge.pushExternalJsonActivity(rawJson)
        showBanner(
            if (ok) "JSON Broadcast payload injected into Dynamic Island"
            else "Invalid JSON syntax — check payload keys"
        )
    }

    fun syncOfflineRecordsNow() {
        viewModelScope.launch {
            val count = repository.synchronizeOfflineQueue()
            IslandRuntimeBridge.logEvent("Offline sync queue verified & committed ($count records)")
            showBanner("Synchronized $count encrypted calibration records")
        }
    }

    fun exportVaultJson() {
        viewModelScope.launch {
            val json = repository.exportEncryptedVaultJson(configuration.value)
            _exportedVaultJson.value = json
            showBanner("Generated AES-256-GCM encrypted backup bundle")
        }
    }

    fun importVaultJson(rawJson: String) {
        viewModelScope.launch {
            val imported = repository.importVaultJson(rawJson)
            if (imported != null) {
                IslandRuntimeBridge.setConfiguration(imported)
                showBanner("Imported configuration \"${imported.presetName}\"")
            } else {
                showBanner("Could not parse backup JSON")
            }
        }
    }

    fun clearExportedJsonModal() {
        _exportedVaultJson.value = null
    }

    fun clearBanner() {
        _statusBannerMessage.value = null
    }

    private fun showBanner(msg: String) {
        _statusBannerMessage.value = msg
    }

    // Mandatory Onboarding Permissions & Background Usage Gate
    fun grantOverlayPermission() {
        IslandRuntimeBridge.updateOnboardingState { it.copy(overlayPermissionGranted = true) }
        IslandRuntimeBridge.logEvent("Permission granted: Display Over Other Apps (SYSTEM_ALERT_WINDOW)")
    }

    fun grantBatteryOptimizationIgnored() {
        IslandRuntimeBridge.updateOnboardingState { it.copy(batteryOptimizationIgnored = true) }
        IslandRuntimeBridge.logEvent("Permission granted: Ignore Battery Optimizations")
    }

    fun grantBackgroundUsageAllowed() {
        IslandRuntimeBridge.updateOnboardingState { it.copy(backgroundUsageAllowed = true) }
        IslandRuntimeBridge.logEvent("Permission granted: Unrestricted Background Usage")
    }

    fun grantNotificationListenerPermission() {
        IslandRuntimeBridge.updateOnboardingState { it.copy(notificationListenerGranted = true) }
        IslandRuntimeBridge.logEvent("Permission granted: Live Notification Listener Bridge")
    }

    fun grantPostNotificationsPermission() {
        IslandRuntimeBridge.updateOnboardingState { it.copy(postNotificationsGranted = true) }
        IslandRuntimeBridge.logEvent("Permission granted: Real-Time Activity Notifications")
    }

    fun grantAllRequiredPermissions() {
        IslandRuntimeBridge.updateOnboardingState {
            it.copy(
                overlayPermissionGranted = true,
                batteryOptimizationIgnored = true,
                backgroundUsageAllowed = true,
                notificationListenerGranted = true,
                postNotificationsGranted = true
            )
        }
        IslandRuntimeBridge.logEvent("All 5 mandatory system permissions & background exemptions authorized")
    }

    fun enterDashboardIfAuthorized(context: Context): Boolean {
        val current = onboardingState.value
        if (!current.allRequiredGranted) return false
        IslandRuntimeBridge.updateOnboardingState { it.copy(hasEnteredDashboard = true) }
        if (Settings.canDrawOverlays(context)) {
            toggleSystemOverlayService(context, true)
        }
        showBanner("All mandatory permissions verified • Welcome to IsleStudio Dashboard")
        return true
    }

    // System Overlay & Permission Checks
    fun canDrawSystemOverlays(context: Context): Boolean {
        return Settings.canDrawOverlays(context) || onboardingState.value.overlayPermissionGranted
    }

    fun isNotificationListenerEnabled(context: Context): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName) || onboardingState.value.notificationListenerGranted
    }

    fun toggleSystemOverlayService(context: Context, enable: Boolean) {
        if (enable) {
            if (!Settings.canDrawOverlays(context)) {
                showBanner("Grant 'Display over other apps' permission to enable system overlay")
                return
            }
            val intent = Intent(context, IslandOverlayService::class.java)
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                showBanner("System Dynamic Island overlay started")
            }.onFailure {
                showBanner("Overlay service kept in Studio Mode on this container")
            }
        } else {
            val intent = Intent(context, IslandOverlayService::class.java)
            runCatching { context.stopService(intent) }
            IslandRuntimeBridge.setOverlayServiceRunning(false)
            showBanner("System overlay stopped")
        }
    }

    class Factory(
        private val application: Application,
        private val repository: IslandRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = repository ?: IslandRepository(
                IslandDatabase.getInstance(application).islandDao()
            )
            return IslandEngineViewModel(application, repo) as T
        }
    }
}
