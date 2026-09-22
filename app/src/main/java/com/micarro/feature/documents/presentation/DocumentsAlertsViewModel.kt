package com.micarro.feature.documents.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.AlertSettings
import com.micarro.domain.repository.AlertSettingsRepository
import com.micarro.feature.documents.domain.DocumentAlert
import com.micarro.feature.documents.domain.ObserveDocumentAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentsAlertsUiState(
    val alerts: List<DocumentAlert> = emptyList(),
    val settings: AlertSettings = AlertSettings(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DocumentsAlertsViewModel @Inject constructor(
    observeDocumentAlerts: ObserveDocumentAlertsUseCase,
    private val settingsRepository: AlertSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<DocumentsAlertsUiState> = combine(
        observeDocumentAlerts(),
        settingsRepository.observeSettings()
    ) { alerts, settings ->
        DocumentsAlertsUiState(alerts = alerts, settings = settings, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DocumentsAlertsUiState())

    fun setGlobalAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setGlobalAlertsEnabled(enabled) }
    }

    fun setAnticipationDays(days: Int) {
        viewModelScope.launch { settingsRepository.setAnticipationDays(days) }
    }
}
