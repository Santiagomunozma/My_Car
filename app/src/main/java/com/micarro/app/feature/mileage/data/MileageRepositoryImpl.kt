package com.micarro.app.feature.mileage.data

import com.micarro.app.domain.model.MileageReading
import com.micarro.app.domain.repository.MileageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MileageRepositoryImpl @Inject constructor(
    private val mileageDao: MileageDao
) : MileageRepository {

    override fun observeMileage(vehicleId: Long): Flow<List<MileageReading>> {
        return mileageDao.observeMileage(vehicleId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addMileage(reading: MileageReading): Long {
        return mileageDao.insertReading(MileageEntity.fromDomain(reading))
    }

    override suspend fun getLatestMileage(vehicleId: Long): MileageReading? {
        return mileageDao.getLatestReading(vehicleId)?.toDomain()
    }
}
