package com.micarro.feature.settings.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.R
import com.micarro.domain.usecase.ClearAllDataUseCase
import com.micarro.feature.demo.DemoDataSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val clearAllDataUseCase: ClearAllDataUseCase,
    private val demoDataSeeder: DemoDataSeeder,
    @param:ApplicationContext private val context: Context
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
                        errorMessage = context.getString(
                            R.string.settings_delete_error,
                            error.message ?: ""
                        )
                    )
                }
            }
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSeeding = true, errorMessage = null, demoLoaded = false) }
            runCatching { demoDataSeeder.replaceWithDemo() }
                .onSuccess {
                    _uiState.update { it.copy(isSeeding = false, demoLoaded = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSeeding = false,
                            errorMessage = context.getString(
                                R.string.settings_demo_error,
                                error.message ?: ""
                            )
                        )
                    }
                }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(isDataCleared = false) }
    }

    fun onDemoHandled() {
        _uiState.update { it.copy(demoLoaded = false) }
    }
}