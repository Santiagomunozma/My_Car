package com.micarro.app.feature.documents.data

import com.micarro.app.domain.model.VehicleDocument
import com.micarro.app.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun observeDocuments(vehicleId: Long): Flow<List<VehicleDocument>> {
        return documentDao.observeDocuments(vehicleId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getDocumentById(id: Long): VehicleDocument? {
        return documentDao.getDocumentById(id)?.toDomain()
    }

    override suspend fun saveDocument(document: VehicleDocument): Long {
        return if (document.id == 0L) {
            documentDao.insertDocument(DocumentEntity.fromDomain(document))
        } else {
            documentDao.updateDocument(DocumentEntity.fromDomain(document))
            document.id
        }
    }

    override suspend fun deleteDocument(id: Long) {
        documentDao.deleteDocument(id)
    }

    override suspend fun setAlertsEnabled(id: Long, enabled: Boolean) {
        documentDao.setAlertsEnabled(id, enabled)
    }
}
