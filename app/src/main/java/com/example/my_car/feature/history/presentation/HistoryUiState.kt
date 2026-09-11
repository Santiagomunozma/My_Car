package com.example.my_car.feature.history.presentation

import com.example.my_car.domain.model.HistoryFilter
import com.example.my_car.domain.model.MaintenanceHistoryItem

data class HistoryUiState(
    val isLoading: Boolean = false,
    val items: List<MaintenanceHistoryItem> = emptyList(),
    val filter: HistoryFilter = HistoryFilter()
)