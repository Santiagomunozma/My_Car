package com.micarro.feature.documents.data

import com.micarro.domain.model.VehicleDocument
import com.micarro.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val dao: DocumentDao
) : DocumentRepository {

    override fun observeDocuments(vehicleId: String): Flow<List<VehicleDocument>> =
        dao.observeByVehicle(vehicleId).map { list -> list.map(DocumentEntity::toDomain) }

    override fun observeAllDocuments(): Flow<List<VehicleDocument>> =
        dao.observeAll().map { list -> list.map(DocumentEntity::toDomain) }

    override suspend fun getDocumentById(id: String): VehicleDocument? =
        dao.getById(id)?.toDomain()

    override suspend fun saveDocument(document: VehicleDocument) = dao.upsert(document.toEntity())

    override suspend fun deleteDocument(id: String) = dao.delete(id)

    override suspend fun setAlertsEnabled(id: String, enabled: Boolean) =
        dao.setAlertsEnabled(id, enabled)
}
