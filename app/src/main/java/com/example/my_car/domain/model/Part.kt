package com.example.my_car.domain.model

import java.util.UUID

data class Part(
    val id: String = UUID.randomUUID().toString(),
    val serviceId: String,
    val name: String,
    val quantity: Int,
    val cost: Double
)
