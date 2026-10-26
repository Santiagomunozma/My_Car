package com.micarro.domain.model

import java.time.LocalDate

enum class DocumentType {
    SOAT,
    TECHNICAL_INSPECTION,
    INSURANCE,
    TAX,
    TRAFFIC_LIGHT,
    OTHER
}

enum class DocumentStatus {
    EXPIRED,
    UPCOMING,
    UP_TO_DATE
}

data class VehicleDocument(
    val id: Long = 0L,
    val vehicleId: Long,
    val type: DocumentType,
    val name: String,
    val expirationDate: LocalDate,
    val issuer: String? = null,
    val alertsEnabled: Boolean = true,
    val notes: String? = null,
    val referenceNumber: String? = null,
    val filePath: String? = null,
    val mimeType: String? = null,
    val remindedMonth: Boolean = false,
    val remindedFortnight: Boolean = false,
    val remindedExpired: Boolean = false
)
