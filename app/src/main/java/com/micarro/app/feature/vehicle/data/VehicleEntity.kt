package com.micarro.app.feature.vehicle.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.micarro.app.domain.model.FuelType
import com.micarro.app.domain.model.Vehicle
import com.micarro.app.domain.model.VehicleType

@Entity(
    tableName = "vehicles",
    indices = [Index(value = ["plate"], unique = true)]
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val plate: String,
    val type: String,
    val brand: String,
    val line: String,
    val model: String,
    val year: Int,
    val currentMileage: Long,
    val color: String?,
    val vin: String?,
    val fuelType: String?,
    val engineCc: Int?,
    val photoUri: String?,
    val isPrimary: Boolean,
    val isArchived: Boolean
) {
    fun toDomain() = Vehicle(
        id = id,
        plate = plate,
        type = runCatching { VehicleType.valueOf(type) }.getOrDefault(VehicleType.CAR),
        brand = brand,
        line = line,
        model = model,
        year = year,
        currentMileage = currentMileage,
        color = color,
        vin = vin,
        fuelType = fuelType?.let { runCatching { FuelType.valueOf(it) }.getOrNull() },
        engineCc = engineCc,
        photoUri = photoUri,
        isPrimary = isPrimary,
        isArchived = isArchived
    )

    companion object {
        fun fromDomain(vehicle: Vehicle) = VehicleEntity(
            id = vehicle.id,
            plate = vehicle.plate.uppercase().trim(),
            type = vehicle.type.name,
            brand = vehicle.brand,
            line = vehicle.line,
            model = vehicle.model,
            year = vehicle.year,
            currentMileage = vehicle.currentMileage,
            color = vehicle.color,
            vin = vehicle.vin,
            fuelType = vehicle.fuelType?.name,
            engineCc = vehicle.engineCc,
            photoUri = vehicle.photoUri,
            isPrimary = vehicle.isPrimary,
            isArchived = vehicle.isArchived
        )
    }
}
