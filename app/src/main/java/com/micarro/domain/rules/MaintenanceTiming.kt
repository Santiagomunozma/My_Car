package com.micarro.domain.rules

/**
 * Cálculo compartido de vencimiento, proximidad y retraso de la notificación.
 * La alerta debe salir al entrar en el margen, no el día del vencimiento.
 */
object MaintenanceTiming {
    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

    fun isOverdue(
        deadlineDate: Long?,
        limitMileage: Int?,
        currentDate: Long,
        currentMileage: Int
    ): Boolean {
        val dateOverdue = deadlineDate?.let { currentDate > it } ?: false
        val mileageOverdue = limitMileage?.let { currentMileage > it } ?: false
        return dateOverdue || mileageOverdue
    }

    fun isUpcoming(
        deadlineDate: Long?,
        limitMileage: Int?,
        currentDate: Long,
        currentMileage: Int,
        marginDays: Int,
        marginKm: Int
    ): Boolean {
        if (isOverdue(deadlineDate, limitMileage, currentDate, currentMileage)) return false
        val marginMillis = marginDays.coerceAtLeast(0) * DAY_MILLIS
        val dateSoon = deadlineDate?.let { currentDate + marginMillis >= it } ?: false
        val mileageSoon = limitMileage?.let { currentMileage + marginKm.coerceAtLeast(0) >= it } ?: false
        return dateSoon || mileageSoon
    }

    /**
     * Minutos hasta el aviso. Null si no hay fecha ni intervalo y todavía no está próximo:
     * un plan solo por kilometraje no debe disparar un aviso inmediato.
     */
    fun alertDelayMinutes(
        dueAt: Long?,
        marginDays: Int,
        now: Long,
        intervalMonths: Int,
        notifyImmediately: Boolean
    ): Long? {
        if (dueAt != null) {
            val alertAt = dueAt - marginDays.coerceAtLeast(0) * DAY_MILLIS
            return ((alertAt - now) / 60_000L).coerceAtLeast(0L)
        }
        if (notifyImmediately) return 0L
        if (intervalMonths > 0) {
            val wholeMinutes = intervalMonths.toLong() * 30L * 24L * 60L
            val marginMinutes = marginDays.coerceAtLeast(0).toLong() * 24L * 60L
            return (wholeMinutes - marginMinutes).coerceAtLeast(0L)
        }
        return null
    }
}
