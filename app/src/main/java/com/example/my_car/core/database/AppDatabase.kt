package com.example.my_car.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

// Por ahora está vacío de entidades, las iremos agregando cuando los compañeros hagan sus PRs.
@Database(
    entities = [
        // Ej: VehicleEntity::class, MileageEntity::class (Se agregarán luego)
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Aquí registraremos los DAOs de los compañeros:
    // abstract fun vehicleDao(): VehicleDao
    // abstract fun mileageDao(): MileageDao
}