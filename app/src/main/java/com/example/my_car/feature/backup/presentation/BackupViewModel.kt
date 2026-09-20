package com.example.my_car.feature.backup.presentation

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_car.feature.backup.domain.usecase.BackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupUseCase: BackupUseCase,
    private val contentResolver: ContentResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "No se pudo abrir el archivo de destino") }
                return@launch
            }

            backupUseCase.export(outputStream)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Respaldo exportado exitosamente") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al exportar: ${error.message}") }
                }
        }
    }

    fun restoreFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "No se pudo abrir el archivo seleccionado") }
                return@launch
            }

            backupUseCase.restore(inputStream)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Copia de seguridad restaurada correctamente") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al restaurar: ${error.message}") }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}