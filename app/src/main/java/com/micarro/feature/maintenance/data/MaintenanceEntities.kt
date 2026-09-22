package com.micarro.feature.maintenance.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.MaintenanceService

@Entity(tableName = "maintenance_plans")
data class MaintenancePlanEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val title: String,
    val category: String,
    val intervalMileage: Int,
    val intervalMonths: Int
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
    val workshopName: String
)

fun MaintenancePlanEntity.toDomain() = MaintenancePlan(
    id = id,
    vehicleId = vehicleId,
    title = title,
    category = category,
    intervalMileage = intervalMileage,
    intervalMonths = intervalMonths
)

fun MaintenancePlan.toEntity() = MaintenancePlanEntity(
    id = id,
    vehicleId = vehicleId,
    title = title,
    category = category,
    intervalMileage = intervalMileage,
    intervalMonths = intervalMonths
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
    workshopName = workshopName
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
    workshopName = workshopName
)
