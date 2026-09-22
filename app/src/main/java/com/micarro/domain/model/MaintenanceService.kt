package com.micarro.domain.model

import java.util.UUID

data class MaintenanceService(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val planId: String? = null,
    val title: String,
    val category: String,
    val date: Long,
    val mileage: Int,
    val totalCost: Double,
    val workshopName: String
)
