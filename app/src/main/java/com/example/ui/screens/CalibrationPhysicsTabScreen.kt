package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.IslandConfiguration
import com.example.model.NotchAnchorMode
import com.example.model.ThemePreference
import com.example.model.parseHexColorSafely
import com.example.ui.theme.AlabasterPrimary
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.ZincMuted
import com.example.ui.theme.ZincSecondary
import java.util.Locale
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sqrt

@Composable
fun CalibrationPhysicsTabScreen(
    configuration: IslandConfiguration,
    onUpdateOffsets: (Float, Float) -> Unit,
    onUpdateAnchorMode: (NotchAnchorMode) -> Unit,
    onUpdateDimensions: (Float, Float, Float, Float, Float) -> Unit,
    onUpdateSpringPhysics: (Float, Float) -> Unit,
    onUpdateStyling: (String, Float, Boolean, ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAccent = parseHexColorSafely(configuration.accentColorHex)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Sub-Pixel X/Y Cutout Calibration Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HairlineBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Precision X/Y Cutout Alignment",
                            style = MaterialTheme.typography.titleMedium,
                            color = AlabasterPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioSurfaceElevated,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onUpdateOffsets(0f, 8f) }
                            .testTag("reset_xy_button")
                    ) {
                        Text(
                            text = "Center (0, +8)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZincSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Anchor Preset Selector (Center, Left Punch-Hole, Right Punch-Hole)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NotchAnchorMode.entries.forEach { anchor ->
                        val selected = configuration.anchorMode == anchor
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) AlabasterPrimary else StudioSurfaceElevated,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onUpdateAnchorMode(anchor) }
                                .testTag("anchor_mode_${anchor.name.lowercase(Locale.US)}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = anchor.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.Black else ZincSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Horizontal X Offset Slider + Micro-Step Buttons
                CalibrationSliderWithSteppers(
                    label = "Horizontal Offset (X)",
                    valueText = String.format(Locale.US, "%+.0f dp", configuration.offsetXDp),
                    value = configuration.offsetXDp,
                    valueRange = -160f..160f,
                    accentColor = activeAccent,
                    onValueChange = { onUpdateOffsets(it, configuration.offsetYDp) },
                    onStepMinus = { onUpdateOffsets(configuration.offsetXDp - 1f, configuration.offsetYDp) },
                    onStepPlus = { onUpdateOffsets(configuration.offsetXDp + 1f, configuration.offsetYDp) },
                    sliderTag = "slider_x_offset",
                    stepPlusTag = "step_x_plus"
                )

                // Vertical Y Offset Slider + Micro-Step Buttons
                CalibrationSliderWithSteppers(
                    label = "Vertical Offset (Y)",
                    valueText = String.format(Locale.US, "%+.0f dp", configuration.offsetYDp),
                    value = configuration.offsetYDp,
                    valueRange = 0f..96f,
                    accentColor = activeAccent,
                    onValueChange = { onUpdateOffsets(configuration.offsetXDp, it) },
                    onStepMinus = { onUpdateOffsets(configuration.offsetXDp, configuration.offsetYDp - 1f) },
                    onStepPlus = { onUpdateOffsets(configuration.offsetXDp, configuration.offsetYDp + 1f) },
                    sliderTag = "slider_y_offset",
                    stepPlusTag = "step_y_plus"
                )

                // Capsule Geometry Sliders
                SimpleLabeledSlider(
                    label = "Compact Capsule Width",
                    valueText = "${configuration.compactWidthDp.toInt()} dp",
                    value = configuration.compactWidthDp,
                    valueRange = 130f..250f,
                    accentColor = activeAccent,
                    onValueChange = {
                        onUpdateDimensions(
                            it,
                            configuration.compactHeightDp,
                            configuration.expandedWidthDp,
                            configuration.expandedHeightDp,
                            configuration.cornerRadiusDp
                        )
                    }
                )

                SimpleLabeledSlider(
                    label = "Compact Capsule Height",
                    valueText = "${configuration.compactHeightDp.toInt()} dp",
                    value = configuration.compactHeightDp,
                    valueRange = 28f..52f,
                    accentColor = activeAccent,
                    onValueChange = {
                        onUpdateDimensions(
                            configuration.compactWidthDp,
                            it,
                            configuration.expandedWidthDp,
                            configuration.expandedHeightDp,
                            configuration.cornerRadiusDp
                        )
                    }
                )

                SimpleLabeledSlider(
                    label = "Expanded Card Corner Radius",
                    valueText = "${configuration.cornerRadiusDp.toInt()} dp",
                    value = configuration.cornerRadiusDp,
                    valueRange = 20f..48f,
                    accentColor = activeAccent,
                    onValueChange = {
                        onUpdateDimensions(
                            configuration.compactWidthDp,
                            configuration.compactHeightDp,
                            configuration.expandedWidthDp,
                            configuration.expandedHeightDp,
                            it
                        )
                    }
                )
            }
        }

        // 2. Damped-Harmonic Spring Physics Card + Real-Time Step Response Curve
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HairlineBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Fluid Spring Physics Engine",
                        style = MaterialTheme.typography.titleMedium,
                        color = AlabasterPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Live Harmonic Oscillator Response Curve Canvas
                SpringResponseCurveCanvas(
                    stiffness = configuration.springStiffness,
                    dampingRatio = configuration.springDamping,
                    accentColor = activeAccent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C0C10))
                        .border(1.dp, Color(0xFF222228), RoundedCornerShape(12.dp))
                )

                SimpleLabeledSlider(
                    label = "Spring Stiffness (k)",
                    valueText = "${configuration.springStiffness.toInt()} N/m",
                    value = configuration.springStiffness,
                    valueRange = 200f..1400f,
                    accentColor = activeAccent,
                    onValueChange = { onUpdateSpringPhysics(it, configuration.springDamping) }
                )

                SimpleLabeledSlider(
                    label = "Damping Ratio (ζ — Lower = Bouncier)",
                    valueText = String.format(Locale.US, "%.2f", configuration.springDamping),
                    value = configuration.springDamping,
                    valueRange = 0.40f..1.00f,
                    accentColor = activeAccent,
                    onValueChange = { onUpdateSpringPhysics(configuration.springStiffness, it) }
                )
            }
        }

        // 3. Minimalist Accent Palette & Theme Posture Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, HairlineBorder, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Capsule Chroma & Theme Posture",
                        style = MaterialTheme.typography.titleMedium,
                        color = AlabasterPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                val swatches = listOf(
                    "#FA2D48" to "Media Coral",
                    "#FF9F0A" to "Chrono Amber",
                    "#34C759" to "System Green",
                    "#0A84FF" to "Precision Blue",
                    "#F4F4F6" to "Pure Monochrome"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    swatches.forEach { (hex, name) ->
                        val color = parseHexColorSafely(hex)
                        val isSelected = configuration.accentColorHex.equals(hex, ignoreCase = true)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onUpdateStyling(
                                        hex,
                                        configuration.borderGlowOpacity,
                                        configuration.showAlignmentGrid,
                                        configuration.themePreference
                                    )
                                }
                                .padding(6.dp)
                                .testTag("swatch_${hex.removePrefix("#").lowercase(Locale.US)}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) AlabasterPrimary else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = name.substringAfter(" "),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) AlabasterPrimary else ZincMuted
                            )
                        }
                    }
                }

                SimpleLabeledSlider(
                    label = "Specular Rim Glow Opacity",
                    valueText = "${(configuration.borderGlowOpacity * 100).toInt()}%",
                    value = configuration.borderGlowOpacity,
                    valueRange = 0.05f..0.85f,
                    accentColor = activeAccent,
                    onValueChange = {
                        onUpdateStyling(
                            configuration.accentColorHex,
                            it,
                            configuration.showAlignmentGrid,
                            configuration.themePreference
                        )
                    }
                )

                // Dark / Light / Auto Theme Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThemePreference.entries.forEach { pref ->
                        val selected = configuration.themePreference == pref
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) AlabasterPrimary else StudioSurfaceElevated,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onUpdateStyling(
                                        configuration.accentColorHex,
                                        configuration.borderGlowOpacity,
                                        configuration.showAlignmentGrid,
                                        pref
                                    )
                                }
                                .testTag("theme_pref_${pref.name.lowercase(Locale.US)}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pref.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.Black else ZincSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
private fun CalibrationSliderWithSteppers(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    onValueChange: (Float) -> Unit,
    onStepMinus: () -> Unit,
    onStepPlus: () -> Unit,
    sliderTag: String,
    stepPlusTag: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = ZincSecondary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioSurfaceElevated,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onStepMinus() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease 1dp",
                            tint = AlabasterPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = valueText,
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.labelLarge,
                    color = AlabasterPrimary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioSurfaceElevated,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onStepPlus() }
                        .testTag(stepPlusTag)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase 1dp",
                            tint = AlabasterPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = AlabasterPrimary,
                activeTrackColor = accentColor,
                inactiveTrackColor = StudioSurfaceElevated
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .testTag(sliderTag)
        )
    }
}

@Composable
private fun SimpleLabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = ZincSecondary
            )
            Text(
                text = valueText,
                fontFamily = JetBrainsMonoFamily,
                style = MaterialTheme.typography.labelLarge,
                color = AlabasterPrimary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = AlabasterPrimary,
                activeTrackColor = accentColor,
                inactiveTrackColor = StudioSurfaceElevated
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        )
    }
}

@Composable
private fun SpringResponseCurveCanvas(
    stiffness: Float,
    dampingRatio: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val baselineY = size.height * 0.45f
        drawLine(
            color = Color(0xFF27272A),
            start = Offset(0f, baselineY),
            end = Offset(size.width, baselineY),
            strokeWidth = 1.dp.toPx()
        )

        val path = Path()
        val omega = sqrt(stiffness.toDouble()) * 0.35
        val zeta = dampingRatio.toDouble().coerceIn(0.25, 0.99)
        val dampedOmega = omega * sqrt(1.0 - zeta * zeta)
        val steps = 90

        for (i in 0..steps) {
            val t = (i.toDouble() / steps) * 0.85
            val envelope = exp(-zeta * omega * t)
            val oscillation = cos(dampedOmega * t)
            val yNorm = 1.0 - envelope * oscillation
            val px = (i.toFloat() / steps) * size.width
            val py = (size.height * 0.88f) - (yNorm.toFloat() * (size.height * 0.43f))
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }

        drawPath(
            path = path,
            color = accentColor,
            style = Stroke(width = 2.2.dp.toPx())
        )
    }
}
