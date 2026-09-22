package com.micarro.feature.vehicle.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.Vehicle
import com.micarro.feature.documents.domain.ObserveDocumentAlertsUseCase
import com.micarro.feature.vehicle.domain.ArchiveVehicleUseCase
import com.micarro.feature.vehicle.domain.ObserveVehiclesUseCase
import com.micarro.feature.vehicle.domain.ReactivateVehicleUseCase
import com.micarro.feature.vehicle.domain.SetMainVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehiclesUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val showArchived: Boolean = false,
    val pendingAlertsCount: Int = 0,
    val vehicleToArchive: Vehicle? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VehiclesViewModel @Inject constructor(
    observeVehicles: ObserveVehiclesUseCase,
    observeDocumentAlerts: ObserveDocumentAlertsUseCase,
    private val archiveVehicle: ArchiveVehicleUseCase,
    private val reactivateVehicle: ReactivateVehicleUseCase,
    private val setMainVehicle: SetMainVehicleUseCase
) : ViewModel() {

    private val showArchived = MutableStateFlow(false)
    private val vehicleToArchive = MutableStateFlow<Vehicle?>(null)

    private val vehiclesFlow = showArchived.flatMapLatest { observeVehicles(it) }

    val uiState: StateFlow<VehiclesUiState> = combine(
        vehiclesFlow,
        observeDocumentAlerts(),
        showArchived,
        vehicleToArchive
    ) { vehicles, alerts, archived, toArchive ->
        VehiclesUiState(
            vehicles = vehicles,
            showArchived = archived,
            pendingAlertsCount = alerts.size,
            vehicleToArchive = toArchive,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VehiclesUiState()
    )

    fun toggleShowArchived() {
        showArchived.value = !showArchived.value
    }

    fun requestArchive(vehicle: Vehicle) {
        vehicleToArchive.value = vehicle
    }

    fun dismissArchive() {
        vehicleToArchive.value = null
    }

    fun confirmArchive() {
        val vehicle = vehicleToArchive.value ?: return
        vehicleToArchive.value = null
        viewModelScope.launch { archiveVehicle(vehicle.id) }
    }

    fun reactivate(vehicleId: String) {
        viewModelScope.launch { reactivateVehicle(vehicleId) }
    }

    fun setMain(vehicleId: String) {
        viewModelScope.launch { setMainVehicle(vehicleId) }
    }
}
