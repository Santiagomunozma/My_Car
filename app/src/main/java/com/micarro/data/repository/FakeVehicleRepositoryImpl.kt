package com.micarro.data.repository

import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeVehicleRepositoryImpl @Inject constructor() : VehicleRepository {
    override fun observeVehicles(): Flow<List<Vehicle>> = flowOf(emptyList())
    override fun observeAllVehicles(): Flow<List<Vehicle>> = flowOf(emptyList())
    override suspend fun getVehicleById(id: Long): Vehicle? = null
    override suspend fun createVehicle(vehicle: Vehicle) {}
    override suspend fun updateVehicle(vehicle: Vehicle) {}
    override suspend fun archiveVehicle(id: Long) {}
    override suspend fun reactivateVehicle(id: Long) {}
    override suspend fun isPlateAvailable(plate: String, excludingId: Long?): Boolean = true
    override suspend fun setMainVehicle(id: Long) {}
    override suspend fun updateCurrentMileage(id: Long, mileage: Long) {}
}
