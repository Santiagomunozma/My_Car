package com.micarro.domain.model

import java.util.UUID

enum class ServiceType {
    PREVENTIVE,
    CORRECTIVE
}

data class MaintenanceService(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val planId: String? = null,
    val title: String,
    val category: String,
    val date: Long,
    val mileage: Int,
    val totalCost: Double,
    val workshopName: String,
    val serviceType: ServiceType = ServiceType.CORRECTIVE,
    val laborCost: Double = 0.0,
    val otherCosts: Double = 0.0,
    val evidenceUri: String? = null,
    val description: String = ""
)
