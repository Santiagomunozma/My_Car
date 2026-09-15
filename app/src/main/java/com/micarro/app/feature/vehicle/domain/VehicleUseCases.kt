package com.micarro.app.feature.vehicle.domain

import com.micarro.app.domain.model.Vehicle
import com.micarro.app.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import java.time.Year
import javax.inject.Inject

enum class VehicleField { PLATE, BRAND, LINE, MODEL, YEAR, MILEAGE, VIN }
enum class VehicleError { REQUIRED, INVALID_VALUE, PLATE_TAKEN }

data class VehicleFieldError(val field: VehicleField, val error: VehicleError)

sealed interface SaveVehicleResult {
    data class Success(val vehicleId: Long) : SaveVehicleResult
    data class Invalid(val errors: List<VehicleFieldError>) : SaveVehicleResult
}

class ValidateVehicleUseCase @Inject constructor() {

    operator fun invoke(vehicle: Vehicle): List<VehicleFieldError> {
        val errors = mutableListOf<VehicleFieldError>()
        val currentYear = Year.now().value

        if (vehicle.plate.isBlank()) {
            errors += VehicleFieldError(VehicleField.PLATE, VehicleError.REQUIRED)
        }
        if (vehicle.brand.isBlank()) {
            errors += VehicleFieldError(VehicleField.BRAND, VehicleError.REQUIRED)
        }
        if (vehicle.line.isBlank()) {
            errors += VehicleFieldError(VehicleField.LINE, VehicleError.REQUIRED)
        }
        if (vehicle.model.isBlank()) {
            errors += VehicleFieldError(VehicleField.MODEL, VehicleError.REQUIRED)
        }
        if (vehicle.year !in MIN_YEAR..currentYear + 1) {
            errors += VehicleFieldError(VehicleField.YEAR, VehicleError.INVALID_VALUE)
        }
        if (vehicle.currentMileage < 0) {
            errors += VehicleFieldError(VehicleField.MILEAGE, VehicleError.INVALID_VALUE)
        }
        vehicle.vin?.takeIf { it.isNotBlank() }?.let { vin ->
            if (vin.trim().length !in VIN_MIN_LENGTH..VIN_MAX_LENGTH) {
                errors += VehicleFieldError(VehicleField.VIN, VehicleError.INVALID_VALUE)
            }
        }
        return errors
    }

    companion object {
        const val MIN_YEAR = 1950
        const val VIN_MIN_LENGTH = 8
        const val VIN_MAX_LENGTH = 17
    }
}

class ObserveVehiclesUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    operator fun invoke(includeArchived: Boolean = false): Flow<List<Vehicle>> =
        if (includeArchived) repository.observeAllVehicles() else repository.observeVehicles()
}

class GetVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(id: Long): Vehicle? = repository.getVehicleById(id)
}

class SaveVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository,
    private val validate: ValidateVehicleUseCase
) {
    suspend operator fun invoke(vehicle: Vehicle): SaveVehicleResult {
        val normalized = vehicle.copy(
            plate = vehicle.plate.uppercase().trim(),
            brand = vehicle.brand.trim(),
            line = vehicle.line.trim(),
            model = vehicle.model.trim(),
            vin = vehicle.vin?.trim()?.uppercase()?.ifBlank { null }
        )
        val errors = validate(normalized).toMutableList()
        if (!repository.isPlateAvailable(normalized.plate, normalized.id.takeIf { it != 0L })) {
            errors += VehicleFieldError(VehicleField.PLATE, VehicleError.PLATE_TAKEN)
        }
        if (errors.isNotEmpty()) {
            return SaveVehicleResult.Invalid(errors)
        }
        val id = if (normalized.id == 0L) {
            repository.createVehicle(normalized)
        } else {
            repository.updateVehicle(normalized)
            normalized.id
        }
        return SaveVehicleResult.Success(id)
    }
}

class SetPrimaryVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(vehicleId: Long) {
        repository.getVehicleById(vehicleId)?.let { vehicle ->
            repository.updateVehicle(vehicle.copy(isPrimary = true))
        }
    }
}

class ArchiveVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(vehicleId: Long) = repository.archiveVehicle(vehicleId)
}

class ReactivateVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(vehicleId: Long) = repository.reactivateVehicle(vehicleId)
}
