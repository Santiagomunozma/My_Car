package com.example.my_car.feature.history.presentation

import com.example.my_car.domain.model.HistoryFilter
import com.example.my_car.domain.model.MaintenanceHistoryItem
import com.example.my_car.domain.model.MaintenancePlan
import com.example.my_car.domain.model.MaintenanceService
import com.example.my_car.domain.model.Part
import com.example.my_car.domain.repository.MaintenanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

// Fake que satisface todos los métodos requeridos por la interfaz MaintenanceRepository
class FakeMaintenanceRepository : MaintenanceRepository {

    override fun observeHistory(vehicleId: String?, filter: HistoryFilter): Flow<List<MaintenanceHistoryItem>> {
        return flowOf(
            listOf(
                MaintenanceHistoryItem(
                    id = "1",
                    title = "Cambio de Aceite",
                    totalCost = 150.0,
                    category = "Motor",
                    vehiclePlate = "ABC123",
                    mileage = 50000,
                    workshopName = "Taller Central",
                    date = System.currentTimeMillis()
                )
            )
        )
    }

    override fun observePlans(vehicleId: String): Flow<List<MaintenancePlan>> {
        return flowOf(emptyList())
    }

    override suspend fun savePlan(plan: MaintenancePlan) {}
    override suspend fun updatePlan(plan: MaintenancePlan) {}
    override suspend fun deletePlanIfWithoutHistory(planId: String): Boolean = true

    override suspend fun registerService(
        service: MaintenanceService,
        parts: List<Part>
    ) {}

    override fun observeServices(vehicleId: String): Flow<List<MaintenanceService>> {
        return flowOf(emptyList())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: MaintenanceRepository
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMaintenanceRepository()
        viewModel = HistoryViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `al cambiar la consulta, filterState debe actualizar la cadena de texto`() = runTest {
        viewModel.onQueryChanged("Aceite")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Aceite", viewModel.filterState.value.query)
    }

    @Test
    fun `al seleccionar la misma categoria dos veces, la desmarca asignando null`() = runTest {
        viewModel.onCategorySelected("Frenos")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Frenos", viewModel.filterState.value.category)

        viewModel.onCategorySelected("Frenos")
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.filterState.value.category)
    }

    @Test
    fun `al limpiar filtros, restablece filterState al estado inicial`() = runTest {
        viewModel.onQueryChanged("Frenos")
        viewModel.onCategorySelected("Frenos")

        viewModel.clearFilters()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.filterState.value.query)
        assertNull(viewModel.filterState.value.category)
    }
}