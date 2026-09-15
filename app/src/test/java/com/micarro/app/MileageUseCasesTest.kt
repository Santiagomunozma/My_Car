package com.micarro.app

import com.micarro.app.domain.model.Vehicle
import com.micarro.app.domain.model.VehicleType
import com.micarro.app.feature.mileage.domain.AddMileageReadingUseCase
import com.micarro.app.feature.mileage.domain.AddReadingResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MileageUseCasesTest {

    private fun vehicleWith(mileage: Long) = Vehicle(
        id = 1L,
        plate = "ABC123",
        type = VehicleType.CAR,
        brand = "Mazda",
        line = "3",
        model = "Touring",
        year = 2020,
        currentMileage = mileage
    )

    private fun setup(vehicleMileage: Long): Triple<FakeVehicleRepository, FakeMileageRepository, AddMileageReadingUseCase> {
        val vehicleRepo = FakeVehicleRepository()
        val mileageRepo = FakeMileageRepository()
        val useCase = AddMileageReadingUseCase(mileageRepo, vehicleRepo)
        runBlocking {
            vehicleRepo.createVehicle(vehicleWith(vehicleMileage))
        }
        return Triple(vehicleRepo, mileageRepo, useCase)
    }

    @Test
    fun `negative reading is rejected`() = runBlocking {
        val (_, mileageRepo, useCase) = setup(50_000L)
        val result = useCase(vehicleId = 1L, mileage = -10L, dateEpochMs = 1000L)
        assertEquals(AddReadingResult.NegativeValue, result)
        assertTrue(mileageRepo.readings.value.isEmpty())
    }

    @Test
    fun `reading on unknown vehicle is rejected`() = runBlocking {
        val (_, _, useCase) = setup(50_000L)
        val result = useCase(vehicleId = 99L, mileage = 60_000L, dateEpochMs = 1000L)
        assertEquals(AddReadingResult.VehicleNotFound, result)
    }

    @Test
    fun `higher reading saves and updates current mileage`() = runBlocking {
        val (vehicleRepo, mileageRepo, useCase) = setup(50_000L)
        val result = useCase(vehicleId = 1L, mileage = 52_500L, dateEpochMs = 1000L)
        assertTrue(result is AddReadingResult.Saved)
        assertEquals(52_500L, vehicleRepo.getVehicleById(1L)?.currentMileage)
        assertEquals(1, mileageRepo.readings.value.size)
    }

    @Test
    fun `lower reading requires confirmation`() = runBlocking {
        val (_, mileageRepo, useCase) = setup(50_000L)
        val result = useCase(vehicleId = 1L, mileage = 40_000L, dateEpochMs = 1000L)
        assertEquals(AddReadingResult.NeedsConfirmation(50_000L), result)
        assertTrue(mileageRepo.readings.value.isEmpty())
    }

    @Test
    fun `confirmed lower reading saves and updates current mileage`() = runBlocking {
        val (vehicleRepo, mileageRepo, useCase) = setup(50_000L)
        val result = useCase(vehicleId = 1L, mileage = 40_000L, dateEpochMs = 1000L, confirmed = true)
        assertTrue(result is AddReadingResult.Saved)
        assertEquals(40_000L, vehicleRepo.getVehicleById(1L)?.currentMileage)
        assertEquals(1, mileageRepo.readings.value.size)
    }

    @Test
    fun `lower reading compares against latest reading not only vehicle mileage`() = runBlocking {
        val (_, _, useCase) = setup(50_000L)
        useCase(vehicleId = 1L, mileage = 60_000L, dateEpochMs = 1000L)
        val result = useCase(vehicleId = 1L, mileage = 55_000L, dateEpochMs = 2000L)
        assertTrue(result is AddReadingResult.NeedsConfirmation)
    }
}
