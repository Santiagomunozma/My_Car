package com.example.my_car.data.local.mapper

import com.example.my_car.data.local.entity.MaintenanceEntity
import com.example.my_car.domain.model.MaintenanceHistoryItem

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
