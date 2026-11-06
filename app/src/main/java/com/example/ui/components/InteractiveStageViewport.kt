package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IslandConfiguration
import com.example.model.IslandDisplayMode
import com.example.model.IslandModuleState
import com.example.model.parseHexColorSafely
import com.example.ui.theme.AlabasterPrimary
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.ZincMuted
import com.example.ui.theme.ZincSecondary
import java.util.Locale

@Composable
fun InteractiveStageViewport(
    configuration: IslandConfiguration,
    displayMode: IslandDisplayMode,
    primaryActivity: IslandModuleState,
    secondaryActivity: IslandModuleState?,
    isOverlayRunning: Boolean,
    onSelectDisplayMode: (IslandDisplayMode) -> Unit,
    onToggleAlignmentGrid: () -> Unit,
    onSwapDroplet: () -> Unit,
    onMediaPlayPause: () -> Unit,
    onMediaSkip: (Boolean) -> Unit,
    onTimerToggle: () -> Unit,
    onTimerReset: () -> Unit,
    onQuickReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = parseHexColorSafely(primaryActivity.accentHex.ifBlank { configuration.accentColorHex })

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = StudioSurface,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HairlineBorder, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Simulated Device Status Bar + Hardware Cutout Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "09:41",
                        color = AlabasterPrimary,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Surface(
                        shape = CircleShape,
                        color = if (isOverlayRunning) Color(0xFF34C759).copy(alpha = 0.18f) else Color(0xFF27272A)
                    ) {
                        Text(
                            text = if (isOverlayRunning) "OVERLAY LIVE" else "STUDIO STAGE",
                            color = if (isOverlayRunning) Color(0xFF34C759) else ZincSecondary,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = String.format(
                            Locale.US,
                            "X:%+.0f Y:%+.0f",
                            configuration.anchorMode.defaultXOffsetDp + configuration.offsetXDp,
                            configuration.offsetYDp
                        ),
                        color = ZincMuted,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        modifier = Modifier.testTag("stage_xy_telemetry")
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (configuration.showAlignmentGrid) accentColor.copy(alpha = 0.22f)
                                else Color(0xFF1C1C21)
                            )
                            .clickable { onToggleAlignmentGrid() }
                            .testTag("toggle_alignment_grid_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Toggle X/Y Alignment Reticle",
                            tint = if (configuration.showAlignmentGrid) accentColor else ZincSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = ZincSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = ZincSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Dynamic Island Stage Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(212.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0B0B0E))
                    .border(1.dp, Color(0xFF1E1E24), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                // Optional Sub-Pixel Alignment Crosshair Grid
                if (configuration.showAlignmentGrid) {
                    AlignmentReticleCanvas(
                        accentColor = accentColor,
                        offsetXDp = configuration.anchorMode.defaultXOffsetDp + configuration.offsetXDp,
                        offsetYDp = configuration.offsetYDp
                    )
                }

                // Live Dynamic Island Capsule hovering in viewport
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    DynamicIslandCapsule(
                        configuration = configuration,
                        displayMode = displayMode,
                        primaryActivity = primaryActivity,
                        secondaryActivity = secondaryActivity,
                        onTapCapsule = {
                            val next = when (displayMode) {
                                IslandDisplayMode.IDLE_CUTOUT -> IslandDisplayMode.COMPACT_PILL
                                IslandDisplayMode.COMPACT_PILL, IslandDisplayMode.SPLIT_MULTITASK ->
                                    IslandDisplayMode.EXPANDED_CARD
                                IslandDisplayMode.EXPANDED_CARD ->
                                    if (secondaryActivity != null) IslandDisplayMode.SPLIT_MULTITASK
                                    else IslandDisplayMode.COMPACT_PILL
                            }
                            onSelectDisplayMode(next)
                        },
                        onLongPressCapsule = {
                            onSelectDisplayMode(IslandDisplayMode.EXPANDED_CARD)
                        },
                        onSwipeDismiss = {
                            onSelectDisplayMode(IslandDisplayMode.IDLE_CUTOUT)
                        },
                        onSwapDroplet = onSwapDroplet,
                        onMediaPlayPause = onMediaPlayPause,
                        onMediaSkip = onMediaSkip,
                        onTimerToggle = onTimerToggle,
                        onTimerReset = onTimerReset,
                        onQuickReply = onQuickReply
                    )
                }

                // Bottom hint bar inside viewport
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap to morph • Long-press to bounce • Swipe to standby",
                        color = ZincMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "k=${configuration.springStiffness.toInt()} ζ=${String.format(Locale.US, "%.2f", configuration.springDamping)}",
                        color = ZincSecondary,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // State Mode Switcher Pills (Cutout | Compact | Split Droplet | Expanded)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IslandDisplayMode.entries.forEach { mode ->
                    val selected = displayMode == mode
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) AlabasterPrimary else Color(0xFF1A1A20),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectDisplayMode(mode) }
                            .testTag("mode_chip_${mode.name.lowercase(Locale.US)}")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.title,
                                color = if (selected) Color(0xFF09090B) else ZincSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlignmentReticleCanvas(
    accentColor: Color,
    offsetXDp: Float,
    offsetYDp: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSpacing = 24.dp.toPx()
        val gridColor = Color(0xFF27272A).copy(alpha = 0.55f)

        var x = 0f
        while (x < size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridSpacing
        }

        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridSpacing
        }

        // Precision Crosshair at current X/Y anchor
        val centerX = (size.width / 2f) + offsetXDp.dp.toPx()
        val centerY = 28.dp.toPx() + offsetYDp.dp.toPx()
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

        drawLine(
            color = accentColor.copy(alpha = 0.75f),
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = 1.5f,
            pathEffect = dashEffect
        )
        drawLine(
            color = accentColor.copy(alpha = 0.75f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.5f,
            pathEffect = dashEffect
        )
    }
}
