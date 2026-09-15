package com.micarro.app

import com.micarro.app.domain.model.FuelType
import com.micarro.app.domain.model.Vehicle
import com.micarro.app.domain.model.VehicleType
import com.micarro.app.feature.vehicle.domain.SaveVehicleResult
import com.micarro.app.feature.vehicle.domain.SaveVehicleUseCase
import com.micarro.app.feature.vehicle.domain.SetPrimaryVehicleUseCase
import com.micarro.app.feature.vehicle.domain.ValidateVehicleUseCase
import com.micarro.app.feature.vehicle.domain.VehicleError
import com.micarro.app.feature.vehicle.domain.VehicleField
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Year

class VehicleUseCasesTest {

    private val validate = ValidateVehicleUseCase()

    private fun validVehicle() = Vehicle(
        plate = "ABC123",
        type = VehicleType.CAR,
        brand = "Mazda",
        line = "3",
        model = "Touring",
        year = 2020,
        currentMileage = 10_000L,
        fuelType = FuelType.GASOLINE
    )

    @Test
    fun `valid vehicle passes validation`() {
        assertTrue(validate(validVehicle()).isEmpty())
    }

    @Test
    fun `blank required fields produce REQUIRED errors`() {
        val errors = validate(validVehicle().copy(plate = "", brand = " ", line = "", model = ""))
        val fields = errors.filter { it.error == VehicleError.REQUIRED }.map { it.field }
        assertTrue(VehicleField.PLATE in fields)
        assertTrue(VehicleField.BRAND in fields)
        assertTrue(VehicleField.LINE in fields)
        assertTrue(VehicleField.MODEL in fields)
    }

    @Test
    fun `unreasonable year is rejected`() {
        val currentYear = Year.now().value
        assertTrue(
            validate(validVehicle().copy(year = 1800))
                .any { it.field == VehicleField.YEAR && it.error == VehicleError.INVALID_VALUE }
        )
        assertTrue(
            validate(validVehicle().copy(year = currentYear + 5))
                .any { it.field == VehicleField.YEAR }
        )
        assertTrue(validate(validVehicle().copy(year = currentYear + 1)).isEmpty())
    }

    @Test
    fun `negative mileage is rejected`() {
        assertTrue(
            validate(validVehicle().copy(currentMileage = -1))
                .any { it.field == VehicleField.MILEAGE && it.error == VehicleError.INVALID_VALUE }
        )
    }

    @Test
    fun `vin with invalid length is rejected, blank vin is allowed`() {
        assertTrue(
            validate(validVehicle().copy(vin = "ABC"))
                .any { it.field == VehicleField.VIN }
        )
        assertTrue(validate(validVehicle().copy(vin = "1HGBH41JXMN109186")).isEmpty())
        assertTrue(validate(validVehicle().copy(vin = null)).isEmpty())
    }

    @Test
    fun `save rejects duplicate plate`() = runBlocking {
        val repo = FakeVehicleRepository()
        val save = SaveVehicleUseCase(repo, validate)
        save(validVehicle())
        val result = save(validVehicle().copy(brand = "Renault"))
        assertTrue(result is SaveVehicleResult.Invalid)
        assertTrue(
            (result as SaveVehicleResult.Invalid).errors
                .any { it.field == VehicleField.PLATE && it.error == VehicleError.PLATE_TAKEN }
        )
    }

    @Test
    fun `save allows same plate on the vehicle being edited`() = runBlocking {
        val repo = FakeVehicleRepository()
        val save = SaveVehicleUseCase(repo, validate)
        val id = (save(validVehicle()) as SaveVehicleResult.Success).vehicleId
        val result = save(validVehicle().copy(id = id, brand = "Renault"))
        assertTrue(result is SaveVehicleResult.Success)
        assertEquals("Renault", repo.getVehicleById(id)?.brand)
    }

    @Test
    fun `save normalizes plate to uppercase`() = runBlocking {
        val repo = FakeVehicleRepository()
        val save = SaveVehicleUseCase(repo, validate)
        val result = save(validVehicle().copy(plate = "  xyz789 "))
        assertTrue(result is SaveVehicleResult.Success)
        assertEquals("XYZ789", repo.vehicles.value.single().plate)
    }

    @Test
    fun `set primary leaves only one primary vehicle`() = runBlocking {
        val repo = FakeVehicleRepository()
        val save = SaveVehicleUseCase(repo, validate)
        save(validVehicle().copy(plate = "AAA111", isPrimary = true))
        save(validVehicle().copy(plate = "BBB222", isPrimary = false))
        val setPrimary = SetPrimaryVehicleUseCase(repo)

        setPrimary(2L)

        val primaries = repo.vehicles.value.filter { it.isPrimary }
        assertEquals(1, primaries.size)
        assertEquals(2L, primaries.single().id)
    }
}
