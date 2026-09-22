package com.micarro.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.usecase.ClearAllDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val clearAllDataUseCase: ClearAllDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }

            // runCatching envuelve la llamada suspend y genera el Result<Unit> para onSuccess/onFailure
            runCatching {
                clearAllDataUseCase()
            }.onSuccess {
                _uiState.update {
                    it.copy(isDeleting = false, isDataCleared = true)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = "Error al eliminar datos: ${error.message}"
                    )
                }
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(isDataCleared = false) }
    }
}