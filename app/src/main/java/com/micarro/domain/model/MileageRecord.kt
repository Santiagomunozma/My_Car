package com.micarro.domain.model

import java.util.UUID

data class MileageRecord(
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val date: Long, // Representado en Milisegundos (EpochMillis)
    val reading: Int,
    val note: String? = null
)
