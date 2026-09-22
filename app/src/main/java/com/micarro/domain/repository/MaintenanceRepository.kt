package com.micarro.domain.repository

import com.micarro.domain.model.CategoryExpenseDto
import com.micarro.domain.model.HistoryFilter
import com.micarro.domain.model.MaintenanceHistoryItem
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    fun observeHistory(vehicleId: String?, filter: HistoryFilter): Flow<List<MaintenanceHistoryItem>>

    fun observePlans(vehicleId: String): Flow<List<MaintenancePlan>>
    suspend fun savePlan(plan: MaintenancePlan)
    suspend fun updatePlan(plan: MaintenancePlan)
    suspend fun deletePlanIfWithoutHistory(planId: String): Boolean
    suspend fun registerService(service: MaintenanceService, parts: List<Part>)
    fun observeServices(vehicleId: String): Flow<List<MaintenanceService>>
    fun observeExpensesByCategory(vehicleId: String, startDateTimestamp: Long): Flow<List<CategoryExpenseDto>>
}
