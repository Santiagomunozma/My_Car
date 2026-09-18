package com.example.my_car.feature.vehicle.domain

import com.example.my_car.domain.model.FuelType
import com.example.my_car.domain.model.Vehicle
import com.example.my_car.domain.model.VehicleType
import com.example.my_car.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

data class VehicleDraft(
    val id: String? = null,
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
    val photoUri: String? = null
)

sealed interface SaveVehicleResult {
    data object Success : SaveVehicleResult
    data class Invalid(val errors: Map<VehicleField, VehicleError>) : SaveVehicleResult
    data class Failure(val cause: Throwable) : SaveVehicleResult
}

class ObserveVehiclesUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    operator fun invoke(showArchived: Boolean): Flow<List<Vehicle>> =
        if (showArchived) repository.observeAllVehicles() else repository.observeVehicles()
}

class GetVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(id: String): Vehicle? = repository.getVehicleById(id)
}

class SaveVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository,
    private val photoStore: VehiclePhotoStore
) {
    suspend operator fun invoke(draft: VehicleDraft): SaveVehicleResult {
        val errors = validate(draft)
        if (errors.isNotEmpty()) return SaveVehicleResult.Invalid(errors)

        val normalizedPlate = VehicleRules.normalizePlate(draft.plate)
        return try {
            if (!repository.isPlateAvailable(normalizedPlate, excludingId = draft.id)) {
                return SaveVehicleResult.Invalid(
                    mapOf(VehicleField.PLATE to VehicleError.DUPLICATE_PLATE)
                )
            }

            if (draft.id == null) {
                repository.createVehicle(
                    Vehicle(
                        id = UUID.randomUUID().toString(),
                        plate = normalizedPlate,
                        type = draft.type,
                        brand = draft.brand.trim(),
                        line = draft.line.trim(),
                        model = draft.model.trim(),
                        year = draft.year.trim().toInt(),
                        currentMileage = draft.mileage.trim().toInt(),
                        color = draft.color.trim().ifEmpty { null },
                        vin = draft.vin.trim().uppercase().ifEmpty { null },
                        fuelType = draft.fuelType,
                        engineCc = draft.engineCc.trim().toIntOrNull(),
                        photoUri = draft.photoUri
                    )
                )
            } else {
                // Edición segura: se recarga el estado actual del repositorio y se
                // conservan currentMileage, isArchived e isMainVehicle.
                val current = repository.getVehicleById(draft.id)
                    ?: return SaveVehicleResult.Failure(IllegalStateException("vehicle_gone"))
                if (current.photoUri != draft.photoUri && current.photoUri != null) {
                    photoStore.deletePhoto(current.photoUri)
                }
                repository.updateVehicle(
                    current.copy(
                        plate = normalizedPlate,
                        type = draft.type,
                        brand = draft.brand.trim(),
                        line = draft.line.trim(),
                        model = draft.model.trim(),
                        year = draft.year.trim().toInt(),
                        color = draft.color.trim().ifEmpty { null },
                        vin = draft.vin.trim().uppercase().ifEmpty { null },
                        fuelType = draft.fuelType,
                        engineCc = draft.engineCc.trim().toIntOrNull(),
                        photoUri = draft.photoUri
                    )
                )
            }
            SaveVehicleResult.Success
        } catch (e: Exception) {
            SaveVehicleResult.Failure(e)
        }
    }

    private fun validate(draft: VehicleDraft): Map<VehicleField, VehicleError> {
        val errors = mutableMapOf<VehicleField, VehicleError>()
        val normalizedPlate = VehicleRules.normalizePlate(draft.plate)
        when {
            normalizedPlate.isEmpty() -> errors[VehicleField.PLATE] = VehicleError.REQUIRED
            !VehicleRules.isPlateFormatValid(normalizedPlate) ->
                errors[VehicleField.PLATE] = VehicleError.INVALID_FORMAT
        }
        if (draft.brand.isBlank()) errors[VehicleField.BRAND] = VehicleError.REQUIRED
        if (draft.line.isBlank()) errors[VehicleField.LINE] = VehicleError.REQUIRED
        if (draft.model.isBlank()) errors[VehicleField.MODEL] = VehicleError.REQUIRED

        val year = draft.year.trim().toIntOrNull()
        when {
            draft.year.isBlank() -> errors[VehicleField.YEAR] = VehicleError.REQUIRED
            year == null -> errors[VehicleField.YEAR] = VehicleError.INVALID_FORMAT
            !VehicleRules.isYearValid(year) -> errors[VehicleField.YEAR] = VehicleError.INVALID_VALUE
        }

        // El kilometraje solo es editable en el alta; en edición se conserva el actual.
        if (draft.id == null) {
            val mileage = draft.mileage.trim().toIntOrNull()
            when {
                draft.mileage.isBlank() -> errors[VehicleField.MILEAGE] = VehicleError.REQUIRED
                mileage == null -> errors[VehicleField.MILEAGE] = VehicleError.INVALID_FORMAT
                !VehicleRules.isMileageValid(mileage) ->
                    errors[VehicleField.MILEAGE] = VehicleError.INVALID_VALUE
            }
        }

        if (draft.vin.isNotBlank() && !VehicleRules.isVinValid(draft.vin)) {
            errors[VehicleField.VIN] = VehicleError.INVALID_FORMAT
        }

        if (draft.engineCc.isNotBlank()) {
            val cc = draft.engineCc.trim().toIntOrNull()
            when {
                cc == null -> errors[VehicleField.ENGINE_CC] = VehicleError.INVALID_FORMAT
                !VehicleRules.isEngineCcValid(cc) ->
                    errors[VehicleField.ENGINE_CC] = VehicleError.INVALID_VALUE
            }
        }
        return errors
    }
}

class ArchiveVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(id: String) = repository.archiveVehicle(id)
}

class ReactivateVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(id: String) = repository.reactivateVehicle(id)
}

class SetMainVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(id: String) = repository.setMainVehicle(id)
}
