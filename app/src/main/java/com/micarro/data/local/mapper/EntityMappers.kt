package com.micarro.data.local.mapper

import com.micarro.data.local.entity.MaintenanceEntity
import com.micarro.domain.model.MaintenanceHistoryItem

fun MaintenanceEntity.toDomain() = MaintenanceHistoryItem(
    id = id.toString(),
    vehiclePlate = vehiclePlate,
    title = title,
    category = category,
    date = date,
    mileage = mileage,
    totalCost = totalCost,
    workshopName = workshopName
)

fun MaintenanceHistoryItem.toEntity() = MaintenanceEntity(
    id = id.toLongOrNull() ?: 0L,
    vehiclePlate = vehiclePlate,
    title = title,
    category = category,
    date = date,
    mileage = mileage,
    totalCost = totalCost,
    workshopName = workshopName
)
