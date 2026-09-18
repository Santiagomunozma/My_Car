package com.example.my_car.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.my_car.feature.documents.data.DocumentDao
import com.example.my_car.feature.documents.data.DocumentEntity
import com.example.my_car.feature.mileage.data.MileageDao
import com.example.my_car.feature.mileage.data.MileageEntity
import com.example.my_car.feature.vehicle.data.VehicleDao
import com.example.my_car.feature.vehicle.data.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        MileageEntity::class,
        DocumentEntity::class
        // El módulo de mantenimiento (Compañero 2) registra aquí sus entidades:
        // MaintenancePlanEntity, MaintenanceServiceEntity, PartEntity
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun mileageDao(): MileageDao
    abstract fun documentDao(): DocumentDao
}
