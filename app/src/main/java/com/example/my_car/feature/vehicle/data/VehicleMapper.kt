package com.example.my_car.feature.vehicle.data

import com.example.my_car.domain.model.FuelType
import com.example.my_car.domain.model.Vehicle
import com.example.my_car.domain.model.VehicleType

fun VehicleEntity.toDomain() = Vehicle(
    id = id,
    plate = plate,
    type = runCatching { VehicleType.valueOf(type) }.getOrDefault(VehicleType.CAR),
    brand = brand,
    line = line,
    model = model,
    year = year,
    currentMileage = currentMileage,
    color = color,
    vin = vin,
    fuelType = fuelType?.let { runCatching { FuelType.valueOf(it) }.getOrNull() },
    engineCc = engineCc,
    photoUri = photoUri,
    isArchived = isArchived,
    isMainVehicle = isMainVehicle
)

fun Vehicle.toEntity() = VehicleEntity(
    id = id,
    plate = plate,
    type = type.name,
    brand = brand,
    line = line,
    model = model,
    year = year,
    currentMileage = currentMileage,
    color = color,
    vin = vin,
    fuelType = fuelType?.name,
    engineCc = engineCc,
    photoUri = photoUri,
    isArchived = isArchived,
    isMainVehicle = isMainVehicle
)
