package com.micarro.feature.alerts.domain

import com.micarro.core.worker.AlertScheduler as CoreAlertScheduler
import com.micarro.domain.model.MaintenancePlan
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Programador de alertas locales para la capa de dominio.
 * Consume el core/worker/AlertScheduler provisto e integra las alertas de mantenimiento.
 * Implementa los contratos comunes del equipo (scheduleForActivity, cancelForActivity, postpone)
 * y la regla RN-08 (posponer mueve el aviso, no la fecha de vencimiento).
 */
@Singleton
class AlertScheduler @Inject constructor(
    private val coreAlertScheduler: CoreAlertScheduler
) {

    /**
     * Contrato del equipo: programa una alerta para una actividad/plan de mantenimiento.
     */
    fun scheduleForActivity(activity: MaintenancePlan) {
        val delayMinutes = if (activity.intervalMonths > 0) {
            activity.intervalMonths.toLong() * 30L * 24L * 60L
        } else {
            0L
        }
        scheduleMaintenanceAlert(
            delayInMinutes = delayMinutes,
            title = "Mantenimiento: ${activity.title}",
            message = "Próximo mantenimiento programado (${activity.category})."
        )
    }

    /**
     * Contrato del equipo: cancela la alerta programada para una actividad.
     */
    fun cancelForActivity(activityId: String) {
        coreAlertScheduler.cancelAlert("activity_$activityId")
    }

    /**
     * Contrato del equipo: pospone una alerta para una nueva fecha (RN-08).
     * Mueve el aviso, manteniendo intacta la fecha de vencimiento real.
     */
    fun postpone(alertId: String, newDate: Long) {
        val now = System.currentTimeMillis()
        val delayMinutes = ((newDate - now) / (1000 * 60)).coerceAtLeast(0L)
        coreAlertScheduler.scheduleAlert(
            delayMinutes,
            "Recordatorio pospuesto",
            "Aviso de mantenimiento pospuesto"
        )
    }

    /**
     * Programa una alerta de mantenimiento utilizando el programador base.
     */
    fun scheduleMaintenanceAlert(delayInMinutes: Long, title: String, message: String) {
        coreAlertScheduler.scheduleAlert(delayInMinutes, title, message)
    }

    /**
     * Implementa la regla RN-08: Posponer mueve el aviso, no la fecha de vencimiento.
     * Reprograma el aviso de la alerta para un tiempo determinado (en minutos).
     */
    fun snoozeAlert(snoozeDurationInMinutes: Long, title: String, message: String) {
        coreAlertScheduler.scheduleAlert(snoozeDurationInMinutes, "Recordatorio: $title", message)
    }
}
