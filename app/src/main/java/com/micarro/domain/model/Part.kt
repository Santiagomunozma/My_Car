package com.micarro.domain.model

import java.util.UUID

data class Part(
    val id: String = UUID.randomUUID().toString(),
    val serviceId: String,
    val name: String,
    val quantity: Int,
    val cost: Double,
    val brand: String? = null,
    val reference: String? = null,
    val provider: String? = null,
    val installationDate: Long? = null,
    val warranty: String? = null,
    val notes: String? = null,
    val originTitle: String? = null
)
