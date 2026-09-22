package com.micarro.feature.mileage.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.MileageReading
import com.micarro.domain.model.Vehicle
import com.micarro.feature.mileage.domain.AddMileageReadingUseCase
import com.micarro.feature.mileage.domain.AddReadingResult
import com.micarro.feature.mileage.domain.MileageError
import com.micarro.feature.mileage.domain.MileageRules
import com.micarro.feature.mileage.domain.ObserveMileageUseCase
import com.micarro.feature.vehicle.domain.GetVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MileageUiState(
    val vehicle: Vehicle? = null,
    val readings: List<MileageReading> = emptyList(),
    val odometerInput: String = "",
    val dateMillis: Long = MileageRules.dateToMillis(LocalDate.now()),
    val noteInput: String = "",
    val errors: Set<MileageError> = emptySet(),
    val pendingConfirmation: Long? = null,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class MileageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeMileage: ObserveMileageUseCase,
    private val getVehicle: GetVehicleUseCase,
    private val addReading: AddMileageReadingUseCase
) : ViewModel() {

    private val vehicleId: Long = checkNotNull(savedStateHandle.get<String>("vehicleId")?.toLongOrNull())

    private val vehicle = MutableStateFlow<Vehicle?>(null)
    private val form = MutableStateFlow(FormState())

    data class FormState(
        val odometerInput: String = "",
        val dateMillis: Long = MileageRules.dateToMillis(LocalDate.now()),
        val noteInput: String = "",
        val errors: Set<MileageError> = emptySet(),
        val pendingConfirmation: Long? = null,
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false
    )

    val uiState: StateFlow<MileageUiState> = combine(
        vehicle,
        observeMileage(vehicleId),
        form
    ) { v, readings, f ->
        MileageUiState(
            vehicle = v,
            readings = readings,
            odometerInput = f.odometerInput,
            dateMillis = f.dateMillis,
            noteInput = f.noteInput,
            errors = f.errors,
            pendingConfirmation = f.pendingConfirmation,
            isSaving = f.isSaving,
            saveFailed = f.saveFailed,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MileageUiState())

    init {
        viewModelScope.launch { vehicle.value = getVehicle(vehicleId) }
    }

    fun onOdometerChange(value: String) {
        form.update {
            it.copy(
                odometerInput = value,
                errors = it.errors - setOf(
                    MileageError.EMPTY, MileageError.NOT_A_NUMBER, MileageError.NEGATIVE
                ),
                // Cambiar el odómetro descarta la confirmación pendiente
                pendingConfirmation = null
            )
        }
    }

    fun onDateChange(millis: Long) {
        form.update {
            it.copy(
                dateMillis = millis,
                errors = it.errors - MileageError.FUTURE_DATE,
                pendingConfirmation = null
            )
        }
    }

    fun onNoteChange(value: String) {
        form.update { it.copy(noteInput = value) }
    }

    fun register(confirmedLowerReading: Boolean = false) {
        val f = form.value
        if (f.isSaving) return
        form.update { it.copy(isSaving = true, saveFailed = false) }

        viewModelScope.launch {
            when (val result = addReading(
                vehicleId = vehicleId,
                odometerText = f.odometerInput,
                dateMillis = f.dateMillis,
                note = f.noteInput,
                confirmedLowerReading = confirmedLowerReading
            )) {
                AddReadingResult.Success -> form.update {
                    FormState(dateMillis = it.dateMillis)
                }
                is AddReadingResult.RequiresConfirmation -> form.update {
                    it.copy(isSaving = false, pendingConfirmation = result.previousReading)
                }
                is AddReadingResult.Invalid -> form.update {
                    it.copy(isSaving = false, errors = result.errors, pendingConfirmation = null)
                }
                is AddReadingResult.Failure -> form.update {
                    it.copy(isSaving = false, saveFailed = true)
                }
            }
        }
    }

    fun confirmLowerReading() {
        form.update { it.copy(pendingConfirmation = null) }
        register(confirmedLowerReading = true)
    }

    fun dismissConfirmation() {
        form.update { it.copy(pendingConfirmation = null) }
    }
}
