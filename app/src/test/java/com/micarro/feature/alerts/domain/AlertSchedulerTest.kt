package com.micarro.feature.alerts.domain

import com.micarro.core.worker.AlertScheduler as CoreAlertScheduler
import com.micarro.domain.model.AlertSettings
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.repository.AlertSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlertSchedulerTest {

    private class RecordingCoreAlertScheduler : CoreAlertScheduler() {
        val scheduled = mutableListOf<Triple<Long, String, String>>()
        val names = mutableListOf<String>()
        val cancelled = mutableListOf<String>()

        override fun scheduleAlert(
            uniqueWorkName: String,
            delayInMinutes: Long,
            title: String,
            message: String,
            vehicleLabel: String,
            activityTitle: String,
            cause: String
        ) {
            names += uniqueWorkName
            scheduled += Triple(delayInMinutes, title, message)
        }

        override fun cancelAlert(uniqueWorkName: String) {
            cancelled += uniqueWorkName
        }
    }

    private class FixedSettings(initial: AlertSettings = AlertSettings()) : AlertSettingsRepository {
        private val state = MutableStateFlow(initial)
        override fun observeSettings(): Flow<AlertSettings> = state
        override fun current(): AlertSettings = state.value
        override suspend fun setGlobalAlertsEnabled(enabled: Boolean) {}
        override suspend fun setAnticipationDays(days: Int) {}
        override suspend fun setMaintenanceAlertsEnabled(enabled: Boolean) {}
        override suspend fun setMaintenanceMargins(days: Int, km: Int) {}
    }

    private lateinit var fakeCoreScheduler: RecordingCoreAlertScheduler
    private lateinit var scheduler: AlertScheduler

    @Before
    fun setUp() {
        fakeCoreScheduler = RecordingCoreAlertScheduler()
        scheduler = AlertScheduler(fakeCoreScheduler, FixedSettings())
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

        scheduler.scheduleForActivity(plan, vehicleLabel = "ABC123")

        assertEquals(1, fakeCoreScheduler.scheduled.size)
        val (delay, title, message) = fakeCoreScheduler.scheduled.first()
        assertTrue(delay > 0)
        assertTrue(title.contains("Cambio de Aceite"))
        assertTrue(message.contains("Aceite"))
        assertTrue(message.contains("ABC123"))
        assertEquals("activity_plan-aceite", fakeCoreScheduler.names.first())
    }

    @Test
    fun `la alerta de fecha sale con el margen de anticipacion no el dia del vencimiento`() {
        val due = System.currentTimeMillis() + 20L * 24L * 60L * 60L * 1000L
        val plan = MaintenancePlan(
            id = "plan-fecha",
            vehicleId = "1",
            title = "Frenos",
            category = "Frenos",
            intervalMileage = 0,
            intervalMonths = 0,
            nextDeadlineDate = due,
            marginDays = 15
        )
        scheduler.scheduleForActivity(plan, vehicleLabel = "ABC123")
        val delay = fakeCoreScheduler.scheduled.first().first
        val fiveDays = 5L * 24L * 60L
        assertTrue(delay in (fiveDays - 2)..(fiveDays + 2))
    }

    @Test
    fun `el margen de ajustes manda sobre el margen guardado en el plan`() {
        val custom = AlertScheduler(
            fakeCoreScheduler,
            FixedSettings(com.micarro.domain.model.AlertSettings(maintenanceMarginDays = 30))
        )
        val due = System.currentTimeMillis() + 40L * 24L * 60L * 60L * 1000L
        val plan = MaintenancePlan(
            id = "plan-margen",
            vehicleId = "1",
            title = "Frenos",
            category = "Frenos",
            intervalMileage = 0,
            intervalMonths = 0,
            nextDeadlineDate = due,
            marginDays = 15
        )
        custom.scheduleForActivity(plan)
        val delay = fakeCoreScheduler.scheduled.last().first
        val tenDays = 10L * 24L * 60L
        assertTrue(delay in (tenDays - 2)..(tenDays + 2))
    }

    @Test
    fun `cancelar actividad cancela el trabajo de alerta asociado`() {
        scheduler.cancelForActivity("plan-aceite")

        assertEquals(1, fakeCoreScheduler.cancelled.size)
        assertEquals("activity_plan-aceite", fakeCoreScheduler.cancelled.first())
    }

    @Test
    fun `posponer alerta reprograma aviso para nueva fecha RN-08`() {
        val futureTime = System.currentTimeMillis() + (60 * 60 * 1000)
        scheduler.postpone("alert-1", futureTime, activityTitle = "Aceite", vehicleLabel = "ABC")

        assertEquals(1, fakeCoreScheduler.scheduled.size)
        val (delay, title, message) = fakeCoreScheduler.scheduled.first()
        assertTrue(delay in 58..62)
        assertTrue(title.contains("pospuesto"))
        assertTrue(message.contains("no cambia"))
        assertEquals("activity_alert-1", fakeCoreScheduler.names.first())
    }

    @Test
    fun `snooze alerta programa recordatorio retrasado`() {
        scheduler.snoozeAlert(15, "Revisar frenos", "Faltan pocos km")

        assertEquals(1, fakeCoreScheduler.scheduled.size)
        val (delay, title, message) = fakeCoreScheduler.scheduled.first()
        assertEquals(15L, delay)
        assertTrue(title.contains("Recordatorio"))
        assertEquals("Faltan pocos km", message)
        assertEquals("activity_snooze_Revisar frenos", fakeCoreScheduler.names.first())
    }
}
