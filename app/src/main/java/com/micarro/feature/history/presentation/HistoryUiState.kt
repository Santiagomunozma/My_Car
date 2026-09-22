package com.micarro.feature.history.presentation

import com.micarro.domain.model.HistoryFilter
import com.micarro.domain.model.MaintenanceHistoryItem

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter(),
    val items: List<MaintenanceHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null
)