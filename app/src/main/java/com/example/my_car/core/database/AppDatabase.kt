package com.example.my_car.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.my_car.feature.documents.data.DocumentDao
import com.example.my_car.feature.documents.data.DocumentEntity
import com.example.my_car.feature.maintenance.data.MaintenancePlanDao
import com.example.my_car.feature.maintenance.data.MaintenancePlanEntity
import com.example.my_car.feature.maintenance.data.MaintenanceServiceDao
import com.example.my_car.feature.maintenance.data.MaintenanceServiceEntity
import com.example.my_car.feature.mileage.data.MileageDao
import com.example.my_car.feature.mileage.data.MileageEntity
import com.example.my_car.feature.parts.data.PartDao
import com.example.my_car.feature.parts.data.PartEntity
import com.example.my_car.feature.vehicle.data.VehicleDao
import com.example.my_car.feature.vehicle.data.VehicleEntity

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
