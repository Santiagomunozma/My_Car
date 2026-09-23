package com.micarro.domain.model

import java.time.LocalDate

enum class DocumentType {
    SOAT,
    TECHNICAL_INSPECTION,
    INSURANCE,
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
    val notes: String? = null
)
