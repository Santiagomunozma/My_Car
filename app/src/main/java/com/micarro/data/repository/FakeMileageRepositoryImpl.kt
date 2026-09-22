package com.micarro.data.repository

import com.micarro.domain.model.MileageRecord
import com.micarro.domain.repository.MileageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeMileageRepositoryImpl @Inject constructor() : MileageRepository {
    override fun observeMileage(vehicleId: String): Flow<List<MileageRecord>> = flowOf(emptyList())
    override suspend fun addMileage(reading: MileageRecord) {}
    override suspend fun getLatestMileage(vehicleId: String): MileageRecord? = null
}
