package com.micarro.feature.mileage.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.micarro.domain.model.MileageRecord

@Entity(
    tableName = "mileage_readings",
    indices = [Index(value = ["vehicle_id"])]
)
data class MileageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "vehicle_id") val vehicleId: String,
    val date: Long,
    val reading: Int,
    val note: String?
)

fun MileageEntity.toDomain() = MileageRecord(
    id = id,
    vehicleId = vehicleId,
    date = date,
    reading = reading,
    note = note
)

fun MileageRecord.toEntity() = MileageEntity(
    id = id,
    vehicleId = vehicleId,
    date = date,
    reading = reading,
    note = note
)
