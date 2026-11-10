package com.micarro.domain.usecase

import com.micarro.core.database.AppDatabase
import com.micarro.feature.documents.domain.DocumentFileStore
import com.micarro.feature.maintenance.domain.ServiceEvidenceStore
import com.micarro.feature.vehicle.domain.VehiclePhotoStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val appDatabase: AppDatabase,
    private val photoStore: VehiclePhotoStore,
    private val evidenceStore: ServiceEvidenceStore,
    private val documentFileStore: DocumentFileStore
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        appDatabase.clearAllTables()
        photoStore.deleteAll()
        evidenceStore.deleteAll()
        documentFileStore.deleteAll()
    }
}