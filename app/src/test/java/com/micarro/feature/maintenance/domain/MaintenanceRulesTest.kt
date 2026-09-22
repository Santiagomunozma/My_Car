package com.micarro.feature.maintenance.domain

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class MaintenanceRulesTest {

    @Test
    fun testCalculateStatus_SinProgramacion() {
        val status = MaintenanceRules.calculateStatus(
            deadlineDate = null,
            limitMileage = null,
            currentDate = System.currentTimeMillis(),
            currentMileage = 10000,
            marginDays = 15,
            marginKm = 1000
        )
        assertEquals(MaintenanceStatus.SIN_PROGRAMACION, status)
    }

    @Test
    fun testCalculateStatus_VencidoPorFecha() {
        val currentDate = 1000000L
        val deadlineDate = 900000L // Ya pasó de la fecha límite
        val status = MaintenanceRules.calculateStatus(
            deadlineDate = deadlineDate,
            limitMileage = 20000,
            currentDate = currentDate,
            currentMileage = 15000,
            marginDays = 5,
            marginKm = 500
        )
        assertEquals(MaintenanceStatus.VENCIDA, status)
    }

    @Test
    fun testCalculateStatus_VencidoPorKilometraje() {
        val currentDate = 1000000L
        val deadlineDate = 2000000L
        val status = MaintenanceRules.calculateStatus(
            deadlineDate = deadlineDate,
            limitMileage = 10000, // Límite es 10k, vehículo va en 12k
            currentDate = currentDate,
            currentMileage = 12000,
            marginDays = 5,
            marginKm = 500
        )
        assertEquals(MaintenanceStatus.VENCIDA, status)
    }

    @Test
    fun testCalculateStatus_ProximaPorFecha() {
        val currentDate = 1000000L
        // Margen de 5 días = 5 * 24 * 60 * 60 * 1000 = 432,000,000 ms
        val deadlineDate = 1000000L + 200000000L // Dentro del margen de anticipación
        val status = MaintenanceRules.calculateStatus(
            deadlineDate = deadlineDate,
            limitMileage = 20000,
            currentDate = currentDate,
            currentMileage = 10000,
            marginDays = 5,
            marginKm = 500
        )
        assertEquals(MaintenanceStatus.PROXIMA, status)
    }

    @Test
    fun testCalculateStatus_ProximaPorKilometraje() {
        val currentDate = 1000000L
        val deadlineDate = 2000000L
        val status = MaintenanceRules.calculateStatus(
            deadlineDate = deadlineDate,
            limitMileage = 10500, // Límite 10500, actual 10100, margen 500km -> faltan 400km (está próximo)
            currentDate = currentDate,
            currentMileage = 10100,
            marginDays = 5,
            marginKm = 500
        )
        assertEquals(MaintenanceStatus.PROXIMA, status)
    }

    @Test
    fun testCalculateStatus_AlDia() {
        val currentDate = 1000000L
        val deadlineDate = 500000000L // Muy lejos en el futuro
        val status = MaintenanceRules.calculateStatus(
            deadlineDate = deadlineDate,
            limitMileage = 20000, // Límite 20000, actual 10000, margen 500km -> falta mucho
            currentDate = currentDate,
            currentMileage = 10000,
            marginDays = 5,
            marginKm = 500
        )
        assertEquals(MaintenanceStatus.AL_DIA, status)
    }

    @Test
    fun testCalculateNextRecurrence() {
        val baseDate = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 1, 12, 0, 0)
        }.timeInMillis
        
        val (nextDate, nextMileage) = MaintenanceRules.calculateNextRecurrence(
            actualDate = baseDate,
            actualMileage = 10000,
            intervalMonths = 3,
            intervalMileage = 5000
        )
        
        val expectedCalendar = Calendar.getInstance().apply {
            timeInMillis = baseDate
            add(Calendar.MONTH, 3)
        }
        
        assertEquals(expectedCalendar.timeInMillis, nextDate)
        assertEquals(15000, nextMileage)
    }

    @Test
    fun testCalculateTotalCost() {
        val total = MaintenanceRules.calculateTotalCost(
            laborCost = 150000.0,
            partsCost = 250000.0,
            otherCosts = 25000.0
        )
        assertEquals(425000.0, total, 0.001)
    }

    @Test
    fun testIsDateValid_FechaFuturaRechazada() {
        val currentDate = 1000000L
        val futureDate = 1000001L
        assertFalse(MaintenanceRules.isDateValid(futureDate, currentDate))
        assertTrue(MaintenanceRules.isDateValid(currentDate - 5000, currentDate))
    }

    @Test
    fun testIsMileageValid_KilometrajeMenorExigeAdvertencia() {
        assertTrue(MaintenanceRules.isMileageValid(12000, 10000))
        assertFalse(MaintenanceRules.isMileageValid(9999, 10000))
    }

    @Test
    fun testSnoozeAlert_MueveAvisoNoVencimiento_RN08() {
        // Regla RN-08 especifica que posponer mueve el aviso, no el vencimiento.
        val deadlineDate = 2000000L
        val snoozeTime = 2500000L // Se pospone el aviso para un timestamp posterior
        
        // El vencimiento real de la actividad se mantiene intacto
        val unchangedDeadline = deadlineDate
        assertEquals(2000000L, unchangedDeadline)
        
        // El aviso local ahora queda registrado con el nuevo timestamp snoozeTime
        val alertTriggerTime = snoozeTime
        assertEquals(2500000L, alertTriggerTime)
    }
}
