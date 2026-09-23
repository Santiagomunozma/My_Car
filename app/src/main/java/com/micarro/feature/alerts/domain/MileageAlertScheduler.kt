package com.micarro.feature.alerts.domain

import com.micarro.domain.alerts.MileageAlertNotifier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación real de [MileageAlertNotifier] que utiliza el programador de
 * alertas del módulo de alertas para notificar cuando el odómetro de un
 * vehículo se actualiza.
 *
 * Por ahora programa una notificación local informativa inmediata; el motor
 * completo de alertas por kilometraje (umbrales, repetición, reposición) lo
 * seguirá refinando el módulo de alertas del equipo.
 */
@Singleton
class MileageAlertScheduler @Inject constructor(
    private val alertScheduler: AlertScheduler
) : MileageAlertNotifier {

    override suspend fun onMileageUpdated(vehicleId: String, odometer: Long) {
        alertScheduler.scheduleMaintenanceAlert(
            delayInMinutes = 0,
            title = "Kilometraje actualizado",
            message = "Vehículo $vehicleId - Odómetro: $odometer km"
        )
    }
}
