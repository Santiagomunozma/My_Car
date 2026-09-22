package com.micarro.domain.alerts

import javax.inject.Inject

/**
 * Contrato de integración con el módulo de alertas (Compañero 2).
 * Se invoca tras cada lectura de kilometraje válida para que ese módulo
 * recalcule sus alertas por odómetro. La implementación real llega con el
 * módulo de alertas; mientras tanto Hilt enlaza [NoOpMileageAlertNotifier].
 */
interface MileageAlertNotifier {
    suspend fun onMileageUpdated(vehicleId: String, odometer: Long)
}

class NoOpMileageAlertNotifier @Inject constructor() : MileageAlertNotifier {
    override suspend fun onMileageUpdated(vehicleId: String, odometer: Long) = Unit
}
