package com.micarro.domain.usecase

import com.micarro.domain.model.DashboardSummary
import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.VehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(selectedVehicleIdFlow: Flow<String?>): Flow<DashboardSummary> {
        return selectedVehicleIdFlow.flatMapLatest { vehicleId ->
            vehicleRepository.observeVehicles().flatMapLatest { vehicles ->

                // Buscamos el vehículo por ID o tomamos el primero de la lista disponible
                val targetVehicle = if (!vehicleId.isNullOrBlank()) {
                    vehicles.find { it.id.toString() == vehicleId }
                } else {
                    vehicles.firstOrNull()
                }

                if (targetVehicle != null) {
                    buildSummaryForVehicle(targetVehicle)
                } else {
                    flowOf(
                        DashboardSummary(
                            selectedVehicle = null,
                            activeAlertsCount = 0,
                            upcomingMaintenances = emptyList(),
                            recentServices = emptyList(),
                            totalRecentExpenses = 0.0
                        )
                    )
                }
            }
        }
    }

    private fun buildSummaryForVehicle(vehicle: Vehicle): Flow<DashboardSummary> {
        return combine(
            maintenanceRepository.observePlans(vehicle.id.toString()),
            maintenanceRepository.observeServices(vehicle.id.toString())
        ) { plans, services ->

            // Tomamos los primeros planes programados y los servicios recientes
            val upcoming = plans.take(3)
            val recent = services.take(5)
            val recentTotalSpent = recent.sumOf { it.totalCost }
            val alertsCount = upcoming.size

            DashboardSummary(
                selectedVehicle = vehicle,
                activeAlertsCount = alertsCount,
                upcomingMaintenances = upcoming,
                recentServices = recent,
                totalRecentExpenses = recentTotalSpent
            )
        }
    }
}