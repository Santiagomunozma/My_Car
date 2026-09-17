package com.example.my_car.feature.parts.data

import com.example.my_car.domain.model.Part
import com.example.my_car.domain.repository.PartRepository
import kotlinx.coroutines.flow.map

class PartRepositoryImpl(
    private val partDao: PartDao
) : PartRepository {
    override suspend fun getPartsForService(serviceId: String): List<Part> {
        return partDao.getPartsForService(serviceId).map { it.toDomain() }
    }

    override suspend fun saveParts(parts: List<Part>) {
        partDao.insertAll(parts.map { it.toEntity() })
    }

    override fun observeInstalledParts(vehicleId: String): kotlinx.coroutines.flow.Flow<List<Part>> {
        return partDao.observeInstalledParts(vehicleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
