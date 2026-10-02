package com.micarro.feature.history.presentation

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.HistoryFilter
import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.domain.usecase.ExportHistoryToCsvUseCase
import com.micarro.domain.usecase.ExportHistoryToPdfUseCase
import com.micarro.feature.alerts.domain.AlertScheduler
import com.micarro.feature.maintenance.domain.DeleteMaintenanceServiceUseCase
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
    private val exportHistoryToCsvUseCase: ExportHistoryToCsvUseCase,
    private val exportHistoryToPdfUseCase: ExportHistoryToPdfUseCase,
    private val deleteMaintenanceService: DeleteMaintenanceServiceUseCase,
    private val alertScheduler: AlertScheduler,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(HistoryFilter())
    val filterState: StateFlow<HistoryFilter> = _filterState.asStateFlow()

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    init {
        viewModelScope.launch {
            vehicleRepository.observeVehicles().collect { _vehicles.value = it }
        }
        viewModelScope.launch {
            _filterState.flatMapLatest { filter ->
                repository.observeHistory(vehicleId = filter.vehicleId, filter = filter)
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
        _filterState.value = _filterState.value.copy(category = newCategory)
    }

    fun onServiceTypeSelected(serviceType: String) {
        val current = _filterState.value.serviceType
        _filterState.value = _filterState.value.copy(serviceType = if (current == serviceType) null else serviceType)
    }

    fun onVehicleSelected(vehicleId: String?) {
        _filterState.value = _filterState.value.copy(vehicleId = vehicleId)
    }

    fun onWorkshopChanged(workshop: String) {
        _filterState.value = _filterState.value.copy(workshop = workshop.ifBlank { null })
    }

    fun onCostRangeChanged(min: String, max: String) {
        _filterState.value = _filterState.value.copy(
            minCost = min.toDoubleOrNull(),
            maxCost = max.toDoubleOrNull()
        )
    }

    fun onDateRangeChanged(start: Long?, end: Long?) {
        _filterState.value = _filterState.value.copy(startDate = start, endDate = end)
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            val plan = deleteMaintenanceService(serviceId) ?: return@launch
            val vehicle = plan.vehicleId.toLongOrNull()?.let { vehicleRepository.getVehicleById(it) }
            if (vehicle?.isArchived == true) {
                alertScheduler.cancelForActivity(plan.id)
            } else {
                alertScheduler.scheduleForActivity(plan, vehicleLabel = vehicle?.plate ?: plan.vehicleId)
            }
        }
    }

    fun clearFilters() {
        _filterState.value = HistoryFilter()
    }

    fun exportToCsv(uri: Uri, contentResolver: ContentResolver, vehiclePlate: String? = null, currencySymbol: String = "$") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userMessage = null) }

            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                _uiState.update {
                    it.copy(isLoading = false, userMessage = "No se pudo crear el archivo de destino.")
                }
                return@launch
            }

            exportHistoryToCsvUseCase(outputStream, vehiclePlate, currencySymbol)
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

    fun exportToPdf(uri: Uri, contentResolver: ContentResolver, vehiclePlate: String? = null, currencySymbol: String = "$") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userMessage = null) }
            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                _uiState.update { it.copy(isLoading = false, userMessage = "No se pudo crear el archivo de destino.") }
                return@launch
            }
            exportHistoryToPdfUseCase(outputStream, vehiclePlate, currencySymbol)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, userMessage = "Historial exportado a PDF con éxito.") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, userMessage = "Error al exportar: ${error.message}") }
                }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}