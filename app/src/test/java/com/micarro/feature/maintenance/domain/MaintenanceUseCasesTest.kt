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
            updateService = UpdateMaintenanceServiceUseCase(repository),
            deleteService = DeleteMaintenanceServiceUseCase(repository),
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
            laborCost = 120000.0,
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
            laborCost = 0.0,
            totalCost = 425000.0,
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

    @Test
    fun `fecha futura y monto negativo se rechazan antes de persistir`() = runTest {
        val future = MaintenanceService(
            id = "srv-futuro",
            vehicleId = "1",
            title = "Servicio",
            category = "Aceite",
            date = System.currentTimeMillis() + 86_400_000L,
            mileage = 1000,
            laborCost = 10.0,
            totalCost = 10.0,
            workshopName = "Taller"
        )
        val futureResult = useCases.registerService(future, emptyList(), lastKnownMileage = 1000)
        assertTrue(futureResult is RegisterServiceResult.Invalid)

        val negative = future.copy(
            id = "srv-neg",
            date = System.currentTimeMillis(),
            laborCost = -1.0,
            totalCost = -1.0
        )
        val negativeResult = useCases.registerService(negative, emptyList(), lastKnownMileage = 1000)
        assertTrue(negativeResult is RegisterServiceResult.Invalid)
        assertEquals(0, repository.services.value.size)
    }

    @Test
    fun `kilometraje menor y total ajustado exigen confirmacion y RN-03 persiste el proximo`() = runTest {
        val plan = MaintenancePlan(
            id = "plan-rn",
            vehicleId = "1",
            title = "Aceite",
            category = "Aceite",
            intervalMileage = 5000,
            intervalMonths = 6
        )
        useCases.savePlan(plan)
        val service = MaintenanceService(
            id = "srv-rn",
            vehicleId = "1",
            planId = plan.id,
            title = "Cambio",
            category = "Aceite",
            date = System.currentTimeMillis(),
            mileage = 1000,
            laborCost = 50.0,
            totalCost = 80.0,
            workshopName = "Taller"
        )
        val mileageResult = useCases.registerService(service, emptyList(), lastKnownMileage = 4000)
        assertTrue(mileageResult is RegisterServiceResult.RequiresMileageConfirmation)

        val totalResult = useCases.registerService(
            service,
            emptyList(),
            lastKnownMileage = 1000,
            confirmedLowerMileage = true
        )
        assertTrue(totalResult is RegisterServiceResult.RequiresTotalConfirmation)

        val saved = useCases.registerService(
            service,
            emptyList(),
            lastKnownMileage = 1000,
            confirmedLowerMileage = true,
            confirmedAdjustedTotal = true
        )
        assertTrue(saved is RegisterServiceResult.Success)
        val updated = useCases.observePlans("1").first().first()
        assertEquals(6000, updated.nextLimitMileage)
        assertTrue(updated.nextDeadlineDate != null)
    }

    @Test
    fun `borrar un servicio recalcula el proximo vencimiento con el historial que queda`() = runTest {
        val plan = MaintenancePlan(
            id = "plan-del",
            vehicleId = "1",
            title = "Aceite",
            category = "Aceite",
            intervalMileage = 1000,
            intervalMonths = 0
        )
        useCases.savePlan(plan)
        val older = MaintenanceService(
            id = "srv-old",
            vehicleId = "1",
            planId = plan.id,
            title = "Primero",
            category = "Aceite",
            date = 1_000L,
            mileage = 1000,
            laborCost = 10.0,
            totalCost = 10.0,
            workshopName = "Taller"
        )
        val newer = older.copy(id = "srv-new", date = 2_000L, mileage = 2500, title = "Segundo")
        useCases.registerService(older, emptyList(), lastKnownMileage = 0, confirmedLowerMileage = true, confirmedAdjustedTotal = true)
        useCases.registerService(newer, emptyList(), lastKnownMileage = 0, confirmedLowerMileage = true, confirmedAdjustedTotal = true)
        useCases.deleteService(newer.id)
        val updated = useCases.observePlans("1").first().first()
        assertEquals(2000, updated.nextLimitMileage)
    }

    @Test
    fun `fecha anterior al ultimo servicio de la actividad exige confirmacion`() = runTest {
        val plan = MaintenancePlan(
            id = "plan-fecha",
            vehicleId = "1",
            title = "Aceite",
            category = "Aceite",
            intervalMileage = 1000,
            intervalMonths = 0
        )
        useCases.savePlan(plan)
        val first = MaintenanceService(
            id = "srv-a",
            vehicleId = "1",
            planId = plan.id,
            title = "Primero",
            category = "Aceite",
            date = 5_000L,
            mileage = 1000,
            laborCost = 10.0,
            totalCost = 10.0,
            workshopName = "Taller"
        )
        useCases.registerService(first, emptyList(), lastKnownMileage = 0, confirmedLowerMileage = true, confirmedAdjustedTotal = true)
        val earlier = first.copy(id = "srv-b", date = 1_000L, title = "Antes")
        val blocked = useCases.registerService(
            earlier,
            emptyList(),
            lastKnownMileage = 0,
            confirmedLowerMileage = true,
            confirmedAdjustedTotal = true
        )
        assertTrue(blocked is RegisterServiceResult.RequiresEarlierDate)
        val saved = useCases.registerService(
            earlier,
            emptyList(),
            lastKnownMileage = 0,
            confirmedLowerMileage = true,
            confirmedAdjustedTotal = true,
            confirmedEarlierDate = true
        )
        assertTrue(saved is RegisterServiceResult.Success)
    }
}
