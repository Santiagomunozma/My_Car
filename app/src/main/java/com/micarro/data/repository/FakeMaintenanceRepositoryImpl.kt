package com.micarro.data.repository

import com.micarro.domain.model.CategoryExpenseDto
import com.micarro.domain.model.HistoryFilter
import com.micarro.domain.model.MaintenanceHistoryItem
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import com.micarro.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeMaintenanceRepositoryImpl @Inject constructor() : MaintenanceRepository {
    override fun observeHistory(
        vehicleId: String?,
        filter: HistoryFilter
    ): Flow<List<MaintenanceHistoryItem>> = flowOf(emptyList())

    override fun observePlans(vehicleId: String): Flow<List<MaintenancePlan>> = flowOf(emptyList())

    override suspend fun savePlan(plan: MaintenancePlan) {}

    override suspend fun updatePlan(plan: MaintenancePlan) {}

    override suspend fun deletePlanIfWithoutHistory(planId: String): Boolean = true

    override suspend fun registerService(service: MaintenanceService, parts: List<Part>) {}

    override fun observeServices(vehicleId: String): Flow<List<MaintenanceService>> = flowOf(emptyList())
    override fun observeExpensesByCategory(
        vehicleId: String,
        startDateTimestamp: Long
    ): Flow<List<CategoryExpenseDto>> = flowOf(emptyList())
}
