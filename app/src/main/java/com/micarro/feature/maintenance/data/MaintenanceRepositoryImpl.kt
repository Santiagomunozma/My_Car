package com.micarro.feature.maintenance.data

import androidx.room.withTransaction
import com.micarro.core.database.AppDatabase
import com.micarro.domain.model.CategoryExpenseDto
import com.micarro.domain.model.HistoryFilter
import com.micarro.domain.model.MaintenanceHistoryItem
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.feature.parts.data.PartDao
import com.micarro.feature.parts.data.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MaintenanceRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val planDao: MaintenancePlanDao,
    private val serviceDao: MaintenanceServiceDao,
    private val partDao: PartDao
) : MaintenanceRepository {

    override fun observeHistory(vehicleId: String?, filter: HistoryFilter): Flow<List<MaintenanceHistoryItem>> {
        val effectiveVehicleId = vehicleId ?: filter.vehicleId
        return serviceDao.observeHistory(
            vehicleId = effectiveVehicleId,
            query = filter.query ?: "",
            category = filter.category,
            serviceType = filter.serviceType,
            workshop = filter.workshop,
            minCost = filter.minCost,
            maxCost = filter.maxCost,
            startDate = filter.startDate,
            endDate = filter.endDate
        ).map { rows ->
            rows.map { row ->
                MaintenanceHistoryItem(
                    id = row.id,
                    vehiclePlate = row.vehiclePlate,
                    title = row.title,
                    category = row.category,
                    date = row.date,
                    mileage = row.mileage,
                    totalCost = row.totalCost,
                    workshopName = row.workshopName,
                    serviceType = row.serviceType,
                    vehicleId = row.vehicleId
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

    override suspend fun updatePlanActiveStatus(planId: String, isActive: Boolean) {
        planDao.updateActiveStatus(planId, isActive)
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

    override suspend fun getPlanById(planId: String): MaintenancePlan? =
        planDao.getById(planId)?.toDomain()

    override suspend fun registerService(service: MaintenanceService, parts: List<Part>) {
        database.withTransaction {
            serviceDao.insert(service.toEntity())
            if (parts.isNotEmpty()) {
                partDao.insertAll(parts.map { it.copy(serviceId = service.id).toEntity() })
            }
        }
    }

    override suspend fun updateService(service: MaintenanceService, parts: List<Part>) {
        database.withTransaction {
            serviceDao.insert(service.toEntity())
            partDao.deleteByService(service.id)
            if (parts.isNotEmpty()) {
                partDao.insertAll(parts.map { it.copy(serviceId = service.id).toEntity() })
            }
        }
    }

    override suspend fun deleteService(serviceId: String) {
        database.withTransaction {
            partDao.deleteByService(serviceId)
            serviceDao.deleteById(serviceId)
        }
    }

    override suspend fun getServiceById(serviceId: String): MaintenanceService? =
        serviceDao.getById(serviceId)?.toDomain()

    override fun observeServices(vehicleId: String): Flow<List<MaintenanceService>> {
        return serviceDao.observeServices(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeExpensesByCategory(
        vehicleId: String,
        startDateTimestamp: Long
    ): Flow<List<CategoryExpenseDto>> {
        return serviceDao.observeExpensesByCategory(vehicleId, startDateTimestamp)
    }
}
