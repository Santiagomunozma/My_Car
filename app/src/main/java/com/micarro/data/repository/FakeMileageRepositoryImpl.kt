package com.micarro.data.repository

import com.micarro.domain.model.MileageReading
import com.micarro.domain.repository.MileageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeMileageRepositoryImpl @Inject constructor() : MileageRepository {
    override fun observeMileage(vehicleId: Long): Flow<List<MileageReading>> = flowOf(emptyList())
    override suspend fun addMileage(reading: MileageReading) {}
    override suspend fun getLatestMileage(vehicleId: Long): MileageReading? = null
}
