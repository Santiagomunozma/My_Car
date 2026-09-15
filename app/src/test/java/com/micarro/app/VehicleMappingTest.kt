package com.micarro.app

import com.micarro.app.feature.vehicle.data.VehicleEntity
import com.micarro.app.domain.model.FuelType
import com.micarro.app.domain.model.Vehicle
import com.micarro.app.domain.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleMappingTest {

    private val sampleVehicle = Vehicle(
        id = 7L,
        plate = "ABC123",
        type = VehicleType.CAR,
        brand = "Mazda",
        line = "3",
        model = "Touring",
        year = 2020,
        currentMileage = 45_000L,
        color = "Rojo",
        vin = "VIN123",
        fuelType = FuelType.GASOLINE,
        engineCc = 2000,
        photoUri = null,
        isPrimary = true,
        isArchived = false
    )

    @Test
    fun `fromDomain normalizes plate to uppercase and trims`() {
        val entity = VehicleEntity.fromDomain(sampleVehicle.copy(plate = "  abc123 "))
        assertEquals("ABC123", entity.plate)
    }

    @Test
    fun `entity to domain roundtrip preserves all fields`() {
        val result = VehicleEntity.fromDomain(sampleVehicle).toDomain()
        assertEquals(sampleVehicle.copy(plate = "ABC123"), result)
    }

    @Test
    fun `unknown vehicle type in entity falls back to CAR`() {
        val entity = VehicleEntity.fromDomain(sampleVehicle).copy(type = "NOT_A_TYPE")
        assertEquals(VehicleType.CAR, entity.toDomain().type)
    }

    @Test
    fun `null fuel type maps to null in domain`() {
        val entity = VehicleEntity.fromDomain(sampleVehicle.copy(fuelType = null))
        assertNull(entity.toDomain().fuelType)
    }

    @Test
    fun `unknown fuel type in entity maps to null`() {
        val entity = VehicleEntity.fromDomain(sampleVehicle).copy(fuelType = "PLASMA")
        assertNull(entity.toDomain().fuelType)
    }
}
