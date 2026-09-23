package com.micarro.domain.alerts

/**
 * Contrato de integración con el módulo de alertas (Compañero 2).
 * Se invoca tras cada lectura de kilometraje válida para que ese módulo
 * recalcule sus alertas por odómetro. Hilt enlaza ahora la implementación
 * real [com.micarro.feature.alerts.domain.MileageAlertScheduler].
 */
interface MileageAlertNotifier {
    suspend fun onMileageUpdated(vehicleId: String, odometer: Long)
}
