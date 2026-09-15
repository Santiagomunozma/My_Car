package com.micarro.app.feature.vehicle.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.app.domain.model.FuelType
import com.micarro.app.domain.model.Vehicle
import com.micarro.app.domain.model.VehicleType
import com.micarro.app.feature.vehicle.domain.ArchiveVehicleUseCase
import com.micarro.app.feature.vehicle.domain.GetVehicleUseCase
import com.micarro.app.feature.vehicle.domain.ObserveVehiclesUseCase
import com.micarro.app.feature.vehicle.domain.ReactivateVehicleUseCase
import com.micarro.app.feature.vehicle.domain.SaveVehicleResult
import com.micarro.app.feature.vehicle.domain.SaveVehicleUseCase
import com.micarro.app.feature.vehicle.domain.SetPrimaryVehicleUseCase
import com.micarro.app.feature.vehicle.domain.VehicleField
import com.micarro.app.feature.vehicle.domain.VehicleFieldError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehiclesUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val showArchived: Boolean = false,
    val loading: Boolean = true
) {
    val visibleVehicles: List<Vehicle>
        get() = vehicles.filter { it.isArchived == showArchived }
}

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    observeVehicles: ObserveVehiclesUseCase,
    private val setPrimary: SetPrimaryVehicleUseCase,
    private val archiveVehicle: ArchiveVehicleUseCase,
    private val reactivateVehicle: ReactivateVehicleUseCase
) : ViewModel() {

    private val showArchived = MutableStateFlow(false)

    val uiState: StateFlow<VehiclesUiState> = combine(
        observeVehicles(includeArchived = true),
        showArchived
    ) { vehicles, archived ->
        VehiclesUiState(vehicles = vehicles, showArchived = archived, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VehiclesUiState())

    fun onFilterChange(archived: Boolean) {
        showArchived.value = archived
    }

    fun setPrimary(vehicleId: Long) = viewModelScope.launch {
        setPrimary.invoke(vehicleId)
    }

    fun archive(vehicleId: Long) = viewModelScope.launch {
        archiveVehicle(vehicleId)
    }

    fun reactivate(vehicleId: Long) = viewModelScope.launch {
        reactivateVehicle(vehicleId)
    }
}

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeVehicles: ObserveVehiclesUseCase
) : ViewModel() {

    private val vehicleId: Long = checkNotNull(savedStateHandle["vehicleId"])

    val vehicle: StateFlow<Vehicle?> = observeVehicles(includeArchived = true)
        .map { vehicles -> vehicles.firstOrNull { it.id == vehicleId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

data class VehicleFormState(
    val plate: String = "",
    val type: VehicleType = VehicleType.CAR,
    val brand: String = "",
    val line: String = "",
    val model: String = "",
    val year: String = "",
    val mileage: String = "",
    val color: String = "",
    val vin: String = "",
    val fuelType: FuelType? = null,
    val engineCc: String = "",
    val isPrimary: Boolean = false,
    val fieldErrors: Map<VehicleField, VehicleFieldError> = emptyMap(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val editingId: Long? = null
)

@HiltViewModel
class VehicleFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getVehicle: GetVehicleUseCase,
    private val saveVehicle: SaveVehicleUseCase
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.get<Long>("vehicleId") ?: -1L

    private val _uiState = MutableStateFlow(VehicleFormState())
    val uiState: StateFlow<VehicleFormState> = _uiState.asStateFlow()

    init {
        if (vehicleId > 0L) {
            viewModelScope.launch {
                getVehicle(vehicleId)?.let { vehicle ->
                    _uiState.update {
                        it.copy(
                            plate = vehicle.plate,
                            type = vehicle.type,
                            brand = vehicle.brand,
                            line = vehicle.line,
                            model = vehicle.model,
                            year = vehicle.year.toString(),
                            mileage = vehicle.currentMileage.toString(),
                            color = vehicle.color.orEmpty(),
                            vin = vehicle.vin.orEmpty(),
                            fuelType = vehicle.fuelType,
                            engineCc = vehicle.engineCc?.toString().orEmpty(),
                            isPrimary = vehicle.isPrimary,
                            editingId = vehicle.id
                        )
                    }
                }
            }
        }
    }

    fun update(transform: (VehicleFormState) -> VehicleFormState) {
        _uiState.update(transform)
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true, fieldErrors = emptyMap()) }

        val vehicle = Vehicle(
            id = state.editingId ?: 0L,
            plate = state.plate,
            type = state.type,
            brand = state.brand,
            line = state.line,
            model = state.model,
            year = state.year.toIntOrNull() ?: 0,
            currentMileage = state.mileage.toLongOrNull() ?: -1L,
            color = state.color.ifBlank { null },
            vin = state.vin.ifBlank { null },
            fuelType = state.fuelType,
            engineCc = state.engineCc.toIntOrNull(),
            isPrimary = state.isPrimary
        )

        viewModelScope.launch {
            when (val result = saveVehicle(vehicle)) {
                is SaveVehicleResult.Success -> _uiState.update {
                    it.copy(isSaving = false, saved = true)
                }
                is SaveVehicleResult.Invalid -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        fieldErrors = result.errors.associateBy { e -> e.field }
                    )
                }
            }
        }
    }
}
