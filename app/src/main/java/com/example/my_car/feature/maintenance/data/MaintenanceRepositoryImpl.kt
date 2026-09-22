package com.example.my_car.feature.maintenance.data

import com.example.my_car.domain.model.*
import com.example.my_car.domain.repository.MaintenanceRepository
import com.example.my_car.feature.parts.data.PartDao
import com.example.my_car.feature.parts.data.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.my_car.domain.model.CategoryExpenseDto
import javax.inject.Inject

class MaintenanceRepositoryImpl @Inject constructor(
    private val planDao: MaintenancePlanDao,
    private val serviceDao: MaintenanceServiceDao,
    private val partDao: PartDao
) : MaintenanceRepository {

    override fun observeHistory(vehicleId: String?, filter: HistoryFilter): Flow<List<MaintenanceHistoryItem>> {
        return serviceDao.observeHistory(
            vehicleId = vehicleId,
            query = filter.query ?: "",
            category = filter.category,
            minCost = filter.minCost,
            maxCost = filter.maxCost,
            startDate = filter.startDate,
            endDate = filter.endDate
        ).map { entities ->
            entities.map { entity ->
                MaintenanceHistoryItem(
                    id = entity.id,
                    vehiclePlate = entity.vehicleId,
                    title = entity.title,
                    category = entity.category,
                    date = entity.date,
                    mileage = entity.mileage,
                    totalCost = entity.totalCost,
                    workshopName = entity.workshopName
                )
            }
        }
    }

    override fun observePlans(vehicleId: String): Flow<List<MaintenancePlan>> {
        return planDao.observePlans(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun savePlan(plan: MaintenancePlan) {
        planDao.insert(plan.toEntity())
    }

    override suspend fun updatePlan(plan: MaintenancePlan) {
        planDao.update(plan.toEntity())
    }

    override suspend fun deletePlanIfWithoutHistory(planId: String): Boolean {
        val count = planDao.getServiceCountForPlan(planId)
        return if (count == 0) {
            planDao.delete(planId)
            true
        } else {
            false
        }
    }

    override suspend fun registerService(service: MaintenanceService, parts: List<Part>) {
        serviceDao.insert(service.toEntity())
        partDao.insertAll(parts.map { it.toEntity() })
    }

    override fun observeServices(vehicleId: String): Flow<List<MaintenanceService>> {
        return serviceDao.observeServices(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    override fun observeExpensesByCategory(
        vehicleId: String,
        startDateTimestamp: Long
    ): Flow<List<CategoryExpenseDto>> {
        // Aquí llamamos al DAO inyectado de la instancia de la clase, no estáticamente
        return serviceDao.observeExpensesByCategory(vehicleId, startDateTimestamp)
}
}