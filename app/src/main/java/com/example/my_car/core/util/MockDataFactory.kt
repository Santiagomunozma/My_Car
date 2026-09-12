package com.example.my_car.core.util

import com.example.my_car.domain.model.MaintenanceHistoryItem
import com.example.my_car.domain.model.Vehicle
import com.example.my_car.domain.model.VehicleType

object MockDataFactory {
    val dummyVehicles = listOf(
        Vehicle(
            plate = "ABC-123",
            brand = "Mazda",
            line = "Mazda 3",
            type = VehicleType.CAR,
            year = 2021,
            currentMileage = 45000
        ),
        Vehicle(
            plate = "XYZ-789",
            brand = "Yamaha",
            line = "MT-07",
            type = VehicleType.MOTORCYCLE,
            year = 2023,
            currentMileage = 12000
        )
    )

    val dummyMaintenances = listOf(
        MaintenanceHistoryItem(
            id = "1",
            vehiclePlate = "ABC-123",
            title = "Cambio de aceite y filtro",
            category = "Preventivo",
            date = System.currentTimeMillis() - (86400000L * 30),
            mileage = 40000,
            totalCost = 180000.0,
            workshopName = "Taller Central"
        ),
        MaintenanceHistoryItem(
            id = "2",
            vehiclePlate = "ABC-123",
            title = "Alineación y balanceo",
            category = "Mantenimiento",
            date = System.currentTimeMillis() - (86400000L * 10),
            mileage = 44500,
            totalCost = 120000.0,
            workshopName = "TecniAutos"
        )
    )
}