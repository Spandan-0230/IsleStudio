package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = AlabasterPrimary,
    onPrimary = ObsidianBackground,
    primaryContainer = StudioSurfaceElevated,
    onPrimaryContainer = AlabasterPrimary,
    secondary = AccentNavBlue,
    onSecondary = AlabasterPrimary,
    secondaryContainer = StudioSurfaceHigh,
    onSecondaryContainer = AlabasterPrimary,
    tertiary = AccentBatteryGreen,
    onTertiary = ObsidianBackground,
    background = ObsidianBackground,
    onBackground = AlabasterPrimary,
    surface = StudioSurface,
    onSurface = AlabasterPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = ZincSecondary,
    outline = HairlineBorder,
    outlineVariant = SpecularRim
)

private val StudioLightColorScheme = lightColorScheme(
    primary = LightInkPrimary,
    onPrimary = LightStudioSurface,
    primaryContainer = LightStudioElevated,
    onPrimaryContainer = LightInkPrimary,
    secondary = AccentNavBlue,
    onSecondary = LightStudioSurface,
    secondaryContainer = LightStudioElevated,
    onSecondaryContainer = LightInkPrimary,
    tertiary = AccentBatteryGreen,
    onTertiary = LightStudioSurface,
    background = LightStudioBackground,
    onBackground = LightInkPrimary,
    surface = LightStudioSurface,
    onSurface = LightInkPrimary,
    surfaceVariant = LightStudioElevated,
    onSurfaceVariant = LightInkSecondary,
    outline = LightHairlineBorder,
    outlineVariant = LightHairlineBorder
)

val StudioShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun IsleStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ObsidianDarkColorScheme else StudioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = StudioShapes,
        content = content
    )
}
