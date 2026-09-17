package com.example.my_car.feature.alerts.domain

import com.example.my_car.feature.maintenance.domain.MaintenanceRules
import com.example.my_car.feature.maintenance.domain.MaintenanceStatus

/**
 * Lógica pura para evaluar si una actividad debe lanzar una alerta inmediata
 * basándose en los parámetros de anticipación.
 */
object AlertCalculator {

    /**
     * Determina si se debe lanzar una alerta inmediata evaluando el estado del mantenimiento.
     * Retorna true si el estado es PROXIMA o VENCIDA.
     */
    fun shouldTriggerAlert(
        deadlineDate: Long?,
        limitMileage: Int?,
        currentDate: Long,
        currentMileage: Int,
        marginDays: Int,
        marginKm: Int
    ): Boolean {
        val status = MaintenanceRules.calculateStatus(
            deadlineDate = deadlineDate,
            limitMileage = limitMileage,
            currentDate = currentDate,
            currentMileage = currentMileage,
            marginDays = marginDays,
            marginKm = marginKm
        )
        return status == MaintenanceStatus.PROXIMA || status == MaintenanceStatus.VENCIDA
    }
}
