package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ThemePreference
import com.example.ui.components.InteractiveStageViewport
import com.example.ui.screens.CalibrationPhysicsTabScreen
import com.example.ui.screens.LiveStageTabScreen
import com.example.ui.screens.PluginSdkTabScreen
import com.example.ui.screens.VaultSystemTabScreen
import com.example.ui.screens.WelcomeOnboardingScreen
import com.example.ui.theme.AlabasterPrimary
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.IsleStudioTheme
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.ZincMuted
import com.example.ui.theme.ZincSecondary
import com.example.viewmodel.IslandEngineViewModel

enum class StudioDestination(
    val index: Int,
    val titleRes: Int,
    val icon: ImageVector,
    val testTag: String
) {
    LIVE_STAGE(0, R.string.tab_live_stage, Icons.Default.GraphicEq, "nav_tab_live_stage"),
    CALIBRATION(1, R.string.tab_calibration, Icons.Default.Tune, "nav_tab_calibration"),
    PLUGINS(2, R.string.tab_plugins, Icons.Default.Extension, "nav_tab_plugins"),
    VAULT_SYNC(3, R.string.tab_vault_sync, Icons.Default.Lock, "nav_tab_vault_sync")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: IslandEngineViewModel = viewModel(
                factory = IslandEngineViewModel.Factory(application)
            )
            IsleStudioApp(viewModel = vm)
        }
    }
}

@Composable
fun IsleStudioApp(viewModel: IslandEngineViewModel) {
    val configuration by viewModel.configuration.collectAsStateWithLifecycle()
    val onboardingState by viewModel.onboardingState.collectAsStateWithLifecycle()
    val displayMode by viewModel.displayMode.collectAsStateWithLifecycle()
    val primaryActivity by viewModel.primaryActivity.collectAsStateWithLifecycle()
    val secondaryActivity by viewModel.secondaryDropletActivity.collectAsStateWithLifecycle()
    val isOverlayRunning by viewModel.isOverlayServiceRunning.collectAsStateWithLifecycle()
    val eventFeed by viewModel.eventFeed.collectAsStateWithLifecycle()

    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val notificationState by viewModel.notificationState.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()

    val savedPresets by viewModel.savedPresets.collectAsStateWithLifecycle()
    val plugins by viewModel.plugins.collectAsStateWithLifecycle()
    val syncQueue by viewModel.syncQueue.collectAsStateWithLifecycle()
    val exportedJson by viewModel.exportedVaultJson.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    val isDark = when (configuration.themePreference) {
        ThemePreference.DARK_OBSIDIAN -> true
        ThemePreference.LIGHT_STUDIO -> false
        ThemePreference.SYSTEM_AUTO -> isSystemInDarkTheme()
    }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // Mandatory BackHandler for secondary tabs to return to Live Stage tab
    BackHandler(enabled = onboardingState.hasEnteredDashboard && selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    val context = LocalContext.current

    IsleStudioTheme(darkTheme = isDark) {
        if (!onboardingState.hasEnteredDashboard) {
            WelcomeOnboardingScreen(
                configuration = configuration,
                displayMode = displayMode,
                primaryActivity = primaryActivity,
                secondaryActivity = secondaryActivity,
                onboardingState = onboardingState,
                onTogglePreviewMode = viewModel::setDisplayMode,
                onSwapDroplet = viewModel::swapPrimaryAndSecondary,
                onGrantOverlayPermission = viewModel::grantOverlayPermission,
                onGrantBatteryOptimization = viewModel::grantBatteryOptimizationIgnored,
                onGrantBackgroundUsage = viewModel::grantBackgroundUsageAllowed,
                onGrantNotificationListener = viewModel::grantNotificationListenerPermission,
                onGrantPostNotifications = viewModel::grantPostNotificationsPermission,
                onGrantAllPermissions = viewModel::grantAllRequiredPermissions,
                onEnterDashboard = { viewModel.enterDashboardIfAuthorized(context) }
            )
            return@IsleStudioTheme
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 700.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            containerColor = StudioSurface,
                            tonalElevation = 0.dp,
                            modifier = Modifier.border(1.dp, HairlineBorder)
                        ) {
                            StudioDestination.entries.forEach { dest ->
                                val selected = selectedTabIndex == dest.index
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { selectedTabIndex = dest.index },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = stringResource(dest.titleRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(dest.titleRes),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        selectedTextColor = AlabasterPrimary,
                                        indicatorColor = AlabasterPrimary,
                                        unselectedIconColor = ZincMuted,
                                        unselectedTextColor = ZincMuted
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                if (isExpandedScreen) {
                    // Canonical Side-by-Side Layout for Tablets / Foldables
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavigationRail(
                            containerColor = StudioSurface,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            StudioDestination.entries.forEach { dest ->
                                val selected = selectedTabIndex == dest.index
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { selectedTabIndex = dest.index },
                                    icon = {
                                        Icon(
                                            imageVector = dest.icon,
                                            contentDescription = stringResource(dest.titleRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(dest.titleRes),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(0.48f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StudioTopHeader(
                                    presetName = configuration.presetName,
                                    isOverlayRunning = isOverlayRunning,
                                    onQuickToggleOverlay = {
                                        viewModel.toggleSystemOverlayService(context, !isOverlayRunning)
                                    }
                                )
                                InteractiveStageViewport(
                                    configuration = configuration,
                                    displayMode = displayMode,
                                    primaryActivity = primaryActivity,
                                    secondaryActivity = secondaryActivity,
                                    isOverlayRunning = isOverlayRunning,
                                    onSelectDisplayMode = viewModel::setDisplayMode,
                                    onToggleAlignmentGrid = {
                                        viewModel.updateStyling(
                                            configuration.accentColorHex,
                                            configuration.borderGlowOpacity,
                                            !configuration.showAlignmentGrid,
                                            configuration.themePreference
                                        )
                                    },
                                    onSwapDroplet = viewModel::swapPrimaryAndSecondary,
                                    onMediaPlayPause = viewModel::toggleMediaPlayPause,
                                    onMediaSkip = viewModel::skipMediaTrack,
                                    onTimerToggle = viewModel::toggleTimerRunning,
                                    onTimerReset = viewModel::resetTimerOrStopwatch,
                                    onQuickReply = viewModel::sendQuickReply
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(0.52f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                BannerNotificationStrip(
                                    message = bannerMessage,
                                    onDismiss = viewModel::clearBanner
                                )
                                ActiveTabContent(
                                    selectedTabIndex = selectedTabIndex,
                                    viewModel = viewModel,
                                    configuration = configuration,
                                    primaryActivity = primaryActivity,
                                    secondaryActivity = secondaryActivity,
                                    mediaState = mediaState,
                                    timerState = timerState,
                                    notificationState = notificationState,
                                    navigationState = navigationState,
                                    eventFeed = eventFeed,
                                    savedPresets = savedPresets,
                                    plugins = plugins,
                                    syncQueue = syncQueue,
                                    isOverlayRunning = isOverlayRunning,
                                    exportedJson = exportedJson
                                )
                            }
                        }
                    }
                } else {
                    // Mobile Handheld Vertical Studio Layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            StudioTopHeader(
                                presetName = configuration.presetName,
                                isOverlayRunning = isOverlayRunning,
                                onQuickToggleOverlay = {
                                    viewModel.toggleSystemOverlayService(context, !isOverlayRunning)
                                }
                            )

                            BannerNotificationStrip(
                                message = bannerMessage,
                                onDismiss = viewModel::clearBanner
                            )

                            InteractiveStageViewport(
                                configuration = configuration,
                                displayMode = displayMode,
                                primaryActivity = primaryActivity,
                                secondaryActivity = secondaryActivity,
                                isOverlayRunning = isOverlayRunning,
                                onSelectDisplayMode = viewModel::setDisplayMode,
                                onToggleAlignmentGrid = {
                                    viewModel.updateStyling(
                                        configuration.accentColorHex,
                                        configuration.borderGlowOpacity,
                                        !configuration.showAlignmentGrid,
                                        configuration.themePreference
                                    )
                                },
                                onSwapDroplet = viewModel::swapPrimaryAndSecondary,
                                onMediaPlayPause = viewModel::toggleMediaPlayPause,
                                onMediaSkip = viewModel::skipMediaTrack,
                                onTimerToggle = viewModel::toggleTimerRunning,
                                onTimerReset = viewModel::resetTimerOrStopwatch,
                                onQuickReply = viewModel::sendQuickReply
                            )

                            ActiveTabContent(
                                selectedTabIndex = selectedTabIndex,
                                viewModel = viewModel,
                                configuration = configuration,
                                primaryActivity = primaryActivity,
                                secondaryActivity = secondaryActivity,
                                mediaState = mediaState,
                                timerState = timerState,
                                notificationState = notificationState,
                                navigationState = navigationState,
                                eventFeed = eventFeed,
                                savedPresets = savedPresets,
                                plugins = plugins,
                                syncQueue = syncQueue,
                                isOverlayRunning = isOverlayRunning,
                                exportedJson = exportedJson
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioTopHeader(
    presetName: String,
    isOverlayRunning: Boolean,
    onQuickToggleOverlay: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "IsleStudio",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Profile: $presetName",
                style = MaterialTheme.typography.labelMedium,
                color = ZincSecondary
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isOverlayRunning) Color(0xFF34C759).copy(alpha = 0.18f) else StudioSurfaceElevated,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = if (isOverlayRunning) Color(0xFF34C759) else HairlineBorder,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onQuickToggleOverlay() }
                .testTag("header_overlay_toggle")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isOverlayRunning) Color(0xFF34C759) else ZincMuted)
                )
                Text(
                    text = if (isOverlayRunning) "Overlay Active" else "Launch Overlay",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isOverlayRunning) Color(0xFF34C759) else AlabasterPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun BannerNotificationStrip(
    message: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(visible = !message.isNullOrBlank()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = StudioSurfaceElevated,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF34C759).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable { onDismiss() }
                .testTag("status_banner_strip")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AlabasterPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "DISMISS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZincMuted
                )
            }
        }
    }
}

@Composable
private fun ActiveTabContent(
    selectedTabIndex: Int,
    viewModel: IslandEngineViewModel,
    configuration: com.example.model.IslandConfiguration,
    primaryActivity: com.example.model.IslandModuleState,
    secondaryActivity: com.example.model.IslandModuleState?,
    mediaState: com.example.model.IslandModuleState.MediaActivity,
    timerState: com.example.model.IslandModuleState.TimerActivity,
    notificationState: com.example.model.IslandModuleState.NotificationBatteryActivity,
    navigationState: com.example.model.IslandModuleState.NavigationTrackerActivity,
    eventFeed: List<String>,
    savedPresets: List<com.example.data.SavedIslandPreset>,
    plugins: List<com.example.model.PluginRuleModel>,
    syncQueue: List<com.example.model.SyncQueueRecord>,
    isOverlayRunning: Boolean,
    exportedJson: String?
) {
    val context = LocalContext.current
    when (selectedTabIndex) {
        0 -> LiveStageTabScreen(
            primaryActivity = primaryActivity,
            secondaryActivity = secondaryActivity,
            mediaState = mediaState,
            timerState = timerState,
            notificationState = notificationState,
            navigationState = navigationState,
            eventFeed = eventFeed,
            onActivateModule = viewModel::activateModule,
            onToggleSplitMultitask = viewModel::toggleSplitMultitask,
            onSwapDroplet = viewModel::swapPrimaryAndSecondary,
            onMediaPlayPause = viewModel::toggleMediaPlayPause,
            onMediaSkip = viewModel::skipMediaTrack,
            onTimerToggle = viewModel::toggleTimerRunning,
            onTimerReset = viewModel::resetTimerOrStopwatch,
            onToggleStopwatchMode = viewModel::toggleStopwatchMode,
            onToggleBatteryAlertMode = viewModel::toggleBatteryAlertMode,
            onSelectNavSubMode = viewModel::selectNavigationSubMode
        )
        1 -> CalibrationPhysicsTabScreen(
            configuration = configuration,
            onUpdateOffsets = viewModel::updateOffsets,
            onUpdateAnchorMode = viewModel::updateAnchorMode,
            onUpdateDimensions = viewModel::updateDimensions,
            onUpdateSpringPhysics = viewModel::updateSpringPhysics,
            onUpdateStyling = viewModel::updateStyling
        )
        2 -> PluginSdkTabScreen(
            plugins = plugins,
            onCreatePlugin = viewModel::createCustomPlugin,
            onTriggerPlugin = viewModel::triggerPluginNow,
            onTogglePlugin = viewModel::togglePluginEnabled,
            onDeletePlugin = viewModel::deletePlugin,
            onDispatchRawJson = viewModel::testRawJsonBroadcast
        )
        3 -> VaultSystemTabScreen(
            configuration = configuration,
            savedPresets = savedPresets,
            syncQueue = syncQueue,
            isOverlayRunning = isOverlayRunning,
            exportedJson = exportedJson,
            canDrawOverlays = viewModel.canDrawSystemOverlays(context),
            isNotifListenerGranted = viewModel.isNotificationListenerEnabled(context),
            onToggleOverlayService = { viewModel.toggleSystemOverlayService(context, it) },
            onUpdateResolutionPreset = viewModel::updateResolutionPreset,
            onSavePreset = viewModel::saveCurrentAsPreset,
            onLoadPreset = viewModel::applySavedPreset,
            onDeletePreset = viewModel::deleteSavedPreset,
            onSyncOfflineQueue = viewModel::syncOfflineRecordsNow,
            onExportVault = viewModel::exportVaultJson,
            onImportVault = viewModel::importVaultJson,
            onDismissExportModal = viewModel::clearExportedJsonModal
        )
    }
}
