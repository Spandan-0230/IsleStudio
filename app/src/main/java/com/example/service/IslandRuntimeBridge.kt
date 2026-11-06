package com.example.service

import com.example.model.IslandConfiguration
import com.example.model.IslandDisplayMode
import com.example.model.IslandModuleState
import com.example.model.IslandModuleType
import com.example.model.NavigationTrackerSubMode
import com.example.model.OnboardingPermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject

/**
 * Process-wide reactive bridge shared across MainActivity, IslandEngineViewModel,
 * IslandOverlayService (WindowManager floating overlay), IslandNotificationListenerService,
 * and PluginBroadcastReceiver.
 */
object IslandRuntimeBridge {

    private val _configuration = MutableStateFlow(IslandConfiguration())
    val configuration: StateFlow<IslandConfiguration> = _configuration.asStateFlow()

    private val _onboardingState = MutableStateFlow(OnboardingPermissionState())
    val onboardingState: StateFlow<OnboardingPermissionState> = _onboardingState.asStateFlow()

    private val _displayMode = MutableStateFlow(IslandDisplayMode.COMPACT_PILL)
    val displayMode: StateFlow<IslandDisplayMode> = _displayMode.asStateFlow()

    private val _primaryActivity = MutableStateFlow<IslandModuleState>(IslandModuleState.MediaActivity())
    val primaryActivity: StateFlow<IslandModuleState> = _primaryActivity.asStateFlow()

    private val _secondaryDropletActivity = MutableStateFlow<IslandModuleState?>(
        IslandModuleState.TimerActivity()
    )
    val secondaryDropletActivity: StateFlow<IslandModuleState?> = _secondaryDropletActivity.asStateFlow()

    private val _isOverlayServiceRunning = MutableStateFlow(false)
    val isOverlayServiceRunning: StateFlow<Boolean> = _isOverlayServiceRunning.asStateFlow()

    private val _isNotificationListenerConnected = MutableStateFlow(false)
    val isNotificationListenerConnected: StateFlow<Boolean> = _isNotificationListenerConnected.asStateFlow()

    private val _eventFeed = MutableStateFlow<List<String>>(
        listOf(
            "Engine initialized • Spring 520 N/m, ζ=0.68",
            "Split-Droplet multiplexer active (Media + Timer)"
        )
    )
    val eventFeed: StateFlow<List<String>> = _eventFeed.asStateFlow()

    fun updateOnboardingState(transform: (OnboardingPermissionState) -> OnboardingPermissionState) {
        _onboardingState.update(transform)
    }

    fun updateConfiguration(transform: (IslandConfiguration) -> IslandConfiguration) {
        _configuration.update(transform)
    }

    fun setConfiguration(config: IslandConfiguration) {
        _configuration.value = config
    }

    fun setDisplayMode(mode: IslandDisplayMode) {
        _displayMode.value = mode
    }

    fun setPrimaryActivity(activity: IslandModuleState) {
        _primaryActivity.value = activity
    }

    fun setSecondaryDropletActivity(activity: IslandModuleState?) {
        _secondaryDropletActivity.value = activity
    }

    fun setOverlayServiceRunning(running: Boolean) {
        _isOverlayServiceRunning.value = running
        logEvent(if (running) "System Overlay Service attached to WindowManager" else "System Overlay Service detached")
    }

    fun setNotificationListenerConnected(connected: Boolean) {
        _isNotificationListenerConnected.value = connected
        logEvent(if (connected) "NotificationListenerService bound to system bar" else "NotificationListenerService unbound")
    }

    fun logEvent(message: String) {
        _eventFeed.update { current ->
            (listOf(message) + current).take(20)
        }
    }

    fun swapPrimaryAndSecondary() {
        val currentPrimary = _primaryActivity.value
        val currentSecondary = _secondaryDropletActivity.value ?: return
        _primaryActivity.value = currentSecondary
        _secondaryDropletActivity.value = currentPrimary
        logEvent("Swapped primary capsule to ${currentSecondary.moduleType.displayName}")
    }

    /**
     * Parses a developer JSON payload from PluginBroadcastReceiver or the in-app SDK console
     * and dispatches it to the Dynamic Island with priority routing.
     */
    fun pushExternalJsonActivity(rawJson: String): Boolean {
        return runCatching {
            val obj = JSONObject(rawJson)
            val moduleStr = obj.optString("module", "notification").lowercase()
            val title = obj.optString("title", "Live Activity Update")
            val subtitle = obj.optString("subtitle", "External Plugin Event")
            val accent = obj.optString("accentHex", "#0A84FF")
            val expand = obj.optBoolean("expandImmediately", false)

            val newState: IslandModuleState = when (moduleStr) {
                "media" -> IslandModuleState.MediaActivity(
                    trackTitle = title,
                    artist = subtitle,
                    accentHex = accent,
                    isPlaying = true
                )
                "timer" -> {
                    val seconds = obj.optInt("durationSeconds", 300).coerceAtLeast(10)
                    IslandModuleState.TimerActivity(
                        label = title,
                        remainingMs = seconds * 1000L,
                        totalMs = seconds * 1000L,
                        isRunning = true,
                        accentHex = accent
                    )
                }
                "navigation", "delivery", "sports" -> {
                    val subMode = when (moduleStr) {
                        "delivery" -> NavigationTrackerSubMode.LIVE_DELIVERY
                        "sports" -> NavigationTrackerSubMode.LIVE_SPORTS
                        else -> NavigationTrackerSubMode.TURN_BY_TURN
                    }
                    IslandModuleState.NavigationTrackerActivity(
                        subMode = subMode,
                        primaryInstruction = title,
                        secondaryDetail = subtitle,
                        distanceOrScore = obj.optString("badge", "LIVE"),
                        etaText = obj.optString("eta", "Now"),
                        progressFraction = obj.optDouble("progress", 0.72).toFloat().coerceIn(0f, 1f),
                        accentHex = accent
                    )
                }
                else -> IslandModuleState.NotificationBatteryActivity(
                    appName = obj.optString("appName", "Plugin SDK"),
                    sender = title,
                    message = subtitle,
                    accentHex = accent,
                    isBatteryAlertMode = obj.optBoolean("isBatteryAlert", false)
                )
            }

            val previousPrimary = _primaryActivity.value
            if (previousPrimary.moduleType != newState.moduleType) {
                _secondaryDropletActivity.value = previousPrimary
            }
            _primaryActivity.value = newState
            _displayMode.value = if (expand) {
                IslandDisplayMode.EXPANDED_CARD
            } else if (_secondaryDropletActivity.value != null) {
                IslandDisplayMode.SPLIT_MULTITASK
            } else {
                IslandDisplayMode.COMPACT_PILL
            }
            logEvent("Plugin SDK pushed [${newState.moduleType.displayName}]: $title")
            true
        }.getOrDefault(false)
    }
}
