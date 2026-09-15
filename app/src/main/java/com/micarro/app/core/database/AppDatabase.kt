package com.micarro.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.micarro.app.feature.documents.data.DocumentDao
import com.micarro.app.feature.documents.data.DocumentEntity
import com.micarro.app.feature.mileage.data.MileageDao
import com.micarro.app.feature.mileage.data.MileageEntity
import com.micarro.app.feature.vehicle.data.VehicleDao
import com.micarro.app.feature.vehicle.data.VehicleEntity

@Database(
    entities = [VehicleEntity::class, MileageEntity::class, DocumentEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun mileageDao(): MileageDao
    abstract fun documentDao(): DocumentDao
}
