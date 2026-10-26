package com.micarro.feature.history.presentation

import com.micarro.domain.model.HistoryFilter
import com.micarro.domain.model.MaintenanceHistoryItem

data class HistoryNotice(
    val messageRes: Int,
    val detail: String? = null
)

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter(),
    val items: List<MaintenanceHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val notice: HistoryNotice? = null
)