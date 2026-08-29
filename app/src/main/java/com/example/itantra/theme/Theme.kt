package com.example.itantra.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * iTantra tactical dark theme.
 *
 * Always dark mode — designed for high-contrast readability in
 * field conditions (bright sunlight, low-light rescue environments).
 * No dynamic color / no light theme to keep it consistent across all devices.
 */
private val TacticalDarkColorScheme = darkColorScheme(
    primary = TacticalGreenLight,
    onPrimary = TextOnGreen,
    primaryContainer = TacticalGreenDark,
    onPrimaryContainer = TacticalGreenLight,

    secondary = AlertAmber,
    onSecondary = TextOnAmber,
    secondaryContainer = AlertAmberDark,
    onSecondaryContainer = AlertAmberLight,

    tertiary = EmergencyRed,
    onTertiary = TextPrimary,
    tertiaryContainer = EmergencyRedDark,
    onTertiaryContainer = EmergencyRedBright,

    error = EmergencyRed,
    onError = TextPrimary,
    errorContainer = EmergencyRedDark,
    onErrorContainer = EmergencyRedBright,

    background = DarkBackground,
    onBackground = TextPrimary,

    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,

    outline = TacticalGreenDark,
    outlineVariant = DarkSurfaceElevated,

    inverseSurface = TextPrimary,
    inverseOnSurface = DarkBackground,
    inversePrimary = TacticalGreen
)

@Composable
fun ITantraTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = TacticalDarkColorScheme,
        typography = Typography,
        content = content
    )
}
