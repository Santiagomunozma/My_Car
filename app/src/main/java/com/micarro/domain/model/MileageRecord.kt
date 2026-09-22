package com.micarro.domain.model

data class MileageRecord(
    val id: Long = 0L,
    val vehicleId: Long,
    val date: Long, // Representado en Milisegundos (EpochMillis)
    val reading: Int,
    val note: String? = null
)
