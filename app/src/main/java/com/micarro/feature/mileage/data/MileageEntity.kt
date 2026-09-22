package com.micarro.feature.mileage.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.micarro.domain.model.MileageRecord
import com.micarro.feature.mileage.domain.MileageRules

@Entity(
    tableName = "mileage_readings",
    indices = [Index(value = ["vehicle_id"])]
)
data class MileageEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "vehicle_id") val vehicleId: Long,
    val date: Long,
    val reading: Long,
    val note: String?
)

fun MileageEntity.toDomain() = MileageRecord(
    id = id,
    vehicleId = vehicleId,
    date = MileageRules.millisToDate(date),
    reading = reading,
    note = note
)

fun MileageRecord.toEntity() = MileageEntity(
    id = id,
    vehicleId = vehicleId,
    date = MileageRules.dateToMillis(date),
    reading = reading,
    note = note
)
