package com.micarro.feature.vehicle.domain

import java.time.Year

enum class VehicleField {
    PLATE, BRAND, LINE, MODEL, YEAR, MILEAGE, VIN, ENGINE_CC
}

enum class VehicleError {
    REQUIRED, INVALID_FORMAT, INVALID_VALUE, DUPLICATE_PLATE
}

object VehicleRules {
    private val PLATE_PATTERN = Regex("^[A-Z0-9-]{3,10}$")
    private val VIN_PATTERN = Regex("^[A-Z0-9]{17}$")
    const val MIN_YEAR = 1900

    fun normalizePlate(raw: String): String =
        raw.trim().uppercase().replace(Regex("\\s+"), "")

    fun isPlateFormatValid(normalizedPlate: String): Boolean =
        PLATE_PATTERN.matches(normalizedPlate)

    fun isYearValid(year: Int, currentYear: Int = Year.now().value): Boolean =
        year in MIN_YEAR..(currentYear + 1)

    fun isMileageValid(mileage: Long): Boolean = mileage >= 0

    fun isVinValid(vin: String): Boolean = VIN_PATTERN.matches(vin.trim().uppercase())

    fun isEngineCcValid(engineCc: Int): Boolean = engineCc > 0
}
