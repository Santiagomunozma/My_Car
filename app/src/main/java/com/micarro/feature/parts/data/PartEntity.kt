package com.micarro.feature.parts.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.micarro.domain.model.Part

@Entity(tableName = "parts")
data class PartEntity(
    @PrimaryKey val id: String,
    val serviceId: String,
    val name: String,
    val quantity: Int,
    val cost: Double,
    val brand: String? = null,
    val reference: String? = null,
    val provider: String? = null,
    val installationDate: Long? = null,
    val warranty: String? = null,
    val notes: String? = null
)

fun PartEntity.toDomain() = Part(
    id = id,
    serviceId = serviceId,
    name = name,
    quantity = quantity,
    cost = cost,
    brand = brand,
    reference = reference,
    provider = provider,
    installationDate = installationDate,
    warranty = warranty,
    notes = notes
)

fun Part.toEntity() = PartEntity(
    id = id,
    serviceId = serviceId,
    name = name,
    quantity = quantity,
    cost = cost,
    brand = brand,
    reference = reference,
    provider = provider,
    installationDate = installationDate,
    warranty = warranty,
    notes = notes
)
