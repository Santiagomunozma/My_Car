package com.micarro.domain.model

data class Vehicle(
    val id: Long = 0L,
    val plate: String,
    val type: VehicleType, // Enum: CAR, TRUCK, MOTORCYCLE
    val brand: String,
    val line: String,
    val year: Int,
    val currentMileage: Long,
    val isArchived: Boolean = false,
    val isMainVehicle: Boolean = false,
    val model: String = "",
    val color: String? = null,
    val vin: String? = null,
    val fuelType: FuelType? = null,
    val engineCc: Int? = null,
    val photoUri: String? = null
)
