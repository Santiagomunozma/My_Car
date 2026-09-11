package com.example.my_car.domain.model

data class DashboardSummary(
    val mainVehicle: Vehicle? = null,
    val activeAlertsCount: Int = 0,
    val upcomingMaintenances: List<String> = emptyList(),
    val recentExpensesTotal: Double = 0.0
)