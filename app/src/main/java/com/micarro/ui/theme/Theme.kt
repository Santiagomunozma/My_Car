package com.micarro.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalThemeController = staticCompositionLocalOf {
    ThemeController(ThemeMode.SYSTEM) {}
}

val LocalDarkTheme = staticCompositionLocalOf { false }

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnPrimaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = BrandIndigo,
    onTertiary = OnBrandGradient,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = BrandIndigo,
    onTertiary = OnBrandGradient,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

@Composable
fun MyCarTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val targetScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = targetScheme.animateRoles()
    val statusColors = if (darkTheme) DarkStatusColors else LightStatusColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalStatusColors provides statusColors,
        LocalDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}

@Composable
private fun androidx.compose.material3.ColorScheme.animateRoles(): androidx.compose.material3.ColorScheme {
    val spec = motionTween<Color>(200)
    val primaryColor by animateColorAsState(this.primary, spec)
    val onPrimaryColor by animateColorAsState(this.onPrimary, spec)
    val backgroundColor by animateColorAsState(this.background, spec)
    val onBackgroundColor by animateColorAsState(this.onBackground, spec)
    val surfaceColor by animateColorAsState(this.surface, spec)
    val onSurfaceColor by animateColorAsState(this.onSurface, spec)
    val surfaceVariantColor by animateColorAsState(this.surfaceVariant, spec)
    val onSurfaceVariantColor by animateColorAsState(this.onSurfaceVariant, spec)
    val outlineColor by animateColorAsState(this.outline, spec)
    val outlineVariantColor by animateColorAsState(this.outlineVariant, spec)
    return copy(
        primary = primaryColor,
        onPrimary = onPrimaryColor,
        background = backgroundColor,
        onBackground = onBackgroundColor,
        surface = surfaceColor,
        onSurface = onSurfaceColor,
        surfaceVariant = surfaceVariantColor,
        onSurfaceVariant = onSurfaceVariantColor,
        outline = outlineColor,
        outlineVariant = outlineVariantColor
    )
}
