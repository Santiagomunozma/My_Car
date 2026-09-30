package com.micarro.feature.maintenance.domain

import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import com.micarro.fakes.InMemoryMaintenanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MaintenanceUseCasesTest {

    private lateinit var repository: InMemoryMaintenanceRepository
    private lateinit var useCases: MaintenanceUseCases

    @Before
    fun setUp() {
        repository = InMemoryMaintenanceRepository()
        useCases = MaintenanceUseCases(
            observePlans = ObserveMaintenancePlansUseCase(repository),
            savePlan = SaveMaintenancePlanUseCase(repository),
            updatePlan = UpdateMaintenancePlanUseCase(repository),
            updatePlanActiveStatus = UpdateMaintenancePlanActiveStatusUseCase(repository),
            deletePlan = DeleteMaintenancePlanUseCase(repository),
            registerService = RegisterMaintenanceServiceUseCase(repository),
            observeServices = ObserveMaintenanceServicesUseCase(repository)
        )
    }

    @Test
    fun `eliminar actividad sin historial debe ser exitoso`() = runTest {
        val plan = MaintenancePlan(
            id = "plan-1",
            vehicleId = "1",
            title = "Cambio de filtro",
            category = "Filtros",
            intervalMileage = 10000,
            intervalMonths = 12
        )
        useCases.savePlan(plan)

        val initialPlans = useCases.observePlans("1").first()
        assertEquals(1, initialPlans.size)

        // No hay servicios asociados: debe permitir eliminar
        val deleted = useCases.deletePlan("plan-1")
        assertTrue(deleted)

        val remainingPlans = useCases.observePlans("1").first()
        assertEquals(0, remainingPlans.size)
    }

    @Test
    fun `eliminar actividad con historial asociado debe ser rechazado`() = runTest {
        val plan = MaintenancePlan(
            id = "plan-aceite",
            vehicleId = "1",
            title = "Cambio de Aceite",
            category = "Aceite",
            intervalMileage = 5000,
            intervalMonths = 6
        )
        useCases.savePlan(plan)

        // Registramos un servicio asociado a este plan
        val service = MaintenanceService(
            id = "srv-1",
            vehicleId = "1",
            planId = "plan-aceite",
            title = "Servicio de aceite realizado",
            category = "Aceite",
            date = System.currentTimeMillis(),
            mileage = 5000,
            totalCost = 120000.0,
            workshopName = "Taller Central"
        )
        useCases.registerService(service, emptyList())

        // RF-15: Debe rechazar la eliminación porque tiene historial
        val deleted = useCases.deletePlan("plan-aceite")
        assertFalse("Eliminar actividad con historial debe ser rechazado", deleted)

        // El plan aún debe existir
        val plans = useCases.observePlans("1").first()
        assertEquals(1, plans.size)
    }

    @Test
    fun `un mantenimiento puede tener varios repuestos y persistirlos correctamente`() = runTest {
        val service = MaintenanceService(
            id = "srv-multirepuestos",
            vehicleId = "1",
            planId = null,
            title = "Mantenimiento General de Frenos",
            category = "Frenos",
            date = System.currentTimeMillis(),
            mileage = 20000,
            totalCost = 350000.0,
            workshopName = "Frenos del Norte"
        )

        val parts = listOf(
            Part(
                serviceId = "srv-multirepuestos",
                name = "Pastillas delanteras",
                quantity = 2,
                cost = 80000.0,
                brand = "Brembo",
                reference = "BR-102"
            ),
            Part(
                serviceId = "srv-multirepuestos",
                name = "Líquido de frenos DOT4",
                quantity = 1,
                cost = 45000.0,
                brand = "Motul",
                reference = "DOT4-500"
            ),
            Part(
                serviceId = "srv-multirepuestos",
                name = "Discos de freno ventilados",
                quantity = 2,
                cost = 110000.0,
                brand = "Fremax",
                reference = "BD-334"
            )
        )

        useCases.registerService(service, parts)

        assertEquals(3, repository.registeredParts.size)
        val totalPartsCost = repository.registeredParts.sumOf { it.cost * it.quantity }
        assertEquals(425000.0, totalPartsCost, 0.001)
    }

    @Test
    fun `pausar y reactivar actividad actualiza estado de activacion`() = runTest {
        val plan = MaintenancePlan(
            id = "plan-activo",
            vehicleId = "1",
            title = "Alineación",
            category = "Suspensión",
            intervalMileage = 10000,
            intervalMonths = 6,
            isActive = true
        )
        useCases.savePlan(plan)

        // Pausar
        useCases.updatePlanActiveStatus("plan-activo", false)
        val pausedPlan = useCases.observePlans("1").first().first()
        assertFalse(pausedPlan.isActive)

        // Reactivar
        useCases.updatePlanActiveStatus("plan-activo", true)
        val reactivatedPlan = useCases.observePlans("1").first().first()
        assertTrue(reactivatedPlan.isActive)
    }
}
