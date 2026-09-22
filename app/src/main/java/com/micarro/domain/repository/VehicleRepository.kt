package com.micarro.domain.repository

import com.micarro.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    fun observeAllVehicles(): Flow<List<Vehicle>>
    suspend fun getVehicleById(id: String): Vehicle?
    suspend fun createVehicle(vehicle: Vehicle)
    suspend fun updateVehicle(vehicle: Vehicle)
    suspend fun archiveVehicle(id: String)
    suspend fun reactivateVehicle(id: String)
    suspend fun isPlateAvailable(plate: String, excludingId: String?): Boolean
    suspend fun setMainVehicle(id: String)
    suspend fun updateCurrentMileage(id: String, mileage: Int)
}
