package com.micarro.feature.dashboard.presentation

import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Vehicle

data class DashboardUiState(
    val isLoading: Boolean = false,
    val selectedVehicle: Vehicle? = null,
    val allVehicles: List<Vehicle> = emptyList(),
    val totalActiveAlerts: Int = 0,
    val lastMileage: Long = 0L,
    val mileageDetails: String = "",
    val nextMaintenanceTitle: String? = null,
    val nextMaintenanceDetails: String = "",
    val currentMonthExpenses: Double = 0.0,
    val expensesDetails: String = "",
    val upcomingMaintenances: List<MaintenancePlan> = emptyList(),
    val recentServices: List<MaintenanceService> = emptyList()
)
