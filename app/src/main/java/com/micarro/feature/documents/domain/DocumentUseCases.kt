package com.micarro.feature.documents.domain

import com.micarro.domain.model.AlertSettings
import com.micarro.domain.model.DocumentStatus
import com.micarro.domain.model.DocumentType
import com.micarro.domain.model.Vehicle
import com.micarro.domain.model.VehicleDocument
import com.micarro.domain.repository.AlertSettingsRepository
import com.micarro.domain.repository.DocumentRepository
import com.micarro.domain.repository.VehicleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

enum class DocumentField {
    NAME, EXPIRATION_DATE
}

enum class DocumentError {
    REQUIRED
}

data class DocumentDraft(
    val id: String? = null,
    val vehicleId: String,
    val type: DocumentType = DocumentType.SOAT,
    val name: String = "",
    val expirationDate: Long? = null,
    val issuer: String = "",
    val alertsEnabled: Boolean = true,
    val notes: String = ""
)

sealed interface SaveDocumentResult {
    data object Success : SaveDocumentResult
    data class Invalid(val errors: Map<DocumentField, DocumentError>) : SaveDocumentResult
    data class Failure(val cause: Throwable) : SaveDocumentResult
}

data class DocumentWithStatus(
    val document: VehicleDocument,
    val status: DocumentStatus,
    val daysUntilExpiration: Long
)

data class DocumentAlert(
    val vehicleId: String,
    val vehiclePlate: String,
    val documentId: String,
    val documentType: DocumentType,
    val documentName: String,
    val status: DocumentStatus,
    val daysUntilExpiration: Long
)

private fun todayTicker(): Flow<LocalDate> = flow {
    while (true) {
        emit(LocalDate.now())
        delay(60_000)
    }
}.distinctUntilChanged()

class ObserveVehicleDocumentsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val settingsRepository: AlertSettingsRepository
) {
    operator fun invoke(vehicleId: String): Flow<List<DocumentWithStatus>> =
        combine(
            documentRepository.observeDocuments(vehicleId),
            settingsRepository.observeSettings(),
            todayTicker()
        ) { documents, settings, today ->
            documents.map { document ->
                DocumentWithStatus(
                    document = document,
                    status = DocumentStatusRules.statusFor(
                        document.expirationDate,
                        settings.anticipationDays,
                        today
                    ),
                    daysUntilExpiration = DocumentStatusRules.daysUntil(
                        document.expirationDate, today
                    )
                )
            }
        }.distinctUntilChanged()
}

class SaveDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(draft: DocumentDraft): SaveDocumentResult {
        val errors = mutableMapOf<DocumentField, DocumentError>()
        if (draft.expirationDate == null) {
            errors[DocumentField.EXPIRATION_DATE] = DocumentError.REQUIRED
        }
        if (draft.type == DocumentType.OTHER && draft.name.isBlank()) {
            errors[DocumentField.NAME] = DocumentError.REQUIRED
        }
        if (errors.isNotEmpty()) return SaveDocumentResult.Invalid(errors)

        return try {
            repository.saveDocument(
                VehicleDocument(
                    id = draft.id ?: UUID.randomUUID().toString(),
                    vehicleId = draft.vehicleId,
                    type = draft.type,
                    name = draft.name.trim(),
                    expirationDate = draft.expirationDate!!,
                    issuer = draft.issuer.trim().ifEmpty { null },
                    alertsEnabled = draft.alertsEnabled,
                    notes = draft.notes.trim().ifEmpty { null }
                )
            )
            SaveDocumentResult.Success
        } catch (e: Exception) {
            SaveDocumentResult.Failure(e)
        }
    }
}

class DeleteDocumentUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteDocument(id)
}

class ToggleDocumentAlertsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(id: String, enabled: Boolean) =
        repository.setAlertsEnabled(id, enabled)
}

class ObserveDocumentAlertsUseCase @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val vehicleRepository: VehicleRepository,
    private val settingsRepository: AlertSettingsRepository
) {
    operator fun invoke(): Flow<List<DocumentAlert>> =
        combine(
            documentRepository.observeAllDocuments(),
            vehicleRepository.observeAllVehicles(),
            settingsRepository.observeSettings(),
            todayTicker()
        ) { documents, vehicles, settings, today ->
            computeAlerts(documents, vehicles, settings, today)
        }.distinctUntilChanged()

    companion object {
        /** Puro y testeable: la bandeja incluye vehículos archivados. */
        fun computeAlerts(
            documents: List<VehicleDocument>,
            vehicles: List<Vehicle>,
            settings: AlertSettings,
            today: LocalDate
        ): List<DocumentAlert> {
            if (!settings.globalAlertsEnabled) return emptyList()
            val vehiclesById = vehicles.associateBy { it.id.toString() }
            return documents
                .filter { it.alertsEnabled }
                .mapNotNull { document ->
                    val vehicle = vehiclesById[document.vehicleId] ?: return@mapNotNull null
                    val status = DocumentStatusRules.statusFor(
                        document.expirationDate, settings.anticipationDays, today
                    )
                    if (status == DocumentStatus.UP_TO_DATE) return@mapNotNull null
                    DocumentAlert(
                        vehicleId = vehicle.id.toString(),
                        vehiclePlate = vehicle.plate,
                        documentId = document.id,
                        documentType = document.type,
                        documentName = document.name,
                        status = status,
                        daysUntilExpiration = DocumentStatusRules.daysUntil(
                            document.expirationDate, today
                        )
                    )
                }
                .sortedWith(
                    compareBy<DocumentAlert> { it.status != DocumentStatus.EXPIRED }
                        .thenBy { it.daysUntilExpiration }
                )
        }
    }
}
