package com.micarro.feature.demo

import com.micarro.domain.model.DocumentStatus
import com.micarro.domain.model.DocumentType
import com.micarro.feature.documents.domain.DocumentStatusRules
import com.micarro.feature.maintenance.domain.MaintenanceRules
import com.micarro.feature.maintenance.domain.MaintenanceStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class DemoScenarioTest {

    private val today = LocalDate.of(2026, 11, 10)
    private val now = today.atTime(15, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
    private val snapshot = DemoScenario.build(today)

    @Test
    fun `el aceite queda vencido y los frenos proximos`() {
        val oil = snapshot.plans.first { it.id == DemoScenario.OIL_PLAN }
        val brakes = snapshot.plans.first { it.id == DemoScenario.BRAKE_PLAN }

        assertEquals(
            MaintenanceStatus.VENCIDA,
            statusOf(oil)
        )
        assertEquals(
            MaintenanceStatus.PROXIMA,
            statusOf(brakes)
        )
    }

    @Test
    fun `el soat esta vencido y la tecnico mecanica proxima`() {
        val soat = snapshot.documents.first {
            it.vehicleId == DemoScenario.CAR_ID && it.type == DocumentType.SOAT
        }
        val inspection = snapshot.documents.first {
            it.type == DocumentType.TECHNICAL_INSPECTION
        }

        assertEquals(
            DocumentStatus.EXPIRED,
            DocumentStatusRules.statusFor(
                soat.expirationDate,
                DemoScenario.DOCUMENT_ANTICIPATION_DAYS,
                today
            )
        )
        assertEquals(
            DocumentStatus.UPCOMING,
            DocumentStatusRules.statusFor(
                inspection.expirationDate,
                DemoScenario.DOCUMENT_ANTICIPATION_DAYS,
                today
            )
        )
    }

    private fun statusOf(plan: DemoScenario.Plan): MaintenanceStatus =
        MaintenanceRules.calculateStatus(
            deadlineDate = plan.deadline?.let(DemoScenario::utcMillis),
            limitMileage = plan.limitMileage,
            currentDate = now,
            currentMileage = DemoScenario.CAR_KM.toInt(),
            marginDays = plan.marginDays,
            marginKm = plan.marginKm
        )
}
