package com.micarro.domain.repository

import com.micarro.domain.model.MileageReading
import kotlinx.coroutines.flow.Flow

interface MileageRepository {
    fun observeMileage(vehicleId: Long): Flow<List<MileageReading>> //[cite: 1]
    suspend fun addMileage(reading: MileageReading) //[cite: 1]
    suspend fun getLatestMileage(vehicleId: Long): MileageReading? //[cite: 1]
}