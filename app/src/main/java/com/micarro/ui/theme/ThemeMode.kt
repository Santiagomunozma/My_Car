package com.micarro.ui.theme

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class ThemeController(
    val mode: ThemeMode,
    val onChange: (ThemeMode) -> Unit
)
