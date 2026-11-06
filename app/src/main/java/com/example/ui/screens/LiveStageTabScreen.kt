package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.IslandModuleState
import com.example.model.IslandModuleType
import com.example.model.NavigationTrackerSubMode
import com.example.model.parseHexColorSafely
import com.example.ui.theme.AlabasterPrimary
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.ZincMuted
import com.example.ui.theme.ZincSecondary

@Composable
fun LiveStageTabScreen(
    primaryActivity: IslandModuleState,
    secondaryActivity: IslandModuleState?,
    mediaState: IslandModuleState.MediaActivity,
    timerState: IslandModuleState.TimerActivity,
    notificationState: IslandModuleState.NotificationBatteryActivity,
    navigationState: IslandModuleState.NavigationTrackerActivity,
    eventFeed: List<String>,
    onActivateModule: (IslandModuleType, Boolean) -> Unit,
    onToggleSplitMultitask: (Boolean) -> Unit,
    onSwapDroplet: () -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaSkip: (Boolean) -> Unit,
    onTimerToggle: () -> Unit,
    onTimerReset: () -> Unit,
    onToggleStopwatchMode: (Boolean) -> Unit,
    onToggleBatteryAlertMode: (Boolean) -> Unit,
    onSelectNavSubMode: (NavigationTrackerSubMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Split-Droplet Multitasking Bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HairlineBorder, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(StudioSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallSplit,
                            contentDescription = null,
                            tint = AlabasterPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Split-Droplet Multitasking",
                            style = MaterialTheme.typography.titleMedium,
                            color = AlabasterPrimary
                        )
                        Text(
                            text = if (secondaryActivity != null) {
                                "Primary: ${primaryActivity.moduleType.displayName} • Droplet: ${secondaryActivity.moduleType.displayName}"
                            } else {
                                "Detach a secondary circular capsule for dual activities"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZincSecondary
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (secondaryActivity != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioSurfaceElevated,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSwapDroplet() }
                                .testTag("swap_droplet_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap Primary and Droplet",
                                tint = AlabasterPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(18.dp)
                            )
                        }
                    }
                    Switch(
                        checked = secondaryActivity != null,
                        onCheckedChange = onToggleSplitMultitask,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = AlabasterPrimary
                        ),
                        modifier = Modifier.testTag("split_multitask_switch")
                    )
                }
            }
        }

        // 4 Live Activity Module Cards
        Text(
            text = "LIVE ACTIVITY MODULES",
            style = MaterialTheme.typography.labelLarge,
            color = ZincMuted,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // 1. Media Playback & Real-Time Waveform Card
        ModuleControlCard(
            moduleType = IslandModuleType.MEDIA,
            isPrimary = primaryActivity.moduleType == IslandModuleType.MEDIA,
            isSecondary = secondaryActivity?.moduleType == IslandModuleType.MEDIA,
            subtitle = "${mediaState.trackTitle} — ${mediaState.artist}",
            badgeText = if (mediaState.isPlaying) "WAVEFORM LIVE" else "PAUSED",
            icon = Icons.Default.GraphicEq,
            onSetPrimary = { onActivateModule(IslandModuleType.MEDIA, false) },
            onPinDroplet = { onActivateModule(IslandModuleType.MEDIA, true) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionPillButton(
                    label = if (mediaState.isPlaying) "Pause Audio" else "Resume Audio",
                    onClick = onMediaPlayPause,
                    modifier = Modifier.weight(1f),
                    testTag = "card_media_play_pause"
                )
                ActionPillButton(
                    label = "Next Studio Track",
                    onClick = { onMediaSkip(true) },
                    modifier = Modifier.weight(1f),
                    testTag = "card_media_next_track"
                )
            }
        }

        // 2. Active Timers, Stopwatch & Progress Card
        ModuleControlCard(
            moduleType = IslandModuleType.TIMER,
            isPrimary = primaryActivity.moduleType == IslandModuleType.TIMER,
            isSecondary = secondaryActivity?.moduleType == IslandModuleType.TIMER,
            subtitle = if (timerState.isStopwatchMode) "Chronograph Lap Mode" else "25:00 Focus Sprint Countdown",
            badgeText = if (timerState.isRunning) "RUNNING" else "HELD",
            icon = Icons.Default.Timer,
            onSetPrimary = { onActivateModule(IslandModuleType.TIMER, false) },
            onPinDroplet = { onActivateModule(IslandModuleType.TIMER, true) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionPillButton(
                    label = if (timerState.isRunning) "Pause Clock" else "Start Clock",
                    onClick = onTimerToggle,
                    modifier = Modifier.weight(1f),
                    testTag = "card_timer_toggle"
                )
                ActionPillButton(
                    label = if (timerState.isStopwatchMode) "Switch to Countdown" else "Switch to Stopwatch",
                    onClick = { onToggleStopwatchMode(!timerState.isStopwatchMode) },
                    modifier = Modifier.weight(1f),
                    testTag = "card_timer_mode_switch"
                )
                ActionPillButton(
                    label = "Reset",
                    onClick = onTimerReset,
                    testTag = "card_timer_reset"
                )
            }
        }

        // 3. Incoming Notifications & System Battery Alerts Card
        ModuleControlCard(
            moduleType = IslandModuleType.NOTIFICATION,
            isPrimary = primaryActivity.moduleType == IslandModuleType.NOTIFICATION,
            isSecondary = secondaryActivity?.moduleType == IslandModuleType.NOTIFICATION,
            subtitle = if (notificationState.isBatteryAlertMode) {
                "${notificationState.chargingWatts}W HyperCharge • ${notificationState.batteryPercent}% Battery"
            } else {
                "${notificationState.sender}: ${notificationState.message}"
            },
            badgeText = "${notificationState.batteryPercent}% PWR",
            icon = if (notificationState.isBatteryAlertMode) Icons.Default.BatteryChargingFull else Icons.Default.NotificationsActive,
            onSetPrimary = { onActivateModule(IslandModuleType.NOTIFICATION, false) },
            onPinDroplet = { onActivateModule(IslandModuleType.NOTIFICATION, true) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionPillButton(
                    label = "Message Alert View",
                    selected = !notificationState.isBatteryAlertMode,
                    onClick = { onToggleBatteryAlertMode(false) },
                    modifier = Modifier.weight(1f),
                    testTag = "card_notif_message_mode"
                )
                ActionPillButton(
                    label = "27W Battery Charge HUD",
                    selected = notificationState.isBatteryAlertMode,
                    onClick = { onToggleBatteryAlertMode(true) },
                    modifier = Modifier.weight(1f),
                    testTag = "card_notif_battery_mode"
                )
            }
        }

        // 4. Navigation Directions & Live Delivery / Sports Tracker Card
        ModuleControlCard(
            moduleType = IslandModuleType.NAVIGATION,
            isPrimary = primaryActivity.moduleType == IslandModuleType.NAVIGATION,
            isSecondary = secondaryActivity?.moduleType == IslandModuleType.NAVIGATION,
            subtitle = "${navigationState.primaryInstruction} (${navigationState.distanceOrScore})",
            badgeText = navigationState.subMode.label,
            icon = Icons.Default.DirectionsCar,
            onSetPrimary = { onActivateModule(IslandModuleType.NAVIGATION, false) },
            onPinDroplet = { onActivateModule(IslandModuleType.NAVIGATION, true) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NavigationTrackerSubMode.entries.forEach { subMode ->
                    ActionPillButton(
                        label = subMode.label,
                        selected = navigationState.subMode == subMode,
                        onClick = { onSelectNavSubMode(subMode) },
                        modifier = Modifier.weight(1f),
                        testTag = "card_nav_submode_${subMode.name.lowercase()}"
                    )
                }
            }
        }

        // Real-Time Activity Multiplexer Telemetry Feed
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HairlineBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ENGINE TELEMETRY LOG",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZincMuted
                )
                eventFeed.take(4).forEach { entry ->
                    Text(
                        text = "• $entry",
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = ZincSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleControlCard(
    moduleType: IslandModuleType,
    isPrimary: Boolean,
    isSecondary: Boolean,
    subtitle: String,
    badgeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSetPrimary: () -> Unit,
    onPinDroplet: () -> Unit,
    controlsContent: @Composable () -> Unit
) {
    val accent = parseHexColorSafely(moduleType.defaultAccentHex)
    val borderColor = if (isPrimary) accent.copy(alpha = 0.65f) else HairlineBorder

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = StudioSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .testTag("module_card_${moduleType.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = moduleType.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = AlabasterPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = accent,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZincSecondary,
                            maxLines = 1
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPrimary) accent else StudioSurfaceElevated,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSetPrimary() }
                            .testTag("activate_primary_${moduleType.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isPrimary) Color.Black else AlabasterPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isPrimary) "Active" else "Primary",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPrimary) Color.Black else AlabasterPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSecondary) AlabasterPrimary else StudioSurfaceElevated,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPinDroplet() }
                            .testTag("pin_droplet_${moduleType.id}")
                    ) {
                        Text(
                            text = if (isSecondary) "Droplet ✓" else "+ Droplet",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSecondary) Color.Black else ZincSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            controlsContent()
        }
    }
}

@Composable
private fun ActionPillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) AlabasterPrimary else StudioSurfaceElevated,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.Black else AlabasterPrimary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
