package com.micarro.app.feature.vehicle.data

import com.micarro.app.domain.model.Vehicle
import com.micarro.app.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {

    override fun observeVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.observeActiveVehicles().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeAllVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.observeAllVehicles().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getVehicleById(id: Long): Vehicle? {
        return vehicleDao.getVehicleById(id)?.toDomain()
    }

    override suspend fun createVehicle(vehicle: Vehicle): Long {
        val id = vehicleDao.insertVehicle(VehicleEntity.fromDomain(vehicle))
        if (vehicle.isPrimary) {
            vehicleDao.clearOtherPrimaryVehicles(id)
        }
        return id
    }

    override suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.updateVehicle(VehicleEntity.fromDomain(vehicle))
        if (vehicle.isPrimary) {
            vehicleDao.clearOtherPrimaryVehicles(vehicle.id)
        }
    }

    override suspend fun archiveVehicle(id: Long) {
        vehicleDao.archiveVehicle(id)
    }

    override suspend fun reactivateVehicle(id: Long) {
        vehicleDao.reactivateVehicle(id)
    }

    override suspend fun isPlateAvailable(plate: String, excludingId: Long?): Boolean {
        return vehicleDao.countPlates(plate.uppercase().trim(), excludingId) == 0
    }

    override suspend fun updateCurrentMileage(vehicleId: Long, mileage: Long) {
        vehicleDao.updateMileage(vehicleId, mileage)
    }
}
