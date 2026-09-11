package com.example.my_car.feature.settings.presentation

data class SettingsUiState(
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val message: String? = null
)