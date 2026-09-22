package com.micarro.data.repository

import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeVehicleRepositoryImpl @Inject constructor() : VehicleRepository {
    override fun observeVehicles(): Flow<List<Vehicle>> = flowOf(emptyList())
    override fun observeAllVehicles(): Flow<List<Vehicle>> = flowOf(emptyList())
    override suspend fun getVehicleById(id: String): Vehicle? = null
    override suspend fun createVehicle(vehicle: Vehicle) {}
    override suspend fun updateVehicle(vehicle: Vehicle) {}
    override suspend fun archiveVehicle(id: String) {}
    override suspend fun reactivateVehicle(id: String) {}
    override suspend fun isPlateAvailable(plate: String, excludingId: String?): Boolean = true
    override suspend fun setMainVehicle(id: String) {}
    override suspend fun updateCurrentMileage(id: String, mileage: Int) {}
}
