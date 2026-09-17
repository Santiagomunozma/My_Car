package com.example.my_car.data.repository

import com.example.my_car.domain.model.HistoryFilter
import com.example.my_car.domain.model.MaintenanceHistoryItem
import com.example.my_car.domain.model.MaintenancePlan
import com.example.my_car.domain.model.MaintenanceService
import com.example.my_car.domain.model.Part
import com.example.my_car.domain.repository.MaintenanceRepository
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
}
