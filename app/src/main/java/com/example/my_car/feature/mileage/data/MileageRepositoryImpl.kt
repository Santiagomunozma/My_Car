package com.example.my_car.feature.mileage.data

import com.example.my_car.domain.model.MileageRecord
import com.example.my_car.domain.repository.MileageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MileageRepositoryImpl @Inject constructor(
    private val dao: MileageDao
) : MileageRepository {

    override fun observeMileage(vehicleId: String): Flow<List<MileageRecord>> =
        dao.observeByVehicle(vehicleId).map { list -> list.map(MileageEntity::toDomain) }

    override suspend fun addMileage(reading: MileageRecord) = dao.insert(reading.toEntity())

    override suspend fun getLatestMileage(vehicleId: String): MileageRecord? =
        dao.getLatest(vehicleId)?.toDomain()
}
