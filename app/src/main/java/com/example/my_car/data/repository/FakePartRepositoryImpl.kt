package com.example.my_car.data.repository

import com.example.my_car.domain.model.Part
import com.example.my_car.domain.repository.PartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakePartRepositoryImpl @Inject constructor() : PartRepository {
    override suspend fun getPartsForService(serviceId: String): List<Part> = emptyList()
    override suspend fun saveParts(parts: List<Part>) {}
    override fun observeInstalledParts(vehicleId: String): Flow<List<Part>> = flowOf(emptyList())
}
