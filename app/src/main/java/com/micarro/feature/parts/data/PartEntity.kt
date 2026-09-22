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
    val cost: Double
)

fun PartEntity.toDomain() = Part(
    id = id,
    serviceId = serviceId,
    name = name,
    quantity = quantity,
    cost = cost
)

fun Part.toEntity() = PartEntity(
    id = id,
    serviceId = serviceId,
    name = name,
    quantity = quantity,
    cost = cost
)
