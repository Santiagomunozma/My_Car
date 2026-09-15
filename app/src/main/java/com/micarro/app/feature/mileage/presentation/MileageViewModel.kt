package com.micarro.app.feature.mileage.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.app.domain.model.MileageReading
import com.micarro.app.domain.model.Vehicle
import com.micarro.app.feature.mileage.domain.AddMileageReadingUseCase
import com.micarro.app.feature.mileage.domain.AddReadingResult
import com.micarro.app.feature.mileage.domain.ObserveMileageUseCase
import com.micarro.app.feature.vehicle.domain.GetVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MileageUiState(
    val vehicle: Vehicle? = null,
    val readings: List<MileageReading> = emptyList(),
    val input: String = "",
    val inputError: Boolean = false,
    val pendingLowerReading: Long? = null,
    val referenceMileage: Long = 0L,
    val loading: Boolean = true
)

@HiltViewModel
class MileageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeMileage: ObserveMileageUseCase,
    private val getVehicle: GetVehicleUseCase,
    private val addReading: AddMileageReadingUseCase
) : ViewModel() {

    private val vehicleId: Long = checkNotNull(savedStateHandle["vehicleId"])

    private val vehicle = MutableStateFlow<Vehicle?>(null)
    private val form = MutableStateFlow(MileageUiState())

    val uiState: StateFlow<MileageUiState> = combine(
        vehicle,
        observeMileage(vehicleId),
        form
    ) { v, readings, f ->
        f.copy(vehicle = v, readings = readings, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MileageUiState())

    init {
        viewModelScope.launch {
            vehicle.value = getVehicle(vehicleId)
        }
    }

    fun onInputChange(value: String) {
        form.update { it.copy(input = value.filter { c -> c.isDigit() }, inputError = false) }
    }

    fun register(dateEpochMs: Long = System.currentTimeMillis()) {
        val mileage = form.value.input.toLongOrNull()
        if (mileage == null) {
            form.update { it.copy(inputError = true) }
            return
        }
        submit(mileage, dateEpochMs, confirmed = false)
    }

    fun confirmLowerReading() {
        val mileage = form.value.pendingLowerReading ?: return
        form.update { it.copy(pendingLowerReading = null) }
        submit(mileage, System.currentTimeMillis(), confirmed = true)
    }

    fun dismissLowerReading() {
        form.update { it.copy(pendingLowerReading = null) }
    }

    private fun submit(mileage: Long, dateEpochMs: Long, confirmed: Boolean) {
        viewModelScope.launch {
            when (val result = addReading(vehicleId, mileage, dateEpochMs, confirmed)) {
                is AddReadingResult.Saved -> {
                    form.update { it.copy(input = "", inputError = false) }
                    vehicle.value = getVehicle(vehicleId)
                }
                is AddReadingResult.NeedsConfirmation -> form.update {
                    it.copy(pendingLowerReading = mileage, referenceMileage = result.referenceMileage)
                }
                else -> form.update { it.copy(inputError = true) }
            }
        }
    }
}
