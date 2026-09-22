package com.micarro.feature.maintenance.domain

import java.util.Calendar

/**
 * Estados posibles de una actividad o plan de mantenimiento según las reglas de negocio.
 */
enum class MaintenanceStatus {
    VENCIDA,
    PROXIMA,
    AL_DIA,
    SIN_PROGRAMACION
}

/**
 * Reglas de negocio puras para el dominio de mantenimiento.
 * Implementa RN-01 a RN-06 del enunciado.
 */
object MaintenanceRules {

    /**
     * RN-01 y RN-02: Determina el estado de una actividad/plan basándose en la fecha límite,
     * kilometraje límite, fecha actual, kilometraje actual del vehículo y márgenes de anticipación.
     */
    fun calculateStatus(
        deadlineDate: Long?,
        limitMileage: Int?,
        currentDate: Long,
        currentMileage: Int,
        marginDays: Int,
        marginKm: Int
    ): MaintenanceStatus {
        if (deadlineDate == null && limitMileage == null) {
            return MaintenanceStatus.SIN_PROGRAMACION
        }

        // RN-01: Verificación de vencimiento
        val isDateOverdue = deadlineDate?.let { currentDate > it } ?: false
        val isMileageOverdue = limitMileage?.let { currentMileage > it } ?: false

        if (isDateOverdue || isMileageOverdue) {
            return MaintenanceStatus.VENCIDA
        }

        // RN-02: Verificación de proximidad
        val marginMillis = marginDays.toLong() * 24 * 60 * 60 * 1000
        val isDateUpcoming = deadlineDate?.let { (currentDate + marginMillis) >= it } ?: false
        val isMileageUpcoming = limitMileage?.let { (currentMileage + marginKm) >= it } ?: false

        return if (isDateUpcoming || isMileageUpcoming) {
            MaintenanceStatus.PROXIMA
        } else {
            MaintenanceStatus.AL_DIA
        }
    }

    /**
     * RN-03: Calcula la fecha y kilometraje de la siguiente recurrencia nacida del servicio real realizado.
     */
    fun calculateNextRecurrence(
        actualDate: Long,
        actualMileage: Int,
        intervalMonths: Int,
        intervalMileage: Int
    ): Pair<Long, Int> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = actualDate
        calendar.add(Calendar.MONTH, intervalMonths)
        
        val nextDate = calendar.timeInMillis
        val nextMileage = actualMileage + intervalMileage
        
        return Pair(nextDate, nextMileage)
    }

    /**
     * RN-04: Calcula el costo total sumando mano de obra, repuestos y otros costos.
     */
    fun calculateTotalCost(
        laborCost: Double,
        partsCost: Double,
        otherCosts: Double
    ): Double {
        return laborCost + partsCost + otherCosts
    }

    /**
     * RN-05: Valida que una fecha de realización no sea futura.
     */
    fun isDateValid(realizationDate: Long, currentDate: Long): Boolean {
        return realizationDate <= currentDate
    }

    /**
     * RN-06: Valida si el kilometraje del servicio es menor al último guardado.
     * Retorna false si el kilometraje es inválido (menor al último).
     */
    fun isMileageValid(serviceMileage: Int, lastMileage: Int): Boolean {
        return serviceMileage >= lastMileage
    }
}
