package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.IslandConfiguration
import com.example.model.IslandDisplayMode
import com.example.model.IslandModuleState
import com.example.model.IslandModuleType
import com.example.model.NavigationTrackerSubMode
import com.example.model.parseHexColorSafely
import com.example.ui.theme.AlabasterPrimary
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.ZincMuted
import com.example.ui.theme.ZincSecondary
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicIslandCapsule(
    configuration: IslandConfiguration,
    displayMode: IslandDisplayMode,
    primaryActivity: IslandModuleState,
    secondaryActivity: IslandModuleState?,
    onTapCapsule: () -> Unit,
    onLongPressCapsule: () -> Unit,
    onSwipeDismiss: () -> Unit,
    onSwapDroplet: () -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaSkip: (Boolean) -> Unit,
    onTimerToggle: () -> Unit,
    onTimerReset: () -> Unit,
    onQuickReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val scaleFactor = configuration.effectiveScale(screenWidthDp)
    val haptics = LocalHapticFeedback.current

    val damping = configuration.springDamping.coerceIn(0.35f, 1.0f)
    val stiffness = configuration.springStiffness.coerceIn(180f, 1500f)

    val targetWidthDp: Dp = when (displayMode) {
        IslandDisplayMode.IDLE_CUTOUT -> (112f * scaleFactor).dp
        IslandDisplayMode.COMPACT_PILL -> (configuration.compactWidthDp * scaleFactor).dp
        IslandDisplayMode.SPLIT_MULTITASK -> ((configuration.compactWidthDp - 14f) * scaleFactor).dp
        IslandDisplayMode.EXPANDED_CARD -> (configuration.expandedWidthDp * scaleFactor)
            .coerceAtMost((screenWidthDp - 24).toFloat()).dp
    }

    val targetHeightDp: Dp = when (displayMode) {
        IslandDisplayMode.IDLE_CUTOUT -> (30f * scaleFactor).dp
        IslandDisplayMode.COMPACT_PILL, IslandDisplayMode.SPLIT_MULTITASK ->
            (configuration.compactHeightDp * scaleFactor).dp
        IslandDisplayMode.EXPANDED_CARD -> (configuration.expandedHeightDp * scaleFactor).dp
    }

    val targetCornerDp: Dp = when (displayMode) {
        IslandDisplayMode.EXPANDED_CARD -> configuration.cornerRadiusDp.coerceIn(22f, 48f).dp
        else -> 50.dp
    }

    val animatedWidth by animateDpAsState(
        targetValue = targetWidthDp,
        animationSpec = spring(dampingRatio = damping, stiffness = stiffness),
        label = "island_width"
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetHeightDp,
        animationSpec = spring(dampingRatio = damping, stiffness = stiffness),
        label = "island_height"
    )
    val animatedCorner by animateDpAsState(
        targetValue = targetCornerDp,
        animationSpec = spring(dampingRatio = damping, stiffness = stiffness),
        label = "island_corner"
    )

    val animatedOffsetX by animateDpAsState(
        targetValue = (configuration.anchorMode.defaultXOffsetDp + configuration.offsetXDp).dp,
        animationSpec = spring(dampingRatio = damping, stiffness = stiffness),
        label = "island_offset_x"
    )
    val animatedOffsetY by animateDpAsState(
        targetValue = configuration.offsetYDp.dp,
        animationSpec = spring(dampingRatio = damping, stiffness = stiffness),
        label = "island_offset_y"
    )

    val dropletSize by animateDpAsState(
        targetValue = if (displayMode == IslandDisplayMode.SPLIT_MULTITASK && secondaryActivity != null) {
            (configuration.compactHeightDp * scaleFactor).dp
        } else {
            0.dp
        },
        animationSpec = spring(dampingRatio = damping * 0.9f, stiffness = stiffness),
        label = "droplet_size"
    )

    val dropletGap by animateDpAsState(
        targetValue = if (displayMode == IslandDisplayMode.SPLIT_MULTITASK && secondaryActivity != null) {
            configuration.splitDropletGapDp.dp
        } else {
            0.dp
        },
        animationSpec = spring(dampingRatio = damping, stiffness = stiffness),
        label = "droplet_gap"
    )

    var isPressedBounce by remember { mutableStateOf(false) }
    val bounceScale by animateFloatAsState(
        targetValue = if (isPressedBounce) 1.04f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "island_bounce_scale"
    )

    val activeAccentColor = parseHexColorSafely(
        primaryActivity.accentHex.ifBlank { configuration.accentColorHex }
    )
    val capsuleColor = parseHexColorSafely(configuration.capsuleSurfaceHex, Color.Black)
    val shape = RoundedCornerShape(animatedCorner)

    var dragAccumulated by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = modifier
            .offset(x = animatedOffsetX, y = animatedOffsetY)
            .scale(bounceScale),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Primary Dynamic Island Capsule
        Box(
            modifier = Modifier
                .testTag("dynamic_island_capsule")
                .width(animatedWidth)
                .height(animatedHeight)
                .shadow(
                    elevation = 20.dp,
                    shape = shape,
                    ambientColor = activeAccentColor.copy(alpha = configuration.borderGlowOpacity),
                    spotColor = activeAccentColor.copy(alpha = configuration.borderGlowOpacity)
                )
                .clip(shape)
                .background(capsuleColor)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            activeAccentColor.copy(alpha = configuration.borderGlowOpacity.coerceIn(0.08f, 0.85f)),
                            Color(0xFF27272A).copy(alpha = 0.65f),
                            activeAccentColor.copy(alpha = (configuration.borderGlowOpacity * 0.5f).coerceIn(0.05f, 0.5f))
                        )
                    ),
                    shape = shape
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragAccumulated = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            dragAccumulated += dragAmount
                        },
                        onDragEnd = {
                            if ( kotlin.math.abs(dragAccumulated) > 72f) {
                                if (configuration.hapticFeedbackEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onSwipeDismiss()
                            }
                            dragAccumulated = 0f
                        }
                    )
                }
                .combinedClickable(
                    onClick = {
                        if (configuration.hapticFeedbackEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onTapCapsule()
                    },
                    onLongClick = {
                        isPressedBounce = !isPressedBounce
                        if (configuration.hapticFeedbackEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onLongPressCapsule()
                    }
                )
                .padding(horizontal = if (displayMode == IslandDisplayMode.EXPANDED_CARD) 16.dp else 12.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = Pair(displayMode, primaryActivity),
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
                },
                label = "island_inner_content"
            ) { (mode, activity) ->
                when (mode) {
                    IslandDisplayMode.IDLE_CUTOUT -> IdleCutoutContent(activeAccentColor)
                    IslandDisplayMode.COMPACT_PILL, IslandDisplayMode.SPLIT_MULTITASK ->
                        CompactCapsuleContent(activity, activeAccentColor)
                    IslandDisplayMode.EXPANDED_CARD -> ExpandedCardContent(
                        activity = activity,
                        accentColor = activeAccentColor,
                        onMediaPlayPause = onMediaPlayPause,
                        onMediaSkip = onMediaSkip,
                        onTimerToggle = onTimerToggle,
                        onTimerReset = onTimerReset,
                        onQuickReply = onQuickReply
                    )
                }
            }
        }

        // Detached Secondary Split-Droplet Pill for Multitasking
        if (dropletSize > 4.dp && secondaryActivity != null) {
            Spacer(modifier = Modifier.width(dropletGap))
            val secondaryAccent = parseHexColorSafely(secondaryActivity.accentHex)
            Box(
                modifier = Modifier
                    .testTag("secondary_droplet_pill")
                    .size(dropletSize)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(capsuleColor)
                    .border(
                        width = 1.dp,
                        color = secondaryAccent.copy(alpha = 0.55f),
                        shape = CircleShape
                    )
                    .clickable {
                        if (configuration.hapticFeedbackEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onSwapDroplet()
                    },
                contentAlignment = Alignment.Center
            ) {
                SecondaryDropletIcon(secondaryActivity, secondaryAccent)
            }
        }
    }
}

@Composable
private fun IdleCutoutContent(accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Subtle camera lens silhouette
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFF141419))
                .border(0.8.dp, Color(0xFF27272E), CircleShape)
        )
        // Minimal status indicator dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.85f))
        )
    }
}

@Composable
private fun SecondaryDropletIcon(activity: IslandModuleState, accentColor: Color) {
    val icon = when (activity.moduleType) {
        IslandModuleType.MEDIA -> Icons.Default.PlayArrow
        IslandModuleType.TIMER -> Icons.Default.Timer
        IslandModuleType.NOTIFICATION -> Icons.Default.Bolt
        IslandModuleType.NAVIGATION -> Icons.Default.TurnRight
    }
    Icon(
        imageVector = icon,
        contentDescription = "Switch to ${activity.moduleType.displayName}",
        tint = accentColor,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun CompactCapsuleContent(activity: IslandModuleState, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        when (activity) {
            is IslandModuleState.MediaActivity -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_album_cover),
                        contentDescription = "Album art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = activity.trackTitle,
                        color = AlabasterPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(68.dp)
                    )
                }
                MiniWaveformVisualizer(
                    bins = activity.waveformBins.take(6),
                    color = accentColor,
                    isPlaying = activity.isPlaying,
                    modifier = Modifier
                        .width(34.dp)
                        .height(14.dp)
                )
            }

            is IslandModuleState.TimerActivity -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (activity.isStopwatchMode) "LAP" else "TIMER",
                        color = accentColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = formatMillisecondsClock(
                        if (activity.isStopwatchMode) activity.stopwatchElapsedMs else activity.remainingMs
                    ),
                    color = AlabasterPrimary,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            is IslandModuleState.NotificationBatteryActivity -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (activity.isBatteryAlertMode) {
                            Icons.Default.BatteryChargingFull
                        } else {
                            Icons.Default.NotificationsActive
                        },
                        contentDescription = "Notification or Battery",
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (activity.isBatteryAlertMode) "Charging" else activity.sender,
                        color = AlabasterPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(74.dp)
                    )
                }
                Text(
                    text = if (activity.isBatteryAlertMode) "${activity.batteryPercent}%" else activity.timestampLabel,
                    color = accentColor,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }

            is IslandModuleState.NavigationTrackerActivity -> {
                val navIcon = when (activity.subMode) {
                    NavigationTrackerSubMode.TURN_BY_TURN -> Icons.Default.TurnRight
                    NavigationTrackerSubMode.LIVE_DELIVERY -> Icons.Default.LocalShipping
                    NavigationTrackerSubMode.LIVE_SPORTS -> Icons.Default.SportsSoccer
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = navIcon,
                        contentDescription = activity.subMode.label,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = activity.primaryInstruction,
                        color = AlabasterPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(78.dp)
                    )
                }
                Text(
                    text = activity.distanceOrScore,
                    color = accentColor,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ExpandedCardContent(
    activity: IslandModuleState,
    accentColor: Color,
    onMediaPlayPause: () -> Unit,
    onMediaSkip: (Boolean) -> Unit,
    onTimerToggle: () -> Unit,
    onTimerReset: () -> Unit,
    onQuickReply: (String) -> Unit
) {
    when (activity) {
        is IslandModuleState.MediaActivity -> ExpandedMediaContent(
            activity = activity,
            accentColor = accentColor,
            onPlayPause = onMediaPlayPause,
            onSkip = onMediaSkip
        )
        is IslandModuleState.TimerActivity -> ExpandedTimerContent(
            activity = activity,
            accentColor = accentColor,
            onToggle = onTimerToggle,
            onReset = onTimerReset
        )
        is IslandModuleState.NotificationBatteryActivity -> ExpandedNotificationBatteryContent(
            activity = activity,
            accentColor = accentColor,
            onQuickReply = onQuickReply
        )
        is IslandModuleState.NavigationTrackerActivity -> ExpandedNavigationContent(
            activity = activity,
            accentColor = accentColor
        )
    }
}

@Composable
private fun ExpandedMediaContent(
    activity: IslandModuleState.MediaActivity,
    accentColor: Color,
    onPlayPause: () -> Unit,
    onSkip: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_album_cover),
                    contentDescription = "Album Artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column {
                    Text(
                        text = activity.trackTitle,
                        color = AlabasterPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${activity.artist} • ${activity.albumName}",
                        color = ZincSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activity.outputDevice,
                        color = accentColor,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            MiniWaveformVisualizer(
                bins = activity.waveformBins,
                color = accentColor,
                isPlaying = activity.isPlaying,
                modifier = Modifier
                    .width(56.dp)
                    .height(26.dp)
            )
        }

        // Scrub bar + tabular timestamps
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSecondsClock(activity.elapsedSec),
                    color = ZincSecondary,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF27272A))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(activity.progressFraction)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
                Text(
                    text = "-${formatSecondsClock((activity.durationSec - activity.elapsedSec).coerceAtLeast(0))}",
                    color = ZincSecondary,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onSkip(false) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("island_media_prev")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous Track",
                        tint = AlabasterPrimary
                    )
                }
                Spacer(modifier = Modifier.width(18.dp))
                Surface(
                    shape = CircleShape,
                    color = AlabasterPrimary,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onPlayPause() }
                        .testTag("island_media_play_pause")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (activity.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (activity.isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(18.dp))
                IconButton(
                    onClick = { onSkip(true) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("island_media_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next Track",
                        tint = AlabasterPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedTimerContent(
    activity: IslandModuleState.TimerActivity,
    accentColor: Color,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = activity.label.uppercase(Locale.US),
                    color = accentColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Text(
                text = formatMillisecondsClock(
                    if (activity.isStopwatchMode) activity.stopwatchElapsedMs else activity.remainingMs
                ),
                color = AlabasterPrimary,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.22f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onToggle() }
                        .testTag("island_timer_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (activity.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle timer",
                            tint = accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (activity.isRunning) "Pause" else "Resume",
                            color = accentColor,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF232329),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onReset() }
                        .testTag("island_timer_reset")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset timer",
                            tint = ZincSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Reset",
                            color = ZincSecondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // Precision Circular Progress Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(96.dp)
        ) {
            Canvas(modifier = Modifier.size(84.dp)) {
                val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                drawArc(
                    color = Color(0xFF27272A),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * activity.progressFraction,
                    useCenter = false,
                    style = stroke
                )
            }
            Text(
                text = "${(activity.progressFraction * 100).toInt()}%",
                color = AlabasterPrimary,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ExpandedNotificationBatteryContent(
    activity: IslandModuleState.NotificationBatteryActivity,
    accentColor: Color,
    onQuickReply: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (activity.isBatteryAlertMode) {
                            Icons.Default.BatteryChargingFull
                        } else {
                            Icons.Default.NotificationsActive
                        },
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = if (activity.isBatteryAlertMode) {
                            "${activity.chargingWatts}W HyperCharge Active"
                        } else {
                            activity.sender
                        },
                        color = AlabasterPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${activity.appName} • ${activity.timestampLabel}",
                        color = ZincMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Battery telemetry badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.16f)
            ) {
                Text(
                    text = "${activity.batteryPercent}%",
                    color = accentColor,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Text(
            text = activity.lastRepliedText?.let { "Sent reply: \"$it\"" } ?: activity.message,
            color = ZincSecondary,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (activity.isBatteryAlertMode) {
            // Segmented battery cells bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF232329))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((activity.batteryPercent / 100f).coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activity.quickReplyOptions.forEach { reply ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF202026),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onQuickReply(reply) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = reply,
                                color = AlabasterPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedNavigationContent(
    activity: IslandModuleState.NavigationTrackerActivity,
    accentColor: Color
) {
    val icon = when (activity.subMode) {
        NavigationTrackerSubMode.TURN_BY_TURN -> Icons.Default.DirectionsCar
        NavigationTrackerSubMode.LIVE_DELIVERY -> Icons.Default.LocalShipping
        NavigationTrackerSubMode.LIVE_SPORTS -> Icons.Default.SportsSoccer
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
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
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = activity.primaryInstruction,
                        color = AlabasterPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activity.secondaryDetail,
                        color = ZincSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = activity.distanceOrScore,
                    color = accentColor,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = activity.etaText,
                    color = ZincMuted,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = activity.subMode.label.uppercase(Locale.US),
                    color = ZincMuted,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "${(activity.progressFraction * 100).toInt()}% ROUTE PROGRESS",
                    color = accentColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF232329))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(activity.progressFraction)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
        }
    }
}

@Composable
fun MiniWaveformVisualizer(
    bins: List<Float>,
    color: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (bins.isEmpty()) return@Canvas
        val count = bins.size
        val spacing = 2.dp.toPx()
        val totalSpacing = spacing * (count - 1).coerceAtLeast(0)
        val barWidth = ((size.width - totalSpacing) / count).coerceAtLeast(2f)

        bins.forEachIndexed { index, amplitude ->
            val effectiveAmp = if (isPlaying) amplitude.coerceIn(0.18f, 1f) else 0.22f
            val barHeight = size.height * effectiveAmp
            val top = (size.height - barHeight) / 2f
            val left = index * (barWidth + spacing)
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

fun formatMillisecondsClock(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

fun formatSecondsClock(sec: Int): String {
    val clean = sec.coerceAtLeast(0)
    val minutes = clean / 60
    val seconds = clean % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
