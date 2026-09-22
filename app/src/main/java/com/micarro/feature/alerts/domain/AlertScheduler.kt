package com.micarro.feature.alerts.domain

import com.micarro.core.worker.AlertScheduler as CoreAlertScheduler
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Programador de alertas locales para la capa de dominio.
 * Consume el core/worker/AlertScheduler provisto e integra las alertas de mantenimiento.
 * Implementa la regla RN-08 (posponer mueve el aviso, no la fecha de vencimiento).
 */
@Singleton
class AlertScheduler @Inject constructor(
    private val coreAlertScheduler: CoreAlertScheduler
) {

    /**
     * Programa una alerta de mantenimiento utilizando el programador base.
     */
    fun scheduleMaintenanceAlert(delayInMinutes: Long, title: String, message: String) {
        coreAlertScheduler.scheduleAlert(delayInMinutes, title, message)
    }

    /**
     * Implementa la regla RN-08: Posponer mueve el aviso, no la fecha de vencimiento.
     * Reprograma el aviso de la alerta para un tiempo determinado (en minutos),
     * manteniendo intacta la fecha de vencimiento y los datos del plan de mantenimiento.
     */
    fun snoozeAlert(snoozeDurationInMinutes: Long, title: String, message: String) {
        // Al posponer, se registra un nuevo aviso retrasado (en minutos) en el programador de alertas,
        // sin alterar la fecha de vencimiento real registrada en la base de datos de mantenimiento.
        coreAlertScheduler.scheduleAlert(snoozeDurationInMinutes, "Recordatorio: $title", message)
    }
}
