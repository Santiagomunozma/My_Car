package com.micarro.feature.alerts.domain

import com.micarro.core.worker.AlertScheduler as CoreAlertScheduler
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.repository.AlertSettingsRepository
import com.micarro.domain.rules.MaintenanceTiming

/**
 * Programa alertas locales con un trabajo único por actividad.
 * Posponer (RN-08) reemplaza el aviso y no modifica el vencimiento del plan.
 */
class AlertScheduler constructor(
    private val coreAlertScheduler: CoreAlertScheduler,
    private val settingsRepository: AlertSettingsRepository
) {

    fun scheduleForActivity(
        activity: MaintenancePlan,
        vehicleLabel: String = activity.vehicleId,
        cause: String = "programada"
    ) {
        val settings = settingsRepository.current()
        val workName = CoreAlertScheduler.workName(activity.id)
        if (!settings.maintenanceAlertsEnabled || !activity.isActive || !activity.alertsEnabled) {
            coreAlertScheduler.cancelAlert(workName)
            return
        }

        val dueAt = activity.nextDeadlineDate
        val now = System.currentTimeMillis()
        val marginDays = settings.maintenanceMarginDays
        val notifyImmediately = dueAt == null && (
            cause.contains("vencida", ignoreCase = true) ||
                cause.contains("próxima", ignoreCase = true) ||
                cause.contains("proxima", ignoreCase = true)
            )
        val delayMinutes = MaintenanceTiming.alertDelayMinutes(
            dueAt = dueAt,
            marginDays = marginDays,
            now = now,
            intervalMonths = activity.intervalMonths,
            notifyImmediately = notifyImmediately
        )
        if (delayMinutes == null) {
            coreAlertScheduler.cancelAlert(workName)
            return
        }
        val resolvedCause = if (dueAt != null && dueAt <= now) "vencida" else cause
        coreAlertScheduler.scheduleAlert(
            uniqueWorkName = workName,
            delayInMinutes = delayMinutes,
            title = "Mantenimiento: ${activity.title}",
            message = "Vehículo $vehicleLabel. Actividad: ${activity.title} (${activity.category}). Causa: $resolvedCause.",
            vehicleLabel = vehicleLabel,
            activityTitle = activity.title,
            cause = resolvedCause
        )
    }

    fun cancelForActivity(activityId: String) {
        coreAlertScheduler.cancelAlert(CoreAlertScheduler.workName(activityId))
    }

    fun postpone(
        alertId: String,
        newDate: Long,
        activityTitle: String = "mantenimiento",
        vehicleLabel: String = ""
    ) {
        val now = System.currentTimeMillis()
        val delayMinutes = ((newDate - now) / 60_000L).coerceAtLeast(0L)
        coreAlertScheduler.scheduleAlert(
            uniqueWorkName = CoreAlertScheduler.workName(alertId),
            delayInMinutes = delayMinutes,
            title = "Recordatorio pospuesto",
            message = "Vehículo $vehicleLabel. Actividad: $activityTitle. Causa: aviso pospuesto. El vencimiento de la actividad no cambia.",
            vehicleLabel = vehicleLabel,
            activityTitle = activityTitle,
            cause = "pospuesta"
        )
    }

    fun scheduleMaintenanceAlert(delayInMinutes: Long, title: String, message: String) {
        coreAlertScheduler.scheduleAlert(
            uniqueWorkName = CoreAlertScheduler.workName(title),
            delayInMinutes = delayInMinutes,
            title = title,
            message = message
        )
    }

    fun snoozeAlert(snoozeDurationInMinutes: Long, title: String, message: String) {
        coreAlertScheduler.scheduleAlert(
            uniqueWorkName = CoreAlertScheduler.workName("snooze_$title"),
            delayInMinutes = snoozeDurationInMinutes,
            title = "Recordatorio: $title",
            message = message,
            activityTitle = title,
            cause = "pospuesta"
        )
    }
}
