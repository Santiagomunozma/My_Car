package com.micarro.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.micarro.feature.documents.data.DocumentDao
import com.micarro.feature.documents.data.DocumentEntity
import com.micarro.feature.maintenance.data.MaintenancePlanDao
import com.micarro.feature.maintenance.data.MaintenancePlanEntity
import com.micarro.feature.maintenance.data.MaintenanceServiceDao
import com.micarro.feature.maintenance.data.MaintenanceServiceEntity
import com.micarro.feature.mileage.data.MileageDao
import com.micarro.feature.mileage.data.MileageEntity
import com.micarro.feature.parts.data.PartDao
import com.micarro.feature.parts.data.PartEntity
import com.micarro.feature.vehicle.data.VehicleDao
import com.micarro.feature.vehicle.data.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        MileageEntity::class,
        DocumentEntity::class,
        MaintenancePlanEntity::class,
        MaintenanceServiceEntity::class,
        PartEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun mileageDao(): MileageDao
    abstract fun documentDao(): DocumentDao
    abstract fun maintenancePlanDao(): MaintenancePlanDao
    abstract fun maintenanceServiceDao(): MaintenanceServiceDao
    abstract fun partDao(): PartDao
}
