package com.micarro.feature.documents.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.micarro.domain.model.DocumentType
import com.micarro.domain.model.VehicleDocument

@Entity(
    tableName = "vehicle_documents",
    indices = [Index(value = ["vehicle_id"])]
)
data class DocumentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "vehicle_id") val vehicleId: String,
    val type: String,
    val name: String,
    @ColumnInfo(name = "expiration_date") val expirationDate: Long,
    val issuer: String?,
    @ColumnInfo(name = "alerts_enabled") val alertsEnabled: Boolean,
    val notes: String?
)

fun DocumentEntity.toDomain() = VehicleDocument(
    id = id,
    vehicleId = vehicleId,
    type = runCatching { DocumentType.valueOf(type) }.getOrDefault(DocumentType.OTHER),
    name = name,
    expirationDate = expirationDate,
    issuer = issuer,
    alertsEnabled = alertsEnabled,
    notes = notes
)

fun VehicleDocument.toEntity() = DocumentEntity(
    id = id,
    vehicleId = vehicleId,
    type = type.name,
    name = name,
    expirationDate = expirationDate,
    issuer = issuer,
    alertsEnabled = alertsEnabled,
    notes = notes
)
