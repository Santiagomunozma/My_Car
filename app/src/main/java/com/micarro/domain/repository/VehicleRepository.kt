package com.micarro.domain.repository

import com.micarro.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    fun observeAllVehicles(): Flow<List<Vehicle>>
    suspend fun getVehicleById(id: Long): Vehicle?
    suspend fun createVehicle(vehicle: Vehicle)
    suspend fun updateVehicle(vehicle: Vehicle)
    suspend fun archiveVehicle(id: Long)
    suspend fun reactivateVehicle(id: Long)
    suspend fun isPlateAvailable(plate: String, excludingId: Long?): Boolean
    suspend fun setMainVehicle(id: Long)
    suspend fun updateCurrentMileage(id: Long, mileage: Long)
}
