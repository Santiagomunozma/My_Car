package com.example.my_car.data.local

import com.example.my_car.core.util.MockDataFactory
import com.example.my_car.domain.model.MaintenanceHistoryItem
import com.example.my_car.domain.model.Vehicle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor() {

    fun getInitialVehicles(): List<Vehicle> {
        return MockDataFactory.dummyVehicles
    }

    fun getInitialMaintenances(): List<MaintenanceHistoryItem> {
        return MockDataFactory.dummyMaintenances
    }
}