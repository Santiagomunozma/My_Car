package com.example.my_car.feature.dashboard.presentation

data class DashboardUiState(
    val isLoading: Boolean = true,
    val mainVehicleName: String? = null,
    val upcomingMaintenances: List<String> = emptyList(),
    val recentExpensesTotal: Double = 0.0,
    val activeAlerts: Int = 0
)