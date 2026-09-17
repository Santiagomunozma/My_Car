package com.example.my_car.domain.repository

import com.example.my_car.domain.model.Part
import kotlinx.coroutines.flow.Flow

interface PartRepository {
    suspend fun getPartsForService(serviceId: String): List<Part>
    suspend fun saveParts(parts: List<Part>)
    fun observeInstalledParts(vehicleId: String): Flow<List<Part>>
}
