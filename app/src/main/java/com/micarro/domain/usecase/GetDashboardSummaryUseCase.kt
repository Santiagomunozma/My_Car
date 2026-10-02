package com.micarro.domain.usecase

import com.micarro.domain.model.DashboardSummary
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.AlertSettingsRepository
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.domain.rules.MaintenanceTiming
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val alertSettingsRepository: AlertSettingsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(selectedVehicleIdFlow: Flow<String?>): Flow<DashboardSummary> {
        return selectedVehicleIdFlow.flatMapLatest { vehicleId ->
            vehicleRepository.observeVehicles().flatMapLatest { vehicles ->
                val targetVehicle = resolveVehicle(vehicles, vehicleId)
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

    private fun resolveVehicle(vehicles: List<Vehicle>, vehicleId: String?): Vehicle? {
        val active = vehicles.filter { !it.isArchived }
        return if (!vehicleId.isNullOrBlank()) {
            active.find { it.id.toString() == vehicleId }
        } else {
            active.firstOrNull { it.isPrimary } ?: active.firstOrNull()
        }
    }

    private fun buildSummaryForVehicle(vehicle: Vehicle): Flow<DashboardSummary> {
        return combine(
            maintenanceRepository.observePlans(vehicle.id.toString()),
            maintenanceRepository.observeServices(vehicle.id.toString()),
            alertSettingsRepository.observeSettings()
        ) { plans, services, settings ->
            val now = System.currentTimeMillis()
            val activePlans = plans.filter { it.isActive }
            val upcoming = activePlans.sortedBy { it.nextDeadlineDate ?: Long.MAX_VALUE }.take(3)
            val recent = services.sortedByDescending { it.date }.take(5)
            val alerts = activePlans.count { plan ->
                isMaintenanceAlert(
                    plan = plan,
                    mileage = vehicle.currentMileage,
                    now = now,
                    marginDays = settings.maintenanceMarginDays,
                    marginKm = settings.maintenanceMarginKm
                )
            }
            DashboardSummary(
                selectedVehicle = vehicle,
                activeAlertsCount = alerts,
                upcomingMaintenances = upcoming,
                recentServices = recent,
                totalRecentExpenses = recent.sumOf { it.totalCost }
            )
        }
    }

    private fun isMaintenanceAlert(
        plan: MaintenancePlan,
        mileage: Long,
        now: Long,
        marginDays: Int,
        marginKm: Int
    ): Boolean {
        if (!plan.alertsEnabled) return false
        val due = plan.nextDeadlineDate
        val limit = plan.nextLimitMileage
        val currentMileage = mileage.toInt()
        return MaintenanceTiming.isOverdue(due, limit, now, currentMileage) ||
            MaintenanceTiming.isUpcoming(due, limit, now, currentMileage, marginDays, marginKm)
    }
}
