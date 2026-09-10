package com.example.my_car.domain.model

import java.util.UUID

data class MileageRecord(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val date: Long, // Timestamp
    val reading: Int
)