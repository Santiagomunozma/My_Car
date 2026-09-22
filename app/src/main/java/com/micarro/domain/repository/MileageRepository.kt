package com.micarro.domain.repository

import com.micarro.domain.model.MileageRecord
import kotlinx.coroutines.flow.Flow

interface MileageRepository {
    fun observeMileage(vehicleId: Long): Flow<List<MileageRecord>> //[cite: 1]
    suspend fun addMileage(reading: MileageRecord) //[cite: 1]
    suspend fun getLatestMileage(vehicleId: Long): MileageRecord? //[cite: 1]
}