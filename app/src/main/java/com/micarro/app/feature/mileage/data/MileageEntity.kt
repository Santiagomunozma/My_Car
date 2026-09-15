package com.micarro.app.feature.mileage.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.micarro.app.domain.model.MileageReading
import com.micarro.app.feature.vehicle.data.VehicleEntity

@Entity(
    tableName = "mileage_readings",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vehicleId"])]
)
data class MileageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val vehicleId: Long,
    val mileage: Long,
    val dateEpochMs: Long
) {
    fun toDomain() = MileageReading(
        id = id,
        vehicleId = vehicleId,
        mileage = mileage,
        dateEpochMs = dateEpochMs
    )

    companion object {
        fun fromDomain(reading: MileageReading) = MileageEntity(
            id = reading.id,
            vehicleId = reading.vehicleId,
            mileage = reading.mileage,
            dateEpochMs = reading.dateEpochMs
        )
    }
}
