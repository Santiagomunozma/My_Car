package com.micarro.app.feature.mileage.domain

import com.micarro.app.domain.model.MileageReading
import com.micarro.app.domain.repository.MileageRepository
import com.micarro.app.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed interface AddReadingResult {
    data class Saved(val readingId: Long) : AddReadingResult
    data class NeedsConfirmation(val referenceMileage: Long) : AddReadingResult
    data object NegativeValue : AddReadingResult
    data object VehicleNotFound : AddReadingResult
}

class ObserveMileageUseCase @Inject constructor(
    private val repository: MileageRepository
) {
    operator fun invoke(vehicleId: Long): Flow<List<MileageReading>> =
        repository.observeMileage(vehicleId)
}

class AddMileageReadingUseCase @Inject constructor(
    private val mileageRepository: MileageRepository,
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(
        vehicleId: Long,
        mileage: Long,
        dateEpochMs: Long,
        confirmed: Boolean = false
    ): AddReadingResult {
        if (mileage < 0) return AddReadingResult.NegativeValue

        val vehicle = vehicleRepository.getVehicleById(vehicleId)
            ?: return AddReadingResult.VehicleNotFound

        val reference = maxOf(
            vehicle.currentMileage,
            mileageRepository.getLatestMileage(vehicleId)?.mileage ?: 0L
        )
        if (mileage < reference && !confirmed) {
            return AddReadingResult.NeedsConfirmation(reference)
        }

        val readingId = mileageRepository.addMileage(
            MileageReading(vehicleId = vehicleId, mileage = mileage, dateEpochMs = dateEpochMs)
        )
        vehicleRepository.updateCurrentMileage(vehicleId, mileage)
        return AddReadingResult.Saved(readingId)
    }
}
