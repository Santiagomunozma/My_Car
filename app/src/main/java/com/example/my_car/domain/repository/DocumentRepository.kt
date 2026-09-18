package com.example.my_car.domain.repository

import com.example.my_car.domain.model.VehicleDocument
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeDocuments(vehicleId: String): Flow<List<VehicleDocument>>
    fun observeAllDocuments(): Flow<List<VehicleDocument>>
    suspend fun getDocumentById(id: String): VehicleDocument?
    suspend fun saveDocument(document: VehicleDocument)
    suspend fun deleteDocument(id: String)
    suspend fun setAlertsEnabled(id: String, enabled: Boolean)
}
