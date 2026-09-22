package com.micarro.domain.model

data class MaintenanceHistoryItem(
    val id: String,
    val vehiclePlate: String,
    val title: String,
    val category: String,
    val date: Long,
    val mileage: Int,
    val totalCost: Double,
    val workshopName: String
)