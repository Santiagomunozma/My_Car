package com.micarro.app.domain.repository

import com.micarro.app.domain.model.MaintenancePlan
import com.micarro.app.domain.model.MileageReading
import com.micarro.app.domain.model.Vehicle
import com.micarro.app.domain.model.VehicleDocument
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeVehicles(): Flow<List<Vehicle>>
    fun observeAllVehicles(): Flow<List<Vehicle>>
    suspend fun getVehicleById(id: Long): Vehicle?
    suspend fun createVehicle(vehicle: Vehicle): Long
    suspend fun updateVehicle(vehicle: Vehicle)
    suspend fun archiveVehicle(id: Long)
    suspend fun reactivateVehicle(id: Long)
    suspend fun isPlateAvailable(plate: String, excludingId: Long?): Boolean
    suspend fun updateCurrentMileage(vehicleId: Long, mileage: Long)
}

interface MileageRepository {
    fun observeMileage(vehicleId: Long): Flow<List<MileageReading>>
    suspend fun addMileage(reading: MileageReading): Long
    suspend fun getLatestMileage(vehicleId: Long): MileageReading?
}

interface DocumentRepository {
    fun observeDocuments(vehicleId: Long): Flow<List<VehicleDocument>>
    suspend fun getDocumentById(id: Long): VehicleDocument?
    suspend fun saveDocument(document: VehicleDocument): Long
    suspend fun deleteDocument(id: Long)
    suspend fun setAlertsEnabled(id: Long, enabled: Boolean)
}

interface MaintenanceRepository {
    fun observePlans(vehicleId: Long): Flow<List<MaintenancePlan>>
    suspend fun savePlan(plan: MaintenancePlan): Long
    suspend fun updatePlan(plan: MaintenancePlan)
    suspend fun deletePlanIfWithoutHistory(planId: Long)
}

interface AlertScheduler {
    suspend fun scheduleForActivity(planId: Long)
    suspend fun cancelForActivity(planId: Long)
    suspend fun postpone(alertId: Long, newDateEpochMs: Long)
}
