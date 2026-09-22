package com.micarro.feature.history.presentation

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.HistoryFilter
import com.micarro.domain.model.MaintenanceHistoryItem
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.usecase.ExportHistoryToCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: MaintenanceRepository,
    private val exportHistoryToCsvUseCase: ExportHistoryToCsvUseCase
) : ViewModel() {

    private val _filterState = MutableStateFlow(HistoryFilter())
    val filterState: StateFlow<HistoryFilter> = _filterState.asStateFlow()

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _filterState.flatMapLatest { filter ->
                repository.observeHistory(vehicleId = null, filter = filter)
            }.collect { items ->
                _uiState.update { it.copy(items = items, filter = _filterState.value) }
            }
        }
    }

    fun onQueryChanged(query: String) {
        val newFilter = _filterState.value.copy(query = query.ifBlank { null })
        _filterState.value = newFilter
    }

    fun onCategorySelected(category: String) {
        val currentCategory = _filterState.value.category
        val newCategory = if (currentCategory == category) null else category
        val newFilter = _filterState.value.copy(category = newCategory)
        _filterState.value = newFilter
    }

    fun clearFilters() {
        _filterState.value = HistoryFilter()
    }

    fun exportToCsv(uri: Uri, contentResolver: ContentResolver, vehiclePlate: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userMessage = null) }

            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                _uiState.update {
                    it.copy(isLoading = false, userMessage = "No se pudo crear el archivo de destino.")
                }
                return@launch
            }

            exportHistoryToCsvUseCase(outputStream, vehiclePlate)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, userMessage = "Historial exportado a CSV con éxito.")
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, userMessage = "Error al exportar: ${error.message}")
                    }
                }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}