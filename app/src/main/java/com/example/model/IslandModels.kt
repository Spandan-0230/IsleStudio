package com.example.model

import androidx.compose.ui.graphics.Color

enum class IslandDisplayMode(val title: String) {
    IDLE_CUTOUT("Cutout Standby"),
    COMPACT_PILL("Compact Pill"),
    SPLIT_MULTITASK("Split Droplet"),
    EXPANDED_CARD("Expanded Card")
}

enum class NotchAnchorMode(val label: String, val defaultXOffsetDp: Float) {
    CENTER("Center Cutout", 0f),
    LEFT_PUNCH("Left Punch-Hole", -96f),
    RIGHT_PUNCH("Right Punch-Hole", 96f)
}

enum class ResolutionScalePreset(val label: String, val scaleFactor: Float) {
    AUTO_ADAPTIVE("Auto Resolution", 1.0f),
    COMPACT_PHONE("Compact (360dp)", 0.92f),
    STANDARD_PRO("Standard Pro (412dp)", 1.0f),
    FOLDABLE_WIDE("Foldable / Medium", 1.08f),
    TABLET_STUDIO("Tablet / Expanded", 1.16f),
    CUSTOM("Custom Scale", 1.0f)
}

enum class ThemePreference(val label: String) {
    DARK_OBSIDIAN("Dark Obsidian"),
    LIGHT_STUDIO("Light Studio"),
    SYSTEM_AUTO("System Auto")
}

enum class IslandModuleType(val id: String, val displayName: String, val defaultAccentHex: String) {
    MEDIA("media", "Media & Waveform", "#FA2D48"),
    TIMER("timer", "Timer & Stopwatch", "#FF9F0A"),
    NOTIFICATION("notification", "Alerts & Battery", "#34C759"),
    NAVIGATION("navigation", "Nav & Live ETA", "#0A84FF")
}

enum class NavigationTrackerSubMode(val label: String) {
    TURN_BY_TURN("Turn-by-Turn Nav"),
    LIVE_DELIVERY("Courier ETA"),
    LIVE_SPORTS("Live Match Score")
}

data class IslandConfiguration(
    val presetName: String = "iOS 18 Pro Precision",
    val offsetXDp: Float = 0f,
    val offsetYDp: Float = 8f,
    val compactWidthDp: Float = 186f,
    val compactHeightDp: Float = 36f,
    val expandedWidthDp: Float = 348f,
    val expandedHeightDp: Float = 178f,
    val cornerRadiusDp: Float = 42f,
    val splitDropletGapDp: Float = 8f,
    val springStiffness: Float = 520f,
    val springDamping: Float = 0.68f,
    val accentColorHex: String = "#FA2D48",
    val capsuleSurfaceHex: String = "#000000",
    val borderGlowOpacity: Float = 0.28f,
    val anchorMode: NotchAnchorMode = NotchAnchorMode.CENTER,
    val resolutionPreset: ResolutionScalePreset = ResolutionScalePreset.AUTO_ADAPTIVE,
    val customScaleFactor: Float = 1.0f,
    val showAlignmentGrid: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.DARK_OBSIDIAN
) {
    fun effectiveScale(screenWidthDp: Int): Float {
        return when (resolutionPreset) {
            ResolutionScalePreset.AUTO_ADAPTIVE -> when {
                screenWidthDp < 370 -> 0.92f
                screenWidthDp >= 840 -> 1.15f
                screenWidthDp >= 600 -> 1.07f
                else -> 1.0f
            }
            ResolutionScalePreset.CUSTOM -> customScaleFactor.coerceIn(0.75f, 1.35f)
            else -> resolutionPreset.scaleFactor
        }
    }
}

sealed interface IslandModuleState {
    val moduleType: IslandModuleType
    val priority: Int
    val accentHex: String

    data class MediaActivity(
        val trackTitle: String = "Midnight Subharmonic",
        val artist: String = "Klangwerk Studio",
        val albumName: String = "Monolith IV",
        val isPlaying: Boolean = true,
        val elapsedSec: Int = 84,
        val durationSec: Int = 236,
        val waveformBins: List<Float> = listOf(0.35f, 0.72f, 0.94f, 0.58f, 0.82f, 0.45f, 0.68f, 0.90f, 0.40f, 0.76f, 0.62f, 0.30f),
        val outputDevice: String = "AirPods Pro • Lossless",
        override val accentHex: String = "#FA2D48",
        override val priority: Int = 90
    ) : IslandModuleState {
        override val moduleType: IslandModuleType = IslandModuleType.MEDIA
        val progressFraction: Float
            get() = if (durationSec > 0) (elapsedSec.toFloat() / durationSec).coerceIn(0f, 1f) else 0f
    }

    data class TimerActivity(
        val label: String = "Focus Sprint",
        val remainingMs: Long = 14 * 60 * 1000L + 42 * 1000L,
        val totalMs: Long = 25 * 60 * 1000L,
        val isRunning: Boolean = true,
        val isStopwatchMode: Boolean = false,
        val stopwatchElapsedMs: Long = 0L,
        val lapTimesMs: List<Long> = listOf(48_210L, 95_840L),
        override val accentHex: String = "#FF9F0A",
        override val priority: Int = 95
    ) : IslandModuleState {
        override val moduleType: IslandModuleType = IslandModuleType.TIMER
        val progressFraction: Float
            get() = if (isStopwatchMode) {
                ((stopwatchElapsedMs % 60_000L).toFloat() / 60_000f).coerceIn(0f, 1f)
            } else if (totalMs > 0L) {
                (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
            } else 0f
    }

    data class NotificationBatteryActivity(
        val appName: String = "Messages",
        val sender: String = "Elena Vance",
        val message: String = "Design specs for the Dynamic Island spring damper are ready for review.",
        val batteryPercent: Int = 84,
        val isCharging: Boolean = true,
        val chargingWatts: Int = 27,
        val isBatteryAlertMode: Boolean = false,
        val timestampLabel: String = "now",
        val quickReplyOptions: List<String> = listOf("On my way", "Looks great", "In 5m"),
        val lastRepliedText: String? = null,
        override val accentHex: String = "#34C759",
        override val priority: Int = 100
    ) : IslandModuleState {
        override val moduleType: IslandModuleType = IslandModuleType.NOTIFICATION
    }

    data class NavigationTrackerActivity(
        val subMode: NavigationTrackerSubMode = NavigationTrackerSubMode.TURN_BY_TURN,
        val primaryInstruction: String = "Turn right onto Howard St",
        val secondaryDetail: String = "350 m • Stay in right 2 lanes",
        val distanceOrScore: String = "350m",
        val etaText: String = "6 min • 10:14",
        val progressFraction: Float = 0.68f,
        override val accentHex: String = "#0A84FF",
        override val priority: Int = 88
    ) : IslandModuleState {
        override val moduleType: IslandModuleType = IslandModuleType.NAVIGATION
    }
}

enum class PluginTriggerType(val label: String) {
    BATTERY_THRESHOLD("Battery Level Trigger"),
    NOTIFICATION_KEYWORD("Notification Filter"),
    WEBHOOK_POLL("REST Webhook Poll"),
    BROADCAST_INTENT("Android Intent Broadcast")
}

data class PluginRuleModel(
    val id: Long = 0,
    val name: String,
    val packageIdentifier: String,
    val triggerType: PluginTriggerType,
    val triggerCondition: String,
    val targetModule: IslandModuleType,
    val headlineTemplate: String,
    val detailTemplate: String,
    val accentHex: String,
    val isEnabled: Boolean = true,
    val executionCount: Int = 0,
    val lastTriggeredEpochMs: Long? = null
)

data class SyncQueueRecord(
    val id: Long = 0,
    val operationType: String,
    val entitySummary: String,
    val checksumSha256: String,
    val resolutionProfile: String,
    val status: String,
    val createdAtEpochMs: Long,
    val syncedAtEpochMs: Long?
)

data class OnboardingPermissionState(
    val overlayPermissionGranted: Boolean = false,
    val batteryOptimizationIgnored: Boolean = false,
    val backgroundUsageAllowed: Boolean = false,
    val notificationListenerGranted: Boolean = false,
    val postNotificationsGranted: Boolean = false,
    val hasEnteredDashboard: Boolean = false
) {
    val grantedCount: Int
        get() = listOf(
            overlayPermissionGranted,
            batteryOptimizationIgnored,
            backgroundUsageAllowed,
            notificationListenerGranted,
            postNotificationsGranted
        ).count { it }

    val totalRequiredCount: Int = 5

    val allRequiredGranted: Boolean
        get() = overlayPermissionGranted &&
            batteryOptimizationIgnored &&
            backgroundUsageAllowed &&
            notificationListenerGranted &&
            postNotificationsGranted
}

fun parseHexColorSafely(hex: String, fallback: Color = Color(0xFFFA2D48)): Color {
    return try {
        val cleaned = hex.trim().removePrefix("#")
        when (cleaned.length) {
            6 -> Color(("FF$cleaned").toLong(16))
            8 -> Color(cleaned.toLong(16))
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

