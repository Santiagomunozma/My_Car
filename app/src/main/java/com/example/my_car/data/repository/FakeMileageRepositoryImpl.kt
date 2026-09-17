package com.example.my_car.data.repository

import com.example.my_car.domain.model.MileageRecord
import com.example.my_car.domain.repository.MileageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeMileageRepositoryImpl @Inject constructor() : MileageRepository {
    override fun observeMileage(vehicleId: String): Flow<List<MileageRecord>> = flowOf(emptyList())
    override suspend fun addMileage(reading: MileageRecord) {}
    override suspend fun getLatestMileage(vehicleId: String): MileageRecord? = null
}
