package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.IslandConfiguration
import com.example.model.IslandDisplayMode
import com.example.model.IslandModuleState
import com.example.model.OnboardingPermissionState
import com.example.ui.components.DynamicIslandCapsule
import com.example.ui.theme.AlabasterPrimary
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.ZincMuted
import com.example.ui.theme.ZincSecondary

@Composable
fun WelcomeOnboardingScreen(
    configuration: IslandConfiguration,
    displayMode: IslandDisplayMode,
    primaryActivity: IslandModuleState,
    secondaryActivity: IslandModuleState?,
    onboardingState: OnboardingPermissionState,
    onTogglePreviewMode: (IslandDisplayMode) -> Unit,
    onSwapDroplet: () -> Unit,
    onGrantOverlayPermission: () -> Unit,
    onGrantBatteryOptimization: () -> Unit,
    onGrantBackgroundUsage: () -> Unit,
    onGrantNotificationListener: () -> Unit,
    onGrantPostNotifications: () -> Unit,
    onGrantAllPermissions: () -> Unit,
    onEnterDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPermissionsPopup by rememberSaveable { mutableStateOf(false) }
    var showMandatoryBlockedError by remember { mutableStateOf(false) }

    val postNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onGrantPostNotifications()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = ObsidianBackground,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Live Dynamic Island Hero Stage
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = StudioSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF34C759).copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "INTERACTIVE PREVIEW",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34C759),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = "Tap Island to Morph",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 10.sp,
                                    color = ZincMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF070709))
                                    .border(1.dp, Color(0xFF1E1E24), RoundedCornerShape(18.dp))
                                    .padding(top = 12.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                DynamicIslandCapsule(
                                    configuration = configuration.copy(offsetXDp = 0f, offsetYDp = 4f),
                                    displayMode = displayMode,
                                    primaryActivity = primaryActivity,
                                    secondaryActivity = secondaryActivity,
                                    onTapCapsule = {
                                        val next = if (displayMode == IslandDisplayMode.EXPANDED_CARD) {
                                            IslandDisplayMode.SPLIT_MULTITASK
                                        } else {
                                            IslandDisplayMode.EXPANDED_CARD
                                        }
                                        onTogglePreviewMode(next)
                                    },
                                    onLongPressCapsule = {
                                        onTogglePreviewMode(IslandDisplayMode.EXPANDED_CARD)
                                    },
                                    onSwipeDismiss = {
                                        onTogglePreviewMode(IslandDisplayMode.COMPACT_PILL)
                                    },
                                    onSwapDroplet = onSwapDroplet,
                                    onMediaPlayPause = {},
                                    onMediaSkip = {},
                                    onTimerToggle = {},
                                    onTimerReset = {},
                                    onQuickReply = {}
                                )
                            }
                        }
                    }

                    // Minimalist Brand Identity Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ISLESTUDIO ARCHITECTURE",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFA2D48)
                        )
                        Text(
                            text = "Fluid Dynamic Island & Live Activity Engine",
                            style = MaterialTheme.typography.displayLarge,
                            color = AlabasterPrimary
                        )
                        Text(
                            text = "Designed with Apple iOS and macOS harmonic spring physics, sub-pixel X/Y cutout alignment, encrypted local storage, and extensible developer plugins.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ZincSecondary
                        )
                    }

                    // Architectural Capability Highlights Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WelcomeFeatureTile(
                            icon = Icons.Default.Speed,
                            accentColor = Color(0xFFFA2D48),
                            title = "Damped-Harmonic Fluid Physics",
                            subtitle = "Customizable spring stiffness (200–1400 N/m) and damping ratio with split-droplet multitasking."
                        )
                        WelcomeFeatureTile(
                            icon = Icons.Default.CenterFocusStrong,
                            accentColor = Color(0xFF0A84FF),
                            title = "Precision X/Y Cutout Calibration",
                            subtitle = "Align to Center, Left, or Right camera punch-holes with ±1dp steppers and adaptive screen scaling."
                        )
                        WelcomeFeatureTile(
                            icon = Icons.Default.GraphicEq,
                            accentColor = Color(0xFFFF9F0A),
                            title = "Real-Time Live Activity Multiplexer",
                            subtitle = "Music waveform visualizer, countdowns & stopwatch, notifications, battery HUD, and live ETA."
                        )
                        WelcomeFeatureTile(
                            icon = Icons.Default.Lock,
                            accentColor = Color(0xFF34C759),
                            title = "AES-256-GCM Vault & Plugin SDK",
                            subtitle = "Hardware-encrypted preset storage, offline synchronization ledger, and JSON broadcast intents."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Continue CTA Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { showPermissionsPopup = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlabasterPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("welcome_continue_button")
                    ) {
                        Text(
                            text = "Continue to Permissions & Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Mandatory overlay & background permissions required on next step",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZincMuted
                    )
                }
            }
        }

        // Mandatory Permissions & Background Usage Gate Pop-up Overlay
        if (showPermissionsPopup) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.78f))
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = StudioSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
                        .testTag("permissions_gate_dialog")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header + Mandatory Counter Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (onboardingState.allRequiredGranted) Color(0xFF34C759) else Color(0xFFFF9F0A),
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "Mandatory System Permissions",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = AlabasterPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Required to hover Dynamic Island & run in background",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ZincSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (onboardingState.allRequiredGranted) {
                                    Color(0xFF34C759).copy(alpha = 0.18f)
                                } else {
                                    Color(0xFFFF9F0A).copy(alpha = 0.18f)
                                }
                            ) {
                                Text(
                                    text = "${onboardingState.grantedCount}/${onboardingState.totalRequiredCount}",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (onboardingState.allRequiredGranted) Color(0xFF34C759) else Color(0xFFFF9F0A),
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                        .testTag("permission_progress_badge")
                                )
                            }
                        }

                        // 1. Display Over Other Apps (SYSTEM_ALERT_WINDOW)
                        MandatoryPermissionItemRow(
                            icon = Icons.Default.Layers,
                            title = "Display Over Other Apps",
                            subtitle = "Allows Dynamic Island capsule to hover above all applications",
                            isGranted = onboardingState.overlayPermissionGranted,
                            buttonTag = "grant_overlay_button",
                            onGrantClick = {
                                runCatching {
                                    if (!Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    }
                                }
                                onGrantOverlayPermission()
                            }
                        )

                        // 2. Ignore Battery Optimizations (REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        MandatoryPermissionItemRow(
                            icon = Icons.Default.BatteryChargingFull,
                            title = "Ignore Battery Optimization",
                            subtitle = "Prevents Android Doze from suspending 60/120Hz spring physics",
                            isGranted = onboardingState.batteryOptimizationIgnored,
                            buttonTag = "grant_battery_opt_button",
                            onGrantClick = {
                                runCatching {
                                    val intent = Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                }
                                onGrantBatteryOptimization()
                            }
                        )

                        // 3. Unrestricted Background Usage & Data
                        MandatoryPermissionItemRow(
                            icon = Icons.Default.Extension,
                            title = "Unrestricted Background Usage",
                            subtitle = "Keeps live timers, media waveforms & webhook plugins active",
                            isGranted = onboardingState.backgroundUsageAllowed,
                            buttonTag = "grant_background_usage_button",
                            onGrantClick = {
                                onGrantBackgroundUsage()
                            }
                        )

                        // 4. Live Notification Bridge (BIND_NOTIFICATION_LISTENER_SERVICE)
                        MandatoryPermissionItemRow(
                            icon = Icons.Default.NotificationsActive,
                            title = "Notification Listener Access",
                            subtitle = "Routes incoming messages, calls & media sessions into the Island",
                            isGranted = onboardingState.notificationListenerGranted,
                            buttonTag = "grant_notif_listener_button",
                            onGrantClick = {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                }
                                onGrantNotificationListener()
                            }
                        )

                        // 5. Runtime Activity Notifications (POST_NOTIFICATIONS)
                        MandatoryPermissionItemRow(
                            icon = Icons.Default.CheckCircle,
                            title = "Real-Time Activity Notifications",
                            subtitle = "Required for foreground overlay service & instant alert pills",
                            isGranted = onboardingState.postNotificationsGranted,
                            buttonTag = "grant_post_notif_button",
                            onGrantClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    runCatching {
                                        postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }.onFailure {
                                        onGrantPostNotifications()
                                    }
                                }
                                onGrantPostNotifications()
                            }
                        )

                        // Quick "Grant All Required Permissions" helper button
                        if (!onboardingState.allRequiredGranted) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StudioSurfaceElevated,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF0A84FF).copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        onGrantAllPermissions()
                                        showMandatoryBlockedError = false
                                    }
                                    .testTag("grant_all_permissions_button")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 11.dp, horizontal = 14.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFF0A84FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Authorize All 5 Required Permissions & Exemptions",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = AlabasterPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Warning Banner if user attempts to enter Dashboard while permissions are missing
                        AnimatedVisibility(visible = showMandatoryBlockedError && !onboardingState.allRequiredGranted) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFA2D48).copy(alpha = 0.14f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFFA2D48).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .testTag("mandatory_permission_warning")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFFA2D48),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "All 5 permissions & background exemptions are mandatory before entering the Dashboard.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AlabasterPrimary
                                    )
                                }
                            }
                        }

                        // Dialog Footer Actions: Back | Enter Dashboard (Mandatory Gate)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showPermissionsPopup = false },
                                modifier = Modifier.weight(0.38f)
                            ) {
                                Text(
                                    text = "Back",
                                    color = ZincSecondary,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Button(
                                onClick = {
                                    if (onboardingState.allRequiredGranted) {
                                        showPermissionsPopup = false
                                        onEnterDashboard()
                                    } else {
                                        showMandatoryBlockedError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (onboardingState.allRequiredGranted) {
                                        Color(0xFF34C759)
                                    } else {
                                        StudioSurfaceElevated
                                    },
                                    contentColor = if (onboardingState.allRequiredGranted) {
                                        Color.Black
                                    } else {
                                        ZincMuted
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.62f)
                                    .height(48.dp)
                                    .testTag("enter_dashboard_button")
                            ) {
                                Text(
                                    text = if (onboardingState.allRequiredGranted) {
                                        "Enter Dashboard"
                                    } else {
                                        "Locked (${onboardingState.grantedCount}/5 Granted)"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
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
private fun WelcomeFeatureTile(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    subtitle: String
) {
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AlabasterPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZincSecondary
                )
            }
        }
    }
}

@Composable
private fun MandatoryPermissionItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    buttonTag: String,
    onGrantClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = StudioSurfaceElevated,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isGranted) Color(0xFF34C759).copy(alpha = 0.45f) else HairlineBorder,
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isGranted) Color(0xFF34C759).copy(alpha = 0.18f)
                            else Color(0xFF27272A)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF34C759) else AlabasterPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.padding(end = 6.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AlabasterPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZincSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isGranted) Color(0xFF34C759).copy(alpha = 0.18f) else AlabasterPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onGrantClick() }
                    .testTag(buttonTag)
            ) {
                Text(
                    text = if (isGranted) "Granted ✓" else "Allow",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isGranted) Color(0xFF34C759) else Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}
