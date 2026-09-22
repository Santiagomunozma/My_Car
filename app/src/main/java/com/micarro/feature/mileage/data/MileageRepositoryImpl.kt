package com.micarro.feature.mileage.data

import com.micarro.domain.model.MileageRecord
import com.micarro.domain.repository.MileageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MileageRepositoryImpl @Inject constructor(
    private val dao: MileageDao
) : MileageRepository {

    override fun observeMileage(vehicleId: Long): Flow<List<MileageRecord>> =
        dao.observeByVehicle(vehicleId).map { list -> list.map(MileageEntity::toDomain) }

    override suspend fun addMileage(reading: MileageRecord) = dao.insert(reading.toEntity())

    override suspend fun getLatestMileage(vehicleId: Long): MileageRecord? =
        dao.getLatest(vehicleId)?.toDomain()
}
