package com.example.my_car.feature.maintenance.presentation

import com.example.my_car.domain.model.MaintenancePlan
import com.example.my_car.domain.model.Vehicle
import com.example.my_car.feature.maintenance.domain.MaintenanceStatus

data class MaintenanceUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val selectedVehicle: Vehicle? = null,
    val plans: List<MaintenancePlanWithStatus> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val alertMarginDays: Int = 15,
    val alertMarginKm: Int = 500
)

data class MaintenancePlanWithStatus(
    val plan: MaintenancePlan,
    val status: MaintenanceStatus,
    val lastServiceDate: Long? = null,
    val lastServiceMileage: Int? = null,
    val nextDeadlineDate: Long? = null,
    val nextLimitMileage: Int? = null
)
