package com.micarro.domain.model

data class DashboardSummary(
    val selectedVehicle: Vehicle?,
    val activeAlertsCount: Int,
    val upcomingMaintenances: List<MaintenancePlan>,
    val recentServices: List<MaintenanceService>,
    val totalRecentExpenses: Double
)