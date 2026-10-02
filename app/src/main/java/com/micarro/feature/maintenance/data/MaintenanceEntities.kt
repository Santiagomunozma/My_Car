package com.micarro.feature.maintenance.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.ServiceType

@Entity(tableName = "maintenance_plans")
data class MaintenancePlanEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val title: String,
    val category: String,
    val intervalMileage: Int,
    val intervalMonths: Int,
    val isActive: Boolean = true,
    val description: String = "",
    val nextDeadlineDate: Long? = null,
    val nextLimitMileage: Int? = null,
    val marginDays: Int = 15,
    val marginKm: Int = 500,
    val alertsEnabled: Boolean = true
)

@Entity(tableName = "maintenance_services")
data class MaintenanceServiceEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val planId: String?,
    val title: String,
    val category: String,
    val date: Long,
    val mileage: Int,
    val totalCost: Double,
    val workshopName: String,
    val serviceType: String = ServiceType.CORRECTIVE.name,
    val laborCost: Double = 0.0,
    val otherCosts: Double = 0.0,
    val evidenceUri: String? = null,
    val description: String = ""
)

fun MaintenancePlanEntity.toDomain() = MaintenancePlan(
    id = id,
    vehicleId = vehicleId,
    title = title,
    category = category,
    intervalMileage = intervalMileage,
    intervalMonths = intervalMonths,
    isActive = isActive,
    description = description,
    nextDeadlineDate = nextDeadlineDate,
    nextLimitMileage = nextLimitMileage,
    marginDays = marginDays,
    marginKm = marginKm,
    alertsEnabled = alertsEnabled
)

fun MaintenancePlan.toEntity() = MaintenancePlanEntity(
    id = id,
    vehicleId = vehicleId,
    title = title,
    category = category,
    intervalMileage = intervalMileage,
    intervalMonths = intervalMonths,
    isActive = isActive,
    description = description,
    nextDeadlineDate = nextDeadlineDate,
    nextLimitMileage = nextLimitMileage,
    marginDays = marginDays,
    marginKm = marginKm,
    alertsEnabled = alertsEnabled
)

fun MaintenanceServiceEntity.toDomain() = MaintenanceService(
    id = id,
    vehicleId = vehicleId,
    planId = planId,
    title = title,
    category = category,
    date = date,
    mileage = mileage,
    totalCost = totalCost,
    workshopName = workshopName,
    serviceType = runCatching { ServiceType.valueOf(serviceType) }.getOrDefault(ServiceType.CORRECTIVE),
    laborCost = laborCost,
    otherCosts = otherCosts,
    evidenceUri = evidenceUri,
    description = description
)

fun MaintenanceService.toEntity() = MaintenanceServiceEntity(
    id = id,
    vehicleId = vehicleId,
    planId = planId,
    title = title,
    category = category,
    date = date,
    mileage = mileage,
    totalCost = totalCost,
    workshopName = workshopName,
    serviceType = serviceType.name,
    laborCost = laborCost,
    otherCosts = otherCosts,
    evidenceUri = evidenceUri,
    description = description
)
