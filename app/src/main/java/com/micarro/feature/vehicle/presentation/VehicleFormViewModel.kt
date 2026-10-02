package com.micarro.feature.vehicle.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.FuelType
import com.micarro.domain.model.VehicleType
import com.micarro.feature.vehicle.data.VehicleCatalogHelper
import com.micarro.feature.vehicle.domain.GetVehicleUseCase
import com.micarro.feature.vehicle.domain.SaveVehicleResult
import com.micarro.feature.vehicle.domain.SaveVehicleUseCase
import com.micarro.feature.vehicle.domain.VehicleDraft
import com.micarro.feature.vehicle.domain.VehicleError
import com.micarro.feature.vehicle.domain.VehicleField
import com.micarro.feature.vehicle.domain.VehiclePhotoStore
import com.micarro.feature.vehicle.domain.VehicleRules
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Year
import javax.inject.Inject

data class VehicleFormState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val saveFailed: Boolean = false,
    val vehicleGone: Boolean = false,
    val typeKey: String = "CARRO", // "CARRO" or "MOTO"
    val draft: VehicleDraft = VehicleDraft(),
    val availableBrands: List<String> = emptyList(),
    val availableLines: List<String> = emptyList(),
    val availableYears: List<String> = (Year.now().value + 1 downTo 2000).map { it.toString() },
    val errors: Map<VehicleField, VehicleError> = emptyMap(),
    val photoImportFailed: Boolean = false,
    val isImportingPhoto: Boolean = false
) {
    val isFormValid: Boolean
        get() {
            val isPlateValid = VehicleRules.isPlateValid(draft.plate, typeKey)
            val isBrandValid = draft.brand.isNotBlank()
            val effectiveLine = draft.line.ifBlank { draft.model }
            val isLineValid = effectiveLine.isNotBlank()
            val yearInt = draft.year.trim().toIntOrNull()
            val isYearValid = yearInt != null && VehicleRules.isYearValid(yearInt)
            val isMileageValid = if (isEditing) true else {
                val mileageLong = draft.mileage.trim().toLongOrNull()
                mileageLong != null && VehicleRules.isMileageValid(mileageLong)
            }
            val isVinValid = draft.vin.isBlank() || VehicleRules.isVinValid(draft.vin)
            val engineCcInt = draft.engineCc.trim().toIntOrNull()
            val isEngineCcValid = draft.engineCc.isBlank() || (engineCcInt != null && VehicleRules.isEngineCcValid(engineCcInt))

            return isPlateValid && isBrandValid && isLineValid && isYearValid &&
                    isMileageValid && isVinValid && isEngineCcValid
        }
}

@HiltViewModel
class VehicleFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveVehicle: SaveVehicleUseCase,
    private val getVehicle: GetVehicleUseCase,
    private val photoStore: VehiclePhotoStore,
    private val catalogHelper: VehicleCatalogHelper
) : ViewModel() {

    private val vehicleId: Long? = savedStateHandle.get<String>("vehicleId")?.toLongOrNull()

    private val _uiState = MutableStateFlow(VehicleFormState())
    val uiState: StateFlow<VehicleFormState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (vehicleId != null) {
                val vehicle = getVehicle(vehicleId)
                if (vehicle == null) {
                    _uiState.update {
                        it.copy(isLoading = false, vehicleGone = true, saveFailed = true)
                    }
                } else {
                    val currentTypeKey = if (vehicle.type == VehicleType.MOTORCYCLE) "MOTO" else "CARRO"
                    val brands = catalogHelper.getBrands(currentTypeKey)
                    val lines = if (vehicle.brand.isNotBlank()) {
                        catalogHelper.getLines(currentTypeKey, vehicle.brand)
                    } else emptyList()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditing = true,
                            typeKey = currentTypeKey,
                            availableBrands = brands,
                            availableLines = lines,
                            draft = VehicleDraft(
                                id = vehicle.id,
                                plate = vehicle.plate,
                                type = vehicle.type,
                                brand = vehicle.brand,
                                line = vehicle.line,
                                model = vehicle.model.ifEmpty { vehicle.line },
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
            } else {
                val initialTypeKey = "CARRO"
                val brands = catalogHelper.getBrands(initialTypeKey)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        typeKey = initialTypeKey,
                        availableBrands = brands,
                        draft = it.draft.copy(type = VehicleType.CAR)
                    )
                }
            }
        }
    }

    fun onTypeSelected(typeKey: String) {
        if (_uiState.value.typeKey == typeKey) return
        val vehicleType = if (typeKey == "MOTO") VehicleType.MOTORCYCLE else VehicleType.CAR
        _uiState.update {
            it.copy(
                typeKey = typeKey,
                availableLines = emptyList(),
                draft = it.draft.copy(
                    type = vehicleType,
                    brand = "",
                    line = "",
                    model = ""
                ),
                errors = it.errors - VehicleField.BRAND - VehicleField.LINE - VehicleField.PLATE,
                saveFailed = false
            )
        }
        viewModelScope.launch {
            val brands = catalogHelper.getBrands(typeKey)
            _uiState.update {
                it.copy(availableBrands = brands)
            }
        }
    }

    fun onPlateChanged(rawPlate: String) {
        val uppercasePlate = rawPlate.uppercase().take(6)
        _uiState.update {
            it.copy(
                draft = it.draft.copy(plate = uppercasePlate),
                errors = it.errors - VehicleField.PLATE,
                saveFailed = false
            )
        }
    }

    fun onBrandSelected(brand: String) {
        val currentTypeKey = _uiState.value.typeKey
        _uiState.update {
            it.copy(
                draft = it.draft.copy(
                    brand = brand,
                    line = "",
                    model = ""
                ),
                errors = it.errors - VehicleField.BRAND - VehicleField.LINE,
                saveFailed = false
            )
        }
        viewModelScope.launch {
            val lines = catalogHelper.getLines(currentTypeKey, brand)
            _uiState.update {
                it.copy(availableLines = lines)
            }
        }
    }

    fun onLineSelected(line: String) {
        _uiState.update {
            it.copy(
                draft = it.draft.copy(line = line, model = line),
                errors = it.errors - VehicleField.LINE - VehicleField.MODEL,
                saveFailed = false
            )
        }
    }

    fun onYearSelected(year: String) {
        _uiState.update {
            it.copy(
                draft = it.draft.copy(year = year),
                errors = it.errors - VehicleField.YEAR,
                saveFailed = false
            )
        }
    }

    fun onColorSelected(colorName: String) {
        _uiState.update {
            it.copy(
                draft = it.draft.copy(color = colorName),
                saveFailed = false
            )
        }
    }

    fun onMileageChanged(mileage: String) {
        _uiState.update {
            it.copy(
                draft = it.draft.copy(mileage = mileage),
                errors = it.errors - VehicleField.MILEAGE,
                saveFailed = false
            )
        }
    }

    fun onVinChanged(vin: String) {
        val formattedVin = vin.uppercase().take(17)
        _uiState.update {
            it.copy(
                draft = it.draft.copy(vin = formattedVin),
                errors = it.errors - VehicleField.VIN,
                saveFailed = false
            )
        }
    }

    fun onEngineCcChanged(cc: String) {
        _uiState.update {
            it.copy(
                draft = it.draft.copy(engineCc = cc),
                errors = it.errors - VehicleField.ENGINE_CC,
                saveFailed = false
            )
        }
    }

    fun onFuelTypeSelected(fuelType: FuelType?) {
        _uiState.update {
            it.copy(
                draft = it.draft.copy(fuelType = fuelType),
                saveFailed = false
            )
        }
    }

    fun clearFieldError(field: VehicleField) {
        _uiState.update { it.copy(errors = it.errors - field) }
    }

    fun updateDraft(transform: (VehicleDraft) -> VehicleDraft) {
        _uiState.update { it.copy(draft = transform(it.draft), saveFailed = false) }
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
