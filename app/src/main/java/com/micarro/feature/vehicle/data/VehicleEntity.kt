package com.micarro.feature.vehicle.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    indices = [Index(value = ["plate"], unique = true)]
)
data class VehicleEntity(
    @PrimaryKey val id: Long,
    val plate: String,
    val type: String,
    val brand: String,
    val line: String,
    val model: String,
    val year: Int,
    @ColumnInfo(name = "current_mileage") val currentMileage: Long,
    val color: String?,
    val vin: String?,
    @ColumnInfo(name = "fuel_type") val fuelType: String?,
    @ColumnInfo(name = "engine_cc") val engineCc: Int?,
    @ColumnInfo(name = "photo_uri") val photoUri: String?,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean,
    @ColumnInfo(name = "is_primary") val isPrimary: Boolean
)
