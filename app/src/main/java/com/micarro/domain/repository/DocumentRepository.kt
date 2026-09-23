package com.micarro.domain.repository

import com.micarro.domain.model.VehicleDocument
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeDocuments(vehicleId: Long): Flow<List<VehicleDocument>>
    fun observeAllDocuments(): Flow<List<VehicleDocument>>
    suspend fun getDocumentById(id: Long): VehicleDocument?
    suspend fun saveDocument(document: VehicleDocument)
    suspend fun deleteDocument(id: Long)
    suspend fun setAlertsEnabled(id: Long, enabled: Boolean)
}
