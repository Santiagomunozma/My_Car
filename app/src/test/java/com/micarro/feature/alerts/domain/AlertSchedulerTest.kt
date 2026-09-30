package com.micarro.feature.alerts.domain

import com.micarro.core.worker.AlertScheduler as CoreAlertScheduler
import com.micarro.domain.model.MaintenancePlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlertSchedulerTest {

    private class RecordingCoreAlertScheduler : CoreAlertScheduler() {
        val scheduled = mutableListOf<Triple<Long, String, String>>()
        val cancelled = mutableListOf<String>()

        override fun scheduleAlert(delayInMinutes: Long, title: String, message: String) {
            scheduled += Triple(delayInMinutes, title, message)
        }

        override fun cancelAlert(uniqueWorkName: String) {
            cancelled += uniqueWorkName
        }
    }

    private lateinit var fakeCoreScheduler: RecordingCoreAlertScheduler
    private lateinit var scheduler: AlertScheduler

    @Before
    fun setUp() {
        fakeCoreScheduler = RecordingCoreAlertScheduler()
        scheduler = AlertScheduler(fakeCoreScheduler)
    }

    @Test
    fun `al crear o actualizar actividad se programa la alerta con delay apropiado`() {
        val plan = MaintenancePlan(
            id = "plan-aceite",
            vehicleId = "1",
            title = "Cambio de Aceite",
            category = "Aceite",
            intervalMileage = 5000,
            intervalMonths = 6
        )

        scheduler.scheduleForActivity(plan)

        assertEquals(1, fakeCoreScheduler.scheduled.size)
        val (delay, title, message) = fakeCoreScheduler.scheduled.first()
        assertTrue(delay > 0)
        assertTrue(title.contains("Cambio de Aceite"))
        assertTrue(message.contains("Aceite"))
    }

    @Test
    fun `cancelar actividad cancela el trabajo de alerta asociado`() {
        scheduler.cancelForActivity("plan-aceite")

        assertEquals(1, fakeCoreScheduler.cancelled.size)
        assertEquals("activity_plan-aceite", fakeCoreScheduler.cancelled.first())
    }

    @Test
    fun `posponer alerta reprograma aviso para nueva fecha RN-08`() {
        val futureTime = System.currentTimeMillis() + (60 * 60 * 1000) // en 1 hora
        scheduler.postpone("alert-1", futureTime)

        assertEquals(1, fakeCoreScheduler.scheduled.size)
        val (delay, title, _) = fakeCoreScheduler.scheduled.first()
        assertTrue(delay in 58..62)
        assertTrue(title.contains("pospuesto"))
    }

    @Test
    fun `snooze alerta programa recordatorio retrasado`() {
        scheduler.snoozeAlert(15, "Revisar frenos", "Faltan pocos km")

        assertEquals(1, fakeCoreScheduler.scheduled.size)
        val (delay, title, message) = fakeCoreScheduler.scheduled.first()
        assertEquals(15L, delay)
        assertTrue(title.contains("Recordatorio"))
        assertEquals("Faltan pocos km", message)
    }
}
