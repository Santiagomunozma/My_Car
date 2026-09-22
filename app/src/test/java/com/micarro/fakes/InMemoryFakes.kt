package com.micarro.fakes

import com.micarro.domain.alerts.MileageAlertNotifier
import com.micarro.domain.model.AlertSettings
import com.micarro.domain.model.MileageRecord
import com.micarro.domain.model.Vehicle
import com.micarro.domain.model.VehicleDocument
import com.micarro.domain.repository.AlertSettingsRepository
import com.micarro.domain.repository.DocumentRepository
import com.micarro.domain.repository.MileageRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.feature.vehicle.domain.VehiclePhotoStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.IOException

class InMemoryVehicleRepository : VehicleRepository {
    private val vehicles = MutableStateFlow<List<Vehicle>>(emptyList())

    override fun observeVehicles(): Flow<List<Vehicle>> =
        vehicles.map { list -> list.filter { !it.isArchived } }

    override fun observeAllVehicles(): Flow<List<Vehicle>> = vehicles

    override suspend fun getVehicleById(id: Long): Vehicle? =
        vehicles.value.firstOrNull { it.id == id }

    override suspend fun createVehicle(vehicle: Vehicle) {
        vehicles.update { it + vehicle }
    }

    override suspend fun updateVehicle(vehicle: Vehicle) {
        vehicles.update { list -> list.map { if (it.id == vehicle.id) vehicle else it } }
    }

    override suspend fun archiveVehicle(id: Long) {
        vehicles.update { list -> list.map { if (it.id == id) it.copy(isArchived = true) else it } }
    }

    override suspend fun reactivateVehicle(id: Long) {
        vehicles.update { list -> list.map { if (it.id == id) it.copy(isArchived = false) else it } }
    }

    override suspend fun isPlateAvailable(plate: String, excludingId: Long?): Boolean =
        vehicles.value.none { it.plate == plate && it.id != excludingId }

    override suspend fun setMainVehicle(id: Long) {
        vehicles.update { list ->
            list.map { it.copy(isMainVehicle = it.id == id) }
        }
    }

    override suspend fun updateCurrentMileage(id: Long, mileage: Int) {
        vehicles.update { list ->
            list.map { if (it.id == id) it.copy(currentMileage = mileage) else it }
        }
    }

    fun seed(vararg items: Vehicle) {
        vehicles.value = items.toList()
    }
}

class InMemoryMileageRepository : MileageRepository {
    private val readings = MutableStateFlow<List<MileageRecord>>(emptyList())

    override fun observeMileage(vehicleId: String): Flow<List<MileageRecord>> =
        readings.map { list ->
            list.filter { it.vehicleId == vehicleId }
                .sortedWith(compareByDescending<MileageRecord> { it.date }.thenByDescending { it.id })
        }

    override suspend fun addMileage(reading: MileageRecord) {
        readings.update { it + reading }
    }

    override suspend fun getLatestMileage(vehicleId: String): MileageRecord? =
        readings.value.filter { it.vehicleId == vehicleId }
            .maxWithOrNull(compareBy<MileageRecord> { it.date }.thenBy { it.id })
}

class InMemoryDocumentRepository : DocumentRepository {
    private val documents = MutableStateFlow<List<VehicleDocument>>(emptyList())

    override fun observeDocuments(vehicleId: String): Flow<List<VehicleDocument>> =
        documents.map { list ->
            list.filter { it.vehicleId == vehicleId }.sortedBy { it.expirationDate }
        }

    override fun observeAllDocuments(): Flow<List<VehicleDocument>> =
        documents.map { list -> list.sortedBy { it.expirationDate } }

    override suspend fun getDocumentById(id: String): VehicleDocument? =
        documents.value.firstOrNull { it.id == id }

    override suspend fun saveDocument(document: VehicleDocument) {
        documents.update { list -> list.filter { it.id != document.id } + document }
    }

    override suspend fun deleteDocument(id: String) {
        documents.update { list -> list.filter { it.id != id } }
    }

    override suspend fun setAlertsEnabled(id: String, enabled: Boolean) {
        documents.update { list ->
            list.map { if (it.id == id) it.copy(alertsEnabled = enabled) else it }
        }
    }

    fun seed(vararg items: VehicleDocument) {
        documents.value = items.toList()
    }
}

class InMemoryAlertSettingsRepository : AlertSettingsRepository {
    private val settings = MutableStateFlow(AlertSettings())

    override fun observeSettings(): Flow<AlertSettings> = settings

    override suspend fun setGlobalAlertsEnabled(enabled: Boolean) {
        settings.update { it.copy(globalAlertsEnabled = enabled) }
    }

    override suspend fun setAnticipationDays(days: Int) {
        settings.update { it.copy(anticipationDays = days) }
    }
}

class FakeVehiclePhotoStore : VehiclePhotoStore {
    val deleted = mutableListOf<String>()
    var importResult: String = "internal/photo.jpg"
    var failOnImport: Boolean = false
    var importCalls = 0

    override suspend fun importPhoto(sourceUri: String): String {
        importCalls++
        if (failOnImport) throw IOException("import failed")
        return importResult
    }

    override suspend fun deletePhoto(path: String) {
        deleted += path
    }
}

class RecordingMileageAlertNotifier : MileageAlertNotifier {
    val calls = mutableListOf<Pair<String, Int>>()
    override suspend fun onMileageUpdated(vehicleId: String, odometer: Int) {
        calls += vehicleId to odometer
    }
}
