package com.example.my_car.domain.model

import java.util.UUID

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
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val type: DocumentType,
    val name: String,
    val expirationDate: Long, // Medianoche UTC en EpochMillis
    val issuer: String? = null,
    val alertsEnabled: Boolean = true,
    val notes: String? = null
)
