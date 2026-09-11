package com.example.my_car.domain.repository

import com.example.my_car.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    suspend fun getVehicleById(id: String): Vehicle?
    suspend fun createVehicle(vehicle: Vehicle)
    suspend fun updateVehicle(vehicle: Vehicle)
    suspend fun archiveVehicle(id: String)
    suspend fun reactivateVehicle(id: String)
    suspend fun isPlateAvailable(plate: String, excludingId: String?): Boolean
}