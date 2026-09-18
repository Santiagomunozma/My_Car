package com.example.my_car.feature.vehicle.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_car.feature.vehicle.domain.SaveVehicleResult
import com.example.my_car.feature.vehicle.domain.SaveVehicleUseCase
import com.example.my_car.feature.vehicle.domain.GetVehicleUseCase
import com.example.my_car.feature.vehicle.domain.VehicleDraft
import com.example.my_car.feature.vehicle.domain.VehicleError
import com.example.my_car.feature.vehicle.domain.VehicleField
import com.example.my_car.feature.vehicle.domain.VehiclePhotoStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class VehicleFormUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val saveFailed: Boolean = false,
    val vehicleGone: Boolean = false,
    val draft: VehicleDraft = VehicleDraft(),
    val errors: Map<VehicleField, VehicleError> = emptyMap(),
    val photoImportFailed: Boolean = false,
    val isImportingPhoto: Boolean = false
)

@HiltViewModel
class VehicleFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveVehicle: SaveVehicleUseCase,
    private val getVehicle: GetVehicleUseCase,
    private val photoStore: VehiclePhotoStore
) : ViewModel() {

    private val vehicleId: String? = savedStateHandle.get<String>("vehicleId")

    private val _uiState = MutableStateFlow(VehicleFormUiState())
    val uiState: StateFlow<VehicleFormUiState> = _uiState.asStateFlow()

    init {
        if (vehicleId != null) {
            viewModelScope.launch {
                val vehicle = getVehicle(vehicleId)
                if (vehicle == null) {
                    _uiState.update {
                        it.copy(isLoading = false, vehicleGone = true, saveFailed = true)
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            draft = VehicleDraft(
                                id = vehicle.id,
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
                                photoUri = vehicle.photoUri
                            )
                        )
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun updateDraft(transform: (VehicleDraft) -> VehicleDraft) {
        _uiState.update { it.copy(draft = transform(it.draft), saveFailed = false) }
    }

    fun clearFieldError(field: VehicleField) {
        _uiState.update { it.copy(errors = it.errors - field) }
    }

    fun onPhotoPicked(sourceUri: String) {
        if (_uiState.value.isImportingPhoto) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImportingPhoto = true, photoImportFailed = false) }
            try {
                val path = photoStore.importPhoto(sourceUri)
                _uiState.update {
                    it.copy(
                        isImportingPhoto = false,
                        draft = it.draft.copy(photoUri = path)
                    )
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isImportingPhoto = false, photoImportFailed = true)
                }
            }
        }
    }

    fun removePhoto() {
        _uiState.update { it.copy(draft = it.draft.copy(photoUri = null)) }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving || state.isLoading) return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            when (val result = saveVehicle(state.draft)) {
                SaveVehicleResult.Success ->
                    _uiState.update { it.copy(isSaving = false, saved = true) }
                is SaveVehicleResult.Invalid ->
                    _uiState.update { it.copy(isSaving = false, errors = result.errors) }
                is SaveVehicleResult.Failure ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveFailed = true,
                            vehicleGone = result.cause.message == "vehicle_gone"
                        )
                    }
            }
        }
    }
}
