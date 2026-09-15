package com.micarro.app.feature.documents.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.micarro.app.domain.model.DocumentType
import com.micarro.app.domain.model.VehicleDocument
import com.micarro.app.feature.vehicle.data.VehicleEntity

@Entity(
    tableName = "vehicle_documents",
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
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val vehicleId: Long,
    val type: String,
    val name: String?,
    val expiryDateEpochMs: Long,
    val notes: String?,
    val alertsEnabled: Boolean
) {
    fun toDomain() = VehicleDocument(
        id = id,
        vehicleId = vehicleId,
        type = runCatching { DocumentType.valueOf(type) }.getOrDefault(DocumentType.OTHER),
        name = name,
        expiryDateEpochMs = expiryDateEpochMs,
        notes = notes,
        alertsEnabled = alertsEnabled
    )

    companion object {
        fun fromDomain(document: VehicleDocument) = DocumentEntity(
            id = document.id,
            vehicleId = document.vehicleId,
            type = document.type.name,
            name = document.name,
            expiryDateEpochMs = document.expiryDateEpochMs,
            notes = document.notes,
            alertsEnabled = document.alertsEnabled
        )
    }
}
