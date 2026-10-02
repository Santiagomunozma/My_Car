package com.micarro.feature.alerts.domain

import com.micarro.domain.alerts.MileageAlertNotifier
import com.micarro.domain.repository.AlertSettingsRepository
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.feature.maintenance.domain.MaintenanceRules
import com.micarro.feature.maintenance.domain.MaintenanceStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recalcula las actividades del vehículo cuando cambia el odómetro (RF-09).
 * Solo reprograma las que quedan próximas o vencidas; no envía un aviso genérico.
 */
@Singleton
class MileageAlertScheduler @Inject constructor(
    private val alertScheduler: AlertScheduler,
    private val maintenanceRepository: MaintenanceRepository,
    private val settingsRepository: AlertSettingsRepository,
    private val vehicleRepository: VehicleRepository
) : MileageAlertNotifier {

    override suspend fun onMileageUpdated(vehicleId: String, odometer: Long) {
        val settings = settingsRepository.current()
        val plans = maintenanceRepository.observePlans(vehicleId).first()
        val vehicle = vehicleId.toLongOrNull()?.let { vehicleRepository.getVehicleById(it) }
        if (vehicle?.isArchived == true) {
            plans.forEach { alertScheduler.cancelForActivity(it.id) }
            return
        }
        val plate = vehicle?.plate ?: vehicleId
        val services = maintenanceRepository.observeServices(vehicleId).first()
        val now = System.currentTimeMillis()

        plans.forEach { plan ->
            if (!settings.maintenanceAlertsEnabled || !plan.isActive || !plan.alertsEnabled) {
                alertScheduler.cancelForActivity(plan.id)
                return@forEach
            }
            val last = services.filter { it.planId == plan.id }.maxByOrNull { it.date }
            val computed = last?.let {
                MaintenanceRules.calculateNextRecurrence(
                    it.date,
                    it.mileage,
                    plan.intervalMonths,
                    plan.intervalMileage
                )
            }
            val nextDate = plan.nextDeadlineDate ?: computed?.first
            val nextKm = plan.nextLimitMileage ?: computed?.second
            val status = MaintenanceRules.calculateStatus(
                deadlineDate = nextDate,
                limitMileage = nextKm,
                currentDate = now,
                currentMileage = odometer.toInt(),
                marginDays = settings.maintenanceMarginDays,
                marginKm = settings.maintenanceMarginKm
            )
            if (status == MaintenanceStatus.PROXIMA || status == MaintenanceStatus.VENCIDA) {
                val cause = if (status == MaintenanceStatus.VENCIDA) {
                    "vencida por kilometraje"
                } else {
                    "próxima por kilometraje"
                }
                alertScheduler.scheduleForActivity(
                    activity = plan.copy(nextDeadlineDate = nextDate, nextLimitMileage = nextKm),
                    vehicleLabel = plate,
                    cause = cause
                )
            } else if (nextDate != null) {
                alertScheduler.scheduleForActivity(
                    activity = plan.copy(nextDeadlineDate = nextDate, nextLimitMileage = nextKm),
                    vehicleLabel = plate,
                    cause = "al día"
                )
            }
        }
    }
}
