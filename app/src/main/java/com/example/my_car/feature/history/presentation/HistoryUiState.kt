package com.example.my_car.feature.history.presentation

import com.example.my_car.domain.model.HistoryFilter
import com.example.my_car.domain.model.MaintenanceHistoryItem

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter(),
    val items: List<MaintenanceHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null
)