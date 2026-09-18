package com.example.my_car.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryBlue,
    onPrimaryContainer = SurfaceWhite,
    secondary = StatusInfo,
    onSecondary = SurfaceWhite,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderGray,
    error = StatusError,
    onError = SurfaceWhite
)

private val DarkSurface = androidx.compose.ui.graphics.Color(0xFF111827)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    background = DarkSurface,
    onBackground = SurfaceWhite,
    surface = DarkSurface,
    onSurface = SurfaceWhite,
    surfaceVariant = TextSecondary,
    onSurfaceVariant = SurfaceWhite,
    outline = TextSecondary,
    error = StatusError,
    onError = SurfaceWhite
)

@Composable
fun MyCarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
