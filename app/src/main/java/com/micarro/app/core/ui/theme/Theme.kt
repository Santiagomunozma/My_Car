package com.micarro.app.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryDarkBlue,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    outline = BorderLight,
    error = StateError,
    onError = SurfaceWhite
)

@Composable
fun MiCarroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
