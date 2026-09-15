package com.micarro.app

import com.micarro.app.domain.model.MileageReading
import com.micarro.app.domain.model.Vehicle
import com.micarro.app.domain.model.VehicleDocument
import com.micarro.app.domain.repository.DocumentRepository
import com.micarro.app.domain.repository.MileageRepository
import com.micarro.app.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeVehicleRepository : VehicleRepository {
    val vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    private var nextId = 1L

    override fun observeVehicles(): Flow<List<Vehicle>> =
        vehicles.map { list -> list.filter { !it.isArchived } }

    override fun observeAllVehicles(): Flow<List<Vehicle>> = vehicles

    override suspend fun getVehicleById(id: Long): Vehicle? =
        vehicles.value.firstOrNull { it.id == id }

    override suspend fun createVehicle(vehicle: Vehicle): Long {
        val id = nextId++
        vehicles.value = vehicles.value + vehicle.copy(id = id)
        if (vehicle.isPrimary) clearOtherPrimary(id)
        return id
    }

    override suspend fun updateVehicle(vehicle: Vehicle) {
        vehicles.value = vehicles.value.map { if (it.id == vehicle.id) vehicle else it }
        if (vehicle.isPrimary) clearOtherPrimary(vehicle.id)
    }

    private fun clearOtherPrimary(primaryId: Long) {
        vehicles.value = vehicles.value.map {
            if (it.id != primaryId) it.copy(isPrimary = false) else it
        }
    }

    override suspend fun archiveVehicle(id: Long) {
        vehicles.value = vehicles.value.map {
            if (it.id == id) it.copy(isArchived = true) else it
        }
    }

    override suspend fun reactivateVehicle(id: Long) {
        vehicles.value = vehicles.value.map {
            if (it.id == id) it.copy(isArchived = false) else it
        }
    }

    override suspend fun isPlateAvailable(plate: String, excludingId: Long?): Boolean =
        vehicles.value.none { it.plate == plate.uppercase().trim() && it.id != excludingId }

    override suspend fun updateCurrentMileage(vehicleId: Long, mileage: Long) {
        vehicles.value = vehicles.value.map {
            if (it.id == vehicleId) it.copy(currentMileage = mileage) else it
        }
    }
}

class FakeMileageRepository : MileageRepository {
    val readings = MutableStateFlow<List<MileageReading>>(emptyList())
    private var nextId = 1L

    override fun observeMileage(vehicleId: Long): Flow<List<MileageReading>> =
        readings.map { list ->
            list.filter { it.vehicleId == vehicleId }
                .sortedWith(compareByDescending<MileageReading> { it.dateEpochMs }.thenByDescending { it.id })
        }

    override suspend fun addMileage(reading: MileageReading): Long {
        val id = nextId++
        readings.value = readings.value + reading.copy(id = id)
        return id
    }

    override suspend fun getLatestMileage(vehicleId: Long): MileageReading? =
        readings.value.filter { it.vehicleId == vehicleId }
            .maxWithOrNull(compareBy<MileageReading> { it.dateEpochMs }.thenBy { it.id })
}

class FakeDocumentRepository : DocumentRepository {
    val documents = MutableStateFlow<List<VehicleDocument>>(emptyList())
    private var nextId = 1L

    override fun observeDocuments(vehicleId: Long): Flow<List<VehicleDocument>> =
        documents.map { list -> list.filter { it.vehicleId == vehicleId } }

    override suspend fun getDocumentById(id: Long): VehicleDocument? =
        documents.value.firstOrNull { it.id == id }

    override suspend fun saveDocument(document: VehicleDocument): Long {
        return if (document.id == 0L) {
            val id = nextId++
            documents.value = documents.value + document.copy(id = id)
            id
        } else {
            documents.value = documents.value.map {
                if (it.id == document.id) document else it
            }
            document.id
        }
    }

    override suspend fun deleteDocument(id: Long) {
        documents.value = documents.value.filter { it.id != id }
    }

    override suspend fun setAlertsEnabled(id: Long, enabled: Boolean) {
        documents.value = documents.value.map {
            if (it.id == id) it.copy(alertsEnabled = enabled) else it
        }
    }
}
