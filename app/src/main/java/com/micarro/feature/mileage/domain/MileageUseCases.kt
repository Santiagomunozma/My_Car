package com.micarro.feature.mileage.domain

import com.micarro.domain.alerts.MileageAlertNotifier
import com.micarro.domain.model.MileageRecord
import com.micarro.domain.repository.MileageRepository
import com.micarro.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

enum class MileageError {
    EMPTY, NOT_A_NUMBER, NEGATIVE, FUTURE_DATE
}

sealed interface AddReadingResult {
    data object Success : AddReadingResult
    data class RequiresConfirmation(val previousReading: Int) : AddReadingResult
    data class Invalid(val errors: Set<MileageError>) : AddReadingResult
    data class Failure(val cause: Throwable) : AddReadingResult
}

object MileageRules {
    fun millisToDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    fun dateToMillis(date: LocalDate): Long =
        date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    fun isFutureDate(dateMillis: Long, today: LocalDate = LocalDate.now()): Boolean =
        millisToDate(dateMillis).isAfter(today)
}

class ObserveMileageUseCase @Inject constructor(
    private val repository: MileageRepository
) {
    operator fun invoke(vehicleId: Long): Flow<List<MileageRecord>> =
        repository.observeMileage(vehicleId)
}

class AddMileageReadingUseCase @Inject constructor(
    private val mileageRepository: MileageRepository,
    private val vehicleRepository: VehicleRepository,
    private val alertNotifier: MileageAlertNotifier
) {
    suspend operator fun invoke(
        vehicleId: Long,
        odometerText: String,
        dateMillis: Long,
        note: String?,
        confirmedLowerReading: Boolean
    ): AddReadingResult {
        val errors = mutableSetOf<MileageError>()
        val odometer = odometerText.trim().toLongOrNull()
        when {
            odometerText.isBlank() -> errors += MileageError.EMPTY
            odometer == null -> errors += MileageError.NOT_A_NUMBER
            odometer < 0 -> errors += MileageError.NEGATIVE
            odometer > Int.MAX_VALUE -> errors += MileageError.NOT_A_NUMBER
        }
        if (MileageRules.isFutureDate(dateMillis)) errors += MileageError.FUTURE_DATE
        if (errors.isNotEmpty()) return AddReadingResult.Invalid(errors)

        return try {
            // Se usa el estado más reciente de los repositorios, no una copia del UiState.
            val vehicle = vehicleRepository.getVehicleById(vehicleId)
                ?: return AddReadingResult.Failure(IllegalStateException("vehicle_gone"))
            val latest = mileageRepository.getLatestMileage(vehicleId)
            val previousReference = maxOf(latest?.reading?.toLong() ?: 0L, vehicle.currentMileage)

            val reading = odometer!!.toInt()
            if (reading < previousReference && !confirmedLowerReading) {
                return AddReadingResult.RequiresConfirmation(
                    previousReference.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                )
            }

            mileageRepository.addMileage(
                MileageRecord(
                    id = System.currentTimeMillis(),
                    vehicleId = vehicleId,
                    date = dateMillis,
                    reading = reading,
                    note = note?.trim()?.ifEmpty { null }
                )
            )

            // El kilometraje actual se deriva de la última lectura cronológica.
            val newLatest = mileageRepository.getLatestMileage(vehicleId)
            if (newLatest != null && newLatest.reading.toLong() != vehicle.currentMileage) {
                vehicleRepository.updateCurrentMileage(vehicleId, newLatest.reading.toLong())
            }
            alertNotifier.onMileageUpdated(vehicleId.toString(), newLatest?.reading ?: reading)
            AddReadingResult.Success
        } catch (e: Exception) {
            AddReadingResult.Failure(e)
        }
    }
}
