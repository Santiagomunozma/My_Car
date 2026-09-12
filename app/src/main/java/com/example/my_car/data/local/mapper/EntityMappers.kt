package com.example.my_car.data.local.mapper

import com.example.my_car.data.local.entity.MaintenanceEntity
import com.example.my_car.data.local.entity.VehicleEntity
import com.example.my_car.domain.model.MaintenanceHistoryItem
import com.example.my_car.domain.model.Vehicle
import com.example.my_car.domain.model.VehicleType

fun VehicleEntity.toDomain() = Vehicle(
    plate = plate,
    brand = brand,
    line = line,
    type = runCatching { VehicleType.valueOf(type) }.getOrElse { VehicleType.values().first() },
    year = year,
    currentMileage = currentMileage
)

fun Vehicle.toEntity() = VehicleEntity(
    plate = plate,
    brand = brand,
    line = line,
    type = type.name,
    year = year,
    currentMileage = currentMileage
)

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