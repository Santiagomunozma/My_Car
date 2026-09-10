package com.example.my_car.domain.model

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
    val isMainVehicle: Boolean = false
    // Los campos opcionales (color, vin, etc.) los podemos agregar después.
)