package com.example.my_car.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val plate: String,
    val brand: String,
    val line: String,
    val type: String,
    val year: Int,
    val currentMileage: Int
)