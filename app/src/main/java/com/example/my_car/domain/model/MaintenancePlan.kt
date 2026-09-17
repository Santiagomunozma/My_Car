package com.example.my_car.domain.model

import java.util.UUID

data class MaintenancePlan(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val title: String,
    val category: String,
    val intervalMileage: Int,
    val intervalMonths: Int
)
