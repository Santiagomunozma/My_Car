package com.micarro.data.local

import com.micarro.core.util.MockDataFactory
import com.micarro.domain.model.MaintenanceHistoryItem
import com.micarro.domain.model.Vehicle
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