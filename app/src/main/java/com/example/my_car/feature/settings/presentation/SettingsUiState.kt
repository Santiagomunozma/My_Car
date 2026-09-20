package com.example.my_car.feature.settings.presentation

data class SettingsUiState(
    val isDeleting: Boolean = false,
    val isDataCleared: Boolean = false,
    val errorMessage: String? = null
)