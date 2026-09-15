package com.micarro.app.feature.documents.domain

import com.micarro.app.domain.model.DocumentStatus
import com.micarro.app.domain.model.DocumentType
import com.micarro.app.domain.model.VehicleDocument
import com.micarro.app.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

enum class DocumentField { NAME, EXPIRY_DATE }
enum class DocumentError { REQUIRED, INVALID_VALUE }

data class DocumentFieldError(val field: DocumentField, val error: DocumentError)

sealed interface SaveDocumentResult {
    data class Success(val documentId: Long) : SaveDocumentResult
    data class Invalid(val errors: List<DocumentFieldError>) : SaveDocumentResult
}

class DocumentStatusUseCase @Inject constructor() {

    operator fun invoke(
        expiryDateEpochMs: Long,
        anticipationDays: Int = DEFAULT_ANTICIPATION_DAYS,
        today: LocalDate = LocalDate.now()
    ): DocumentStatus {
        val expiry = Instant.ofEpochMilli(expiryDateEpochMs)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        return when {
            expiry.isBefore(today) -> DocumentStatus.EXPIRED
            !expiry.isAfter(today.plusDays(anticipationDays.toLong())) -> DocumentStatus.EXPIRING_SOON
            else -> DocumentStatus.VALID
        }
    }

    companion object {
        const val DEFAULT_ANTICIPATION_DAYS = 30
    }
}

class ObserveDocumentsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(vehicleId: Long): Flow<List<VehicleDocument>> =
        repository.observeDocuments(vehicleId)
}

class SaveDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(document: VehicleDocument): SaveDocumentResult {
        val normalized = document.copy(
            name = document.name?.trim()?.ifBlank { null },
            notes = document.notes?.trim()?.ifBlank { null }
        )
        val errors = mutableListOf<DocumentFieldError>()
        if (normalized.type == DocumentType.OTHER && normalized.name == null) {
            errors += DocumentFieldError(DocumentField.NAME, DocumentError.REQUIRED)
        }
        if (normalized.expiryDateEpochMs <= 0L) {
            errors += DocumentFieldError(DocumentField.EXPIRY_DATE, DocumentError.INVALID_VALUE)
        }
        if (errors.isNotEmpty()) {
            return SaveDocumentResult.Invalid(errors)
        }
        return SaveDocumentResult.Success(repository.saveDocument(normalized))
    }
}

class DeleteDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(documentId: Long) = repository.deleteDocument(documentId)
}

class ToggleDocumentAlertsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(documentId: Long, enabled: Boolean) =
        repository.setAlertsEnabled(documentId, enabled)
}
