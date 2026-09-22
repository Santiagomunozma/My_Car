package com.micarro.feature.mileage.data

import com.micarro.domain.model.MileageReading
import com.micarro.domain.repository.MileageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MileageRepositoryImpl @Inject constructor(
    private val dao: MileageDao
) : MileageRepository {

    override fun observeMileage(vehicleId: Long): Flow<List<MileageReading>> =
        dao.observeByVehicle(vehicleId).map { list -> list.map(MileageEntity::toDomain) }

    override suspend fun addMileage(reading: MileageReading) = dao.insert(reading.toEntity())

    override suspend fun getLatestMileage(vehicleId: Long): MileageReading? =
        dao.getLatest(vehicleId)?.toDomain()
}
