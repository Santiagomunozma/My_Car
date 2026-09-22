package com.micarro.domain.model

import java.util.UUID

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    val plate: String,
    val type: VehicleType, // Enum: CAR, TRUCK, MOTORCYCLE
    val brand: String,
    val line: String,
    val year: Int,
    val currentMileage: Int,
    val isArchived: Boolean = false,
    val isMainVehicle: Boolean = false,
    val model: String = "",
    val color: String? = null,
    val vin: String? = null,
    val fuelType: FuelType? = null,
    val engineCc: Int? = null,
    val photoUri: String? = null
)
