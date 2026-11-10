package com.micarro.feature.settings.presentation

data class SettingsUiState(
    val isDeleting: Boolean = false,
    val isDataCleared: Boolean = false,
    val isSeeding: Boolean = false,
    val demoLoaded: Boolean = false,
    val errorMessage: String? = null
)