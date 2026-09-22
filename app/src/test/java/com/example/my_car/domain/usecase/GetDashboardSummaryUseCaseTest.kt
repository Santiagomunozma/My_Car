package com.example.my_car.domain.usecase

import com.example.my_car.data.repository.FakeVehicleRepositoryImpl
import com.example.my_car.domain.model.HistoryFilter
import com.example.my_car.domain.model.MaintenanceHistoryItem
import com.example.my_car.domain.model.MaintenancePlan
import com.example.my_car.domain.model.MaintenanceService
import com.example.my_car.domain.model.Part
import com.example.my_car.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FakeMaintenanceRepository : MaintenanceRepository {
    override fun observeHistory(vehicleId: String?, filter: HistoryFilter): Flow<List<MaintenanceHistoryItem>> = flowOf(emptyList())
    override fun observePlans(vehicleId: String): Flow<List<MaintenancePlan>> = flowOf(emptyList())
    override suspend fun savePlan(plan: MaintenancePlan) {}
    override suspend fun updatePlan(plan: MaintenancePlan) {}
    override suspend fun deletePlanIfWithoutHistory(planId: String): Boolean = true
    override suspend fun registerService(service: MaintenanceService, parts: List<Part>) {}
    override fun observeServices(vehicleId: String): Flow<List<MaintenanceService>> = flowOf(emptyList())
    override fun observeExpensesByCategory(vehicleId: String, startDateTimestamp: Long) = flowOf(emptyList<com.example.my_car.domain.model.CategoryExpenseDto>())
}

class GetDashboardSummaryUseCaseTest {

    @Test
    fun `when no vehicles exist summary returns null main vehicle`() = runTest {
        val fakeRepo = FakeVehicleRepositoryImpl()
        val fakeMaintenanceRepo = FakeMaintenanceRepository()
        val useCase = GetDashboardSummaryUseCase(fakeRepo, fakeMaintenanceRepo)

        val result = useCase(flowOf(null)).first()

        assertNull(result.selectedVehicle)
        assertEquals(0, result.activeAlertsCount)
    }
}
