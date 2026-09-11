package com.example.my_car.domain.usecase

import com.example.my_car.domain.model.DashboardSummary
import com.example.my_car.domain.model.Vehicle
import com.example.my_car.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(): Flow<DashboardSummary> {
        return vehicleRepository.observeVehicles().map { vehicles ->
            val mainVehicle = vehicles.find { it.isMainVehicle } ?: vehicles.firstOrNull()

            DashboardSummary(
                mainVehicle = mainVehicle,
                activeAlertsCount = 0,
                upcomingMaintenances = if (mainVehicle != null) {
                    listOf("Próximo cambio de aceite a los ${mainVehicle.currentMileage + 5000} km")
                } else emptyList(),
                recentExpensesTotal = 0.0
            )
        }
    }
}