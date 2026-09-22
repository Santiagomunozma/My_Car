package com.micarro.domain.model

import java.time.LocalDate

data class MileageRecord(
    val id: Long = 0L,
    val vehicleId: Long,
    val date: LocalDate,
    val reading: Int,
    val note: String? = null
)
