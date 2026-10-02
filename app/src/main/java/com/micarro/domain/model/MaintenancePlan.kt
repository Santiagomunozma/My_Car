package com.micarro.domain.model

import java.util.UUID

data class MaintenancePlan(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val title: String,
    val category: String,
    val intervalMileage: Int,
    val intervalMonths: Int,
    val isActive: Boolean = true,
    val description: String = "",
    val nextDeadlineDate: Long? = null,
    val nextLimitMileage: Int? = null,
    val marginDays: Int = 15,
    val marginKm: Int = 500,
    val alertsEnabled: Boolean = true
)
