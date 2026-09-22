package com.micarro.feature.vehicle.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Year

class VehicleRulesTest {

    @Test
    fun `normalizePlate uppercase y sin espacios`() {
        assertEquals("ABC123", VehicleRules.normalizePlate(" abc 123 "))
        assertEquals("ABC-123", VehicleRules.normalizePlate("abc-123"))
    }

    @Test
    fun `placa valida entre 3 y 10 caracteres alfanumericos o guiones`() {
        assertTrue(VehicleRules.isPlateFormatValid("ABC123"))
        assertTrue(VehicleRules.isPlateFormatValid("AB-12"))
        assertFalse(VehicleRules.isPlateFormatValid("AB"))
        assertFalse(VehicleRules.isPlateFormatValid("ABCDEFGHIJK"))
        assertFalse(VehicleRules.isPlateFormatValid("ABC_123"))
        assertFalse(VehicleRules.isPlateFormatValid(""))
    }

    @Test
    fun `anio valido entre 1900 y anio actual mas uno`() {
        val current = Year.now().value
        assertTrue(VehicleRules.isYearValid(2020, current))
        assertTrue(VehicleRules.isYearValid(1900, current))
        assertTrue(VehicleRules.isYearValid(current + 1, current))
        assertFalse(VehicleRules.isYearValid(1899, current))
        assertFalse(VehicleRules.isYearValid(current + 2, current))
    }

    @Test
    fun `kilometraje no negativo`() {
        assertTrue(VehicleRules.isMileageValid(0))
        assertTrue(VehicleRules.isMileageValid(15000))
        assertFalse(VehicleRules.isMileageValid(-1))
    }

    @Test
    fun `vin debe tener 17 caracteres alfanumericos`() {
        assertTrue(VehicleRules.isVinValid("1HGCM82633A004352"))
        assertTrue(VehicleRules.isVinValid("1hgcm82633a004352"))
        assertFalse(VehicleRules.isVinValid("1HGCM82633A00435"))
        assertFalse(VehicleRules.isVinValid("1HGCM82633A004352!"))
        assertFalse(VehicleRules.isVinValid(""))
    }

    @Test
    fun `cilindraje mayor que cero`() {
        assertTrue(VehicleRules.isEngineCcValid(1600))
        assertFalse(VehicleRules.isEngineCcValid(0))
        assertFalse(VehicleRules.isEngineCcValid(-200))
    }
}
