package com.micarro.feature.vehicle.data

import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val dao: VehicleDao
) : VehicleRepository {

    override fun observeVehicles(): Flow<List<Vehicle>> =
        dao.observeActive().map { list -> list.map(VehicleEntity::toDomain) }

    override fun observeAllVehicles(): Flow<List<Vehicle>> =
        dao.observeAll().map { list -> list.map(VehicleEntity::toDomain) }

    override suspend fun getVehicleById(id: Long): Vehicle? = dao.getById(id)?.toDomain()

    override suspend fun createVehicle(vehicle: Vehicle) = dao.insert(vehicle.toEntity())

    override suspend fun updateVehicle(vehicle: Vehicle) = dao.update(vehicle.toEntity())

    override suspend fun archiveVehicle(id: Long) = dao.archive(id)

    override suspend fun reactivateVehicle(id: Long) = dao.reactivate(id)

    override suspend fun isPlateAvailable(plate: String, excludingId: Long?): Boolean =
        dao.countPlate(plate, excludingId ?: -1L) == 0

    override suspend fun setMainVehicle(id: Long) = dao.setMainVehicle(id)

    override suspend fun updateCurrentMileage(id: Long, mileage: Long) =
        dao.updateCurrentMileage(id, mileage)
}
