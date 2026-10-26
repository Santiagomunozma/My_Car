package com.micarro.feature.documents.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.micarro.domain.model.DocumentType
import com.micarro.domain.model.VehicleDocument
import com.micarro.feature.documents.domain.DocumentStatusRules

@Entity(
    tableName = "vehicle_documents",
    indices = [Index(value = ["vehicle_id"])]
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "vehicle_id") val vehicleId: Long,
    val type: String,
    val name: String,
    @ColumnInfo(name = "expiration_date") val expirationDate: Long,
    val issuer: String?,
    @ColumnInfo(name = "alerts_enabled") val alertsEnabled: Boolean,
    val notes: String?,
    @ColumnInfo(name = "reference_number") val referenceNumber: String? = null,
    @ColumnInfo(name = "file_path") val filePath: String? = null,
    @ColumnInfo(name = "mime_type") val mimeType: String? = null,
    @ColumnInfo(name = "reminded_month", defaultValue = "0") val remindedMonth: Boolean = false,
    @ColumnInfo(name = "reminded_fortnight", defaultValue = "0") val remindedFortnight: Boolean = false,
    @ColumnInfo(name = "reminded_expired", defaultValue = "0") val remindedExpired: Boolean = false
)

fun DocumentEntity.toDomain() = VehicleDocument(
    id = id,
    vehicleId = vehicleId,
    type = runCatching { DocumentType.valueOf(type) }.getOrDefault(DocumentType.OTHER),
    name = name,
    expirationDate = DocumentStatusRules.millisToDate(expirationDate),
    issuer = issuer,
    alertsEnabled = alertsEnabled,
    notes = notes,
    referenceNumber = referenceNumber,
    filePath = filePath,
    mimeType = mimeType,
    remindedMonth = remindedMonth,
    remindedFortnight = remindedFortnight,
    remindedExpired = remindedExpired
)

fun VehicleDocument.toEntity() = DocumentEntity(
    id = id,
    vehicleId = vehicleId,
    type = type.name,
    name = name,
    expirationDate = DocumentStatusRules.dateToMillis(expirationDate),
    issuer = issuer,
    alertsEnabled = alertsEnabled,
    notes = notes,
    referenceNumber = referenceNumber,
    filePath = filePath,
    mimeType = mimeType,
    remindedMonth = remindedMonth,
    remindedFortnight = remindedFortnight,
    remindedExpired = remindedExpired
)
