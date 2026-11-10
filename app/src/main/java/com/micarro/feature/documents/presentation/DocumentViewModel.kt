package com.micarro.feature.documents.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.Vehicle
import com.micarro.domain.model.VehicleDocument
import com.micarro.feature.documents.domain.DeleteDocumentUseCase
import com.micarro.feature.documents.domain.DocumentDraft
import com.micarro.feature.documents.domain.DocumentError
import com.micarro.feature.documents.domain.DocumentExtraction
import com.micarro.feature.documents.domain.DocumentField
import com.micarro.feature.documents.domain.DocumentFileStore
import com.micarro.feature.documents.domain.DocumentFileTooLarge
import com.micarro.feature.documents.domain.DocumentReader
import com.micarro.feature.documents.domain.DocumentWithStatus
import com.micarro.feature.documents.domain.ObserveVehicleDocumentsUseCase
import com.micarro.feature.documents.domain.PlateMatch
import com.micarro.feature.documents.domain.SaveDocumentResult
import com.micarro.feature.documents.domain.SaveDocumentUseCase
import com.micarro.feature.documents.domain.ToggleDocumentAlertsUseCase
import com.micarro.feature.vehicle.domain.GetVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DocumentReadProblem {
    MISSING_KEY,
    FAILED,
    TOO_LARGE
}

data class DocumentScan(
    val path: String,
    val mimeType: String,
    val extraction: DocumentExtraction?
)

data class DocumentUiState(
    val vehicle: Vehicle? = null,
    val documents: List<DocumentWithStatus> = emptyList(),
    val showForm: Boolean = false,
    val editingDocument: VehicleDocument? = null,
    val formErrors: Map<DocumentField, DocumentError> = emptyMap(),
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
    val documentToDelete: VehicleDocument? = null,
    val isLoading: Boolean = true,
    val isReading: Boolean = false,
    val readProblem: DocumentReadProblem? = null,
    val scan: DocumentScan? = null,
    val scanToken: Int = 0
)

@HiltViewModel
class DocumentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDocuments: ObserveVehicleDocumentsUseCase,
    private val getVehicle: GetVehicleUseCase,
    private val saveDocument: SaveDocumentUseCase,
    private val deleteDocument: DeleteDocumentUseCase,
    private val toggleAlerts: ToggleDocumentAlertsUseCase,
    private val fileStore: DocumentFileStore,
    private val reader: DocumentReader
) : ViewModel() {

    private val routeVehicleId: String = checkNotNull(savedStateHandle["vehicleId"])
    private val vehicleId: Long = routeVehicleId.toLongOrNull() ?: -1L

    private val vehicle = MutableStateFlow<Vehicle?>(null)
    private val formState = MutableStateFlow(FormState())

    data class FormState(
        val showForm: Boolean = false,
        val editingDocument: VehicleDocument? = null,
        val errors: Map<DocumentField, DocumentError> = emptyMap(),
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false,
        val documentToDelete: VehicleDocument? = null,
        val isReading: Boolean = false,
        val readProblem: DocumentReadProblem? = null,
        val scan: DocumentScan? = null,
        val scanToken: Int = 0
    )

    val uiState: StateFlow<DocumentUiState> = combine(
        vehicle,
        observeDocuments(vehicleId),
        formState
    ) { v, docs, f ->
        DocumentUiState(
            vehicle = v,
            documents = docs,
            showForm = f.showForm,
            editingDocument = f.editingDocument,
            formErrors = f.errors,
            isSaving = f.isSaving,
            saveFailed = f.saveFailed,
            documentToDelete = f.documentToDelete,
            isLoading = false,
            isReading = f.isReading,
            readProblem = f.readProblem,
            scan = f.scan,
            scanToken = f.scanToken
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DocumentUiState())

    init {
        viewModelScope.launch { vehicle.value = getVehicle(vehicleId) }
    }

    fun openAddForm() {
        formState.update { FormState(showForm = true) }
    }

    fun openEditForm(document: VehicleDocument) {
        formState.update { FormState(showForm = true, editingDocument = document) }
    }

    fun dismissForm() {
        val pending = formState.value.scan?.path
        val kept = formState.value.editingDocument?.filePath
        formState.update { FormState() }
        if (pending != null && pending != kept) {
            viewModelScope.launch { fileStore.delete(pending) }
        }
    }

    fun createCameraFile(): String = fileStore.createCameraFile()

    fun attachFromUri(uri: String) {
        import { fileStore.importUri(uri) }
    }

    fun attachFromCamera(path: String) {
        import { fileStore.importLocalFile(path, "image/jpeg") }
    }

    fun save(draft: DocumentDraft) {
        if (formState.value.isSaving || formState.value.isReading) return
        val plate = vehicle.value?.plate.orEmpty()
        if (!draft.plateAcknowledged && !PlateMatch.matches(draft.extractedPlate, plate)) {
            formState.update {
                it.copy(errors = mapOf(DocumentField.PLATE to DocumentError.MISMATCH), saveFailed = false)
            }
            return
        }
        formState.update { it.copy(isSaving = true, saveFailed = false, errors = emptyMap()) }
        viewModelScope.launch {
            when (val result = saveDocument(draft)) {
                SaveDocumentResult.Success -> formState.update { FormState() }
                is SaveDocumentResult.Invalid -> formState.update {
                    it.copy(isSaving = false, errors = result.errors)
                }
                is SaveDocumentResult.Failure -> formState.update {
                    it.copy(isSaving = false, saveFailed = true)
                }
            }
        }
    }

    fun requestDelete(document: VehicleDocument) {
        formState.update { it.copy(documentToDelete = document) }
    }

    fun dismissDelete() {
        formState.update { it.copy(documentToDelete = null) }
    }

    fun confirmDelete() {
        val doc = formState.value.documentToDelete ?: return
        formState.update { it.copy(documentToDelete = null) }
        viewModelScope.launch { deleteDocument(doc.id) }
    }

    fun setAlertsEnabled(document: VehicleDocument, enabled: Boolean) {
        viewModelScope.launch { toggleAlerts(document.id, enabled) }
    }

    private fun import(load: suspend () -> com.micarro.feature.documents.domain.StoredDocumentFile) {
        if (formState.value.isReading) return
        viewModelScope.launch {
            formState.update { it.copy(isReading = true, readProblem = null) }
            val stored = try {
                load()
            } catch (_: DocumentFileTooLarge) {
                formState.update {
                    it.copy(isReading = false, readProblem = DocumentReadProblem.TOO_LARGE)
                }
                return@launch
            } catch (_: Exception) {
                formState.update {
                    it.copy(isReading = false, readProblem = DocumentReadProblem.FAILED)
                }
                return@launch
            }
            discardPendingFile(except = stored.path)
            if (!reader.isConfigured) {
                publishScan(stored.path, stored.mimeType, extraction = null, DocumentReadProblem.MISSING_KEY)
                return@launch
            }
            val extraction = runCatching { reader.read(stored.path, stored.mimeType) }.getOrNull()
            publishScan(
                path = stored.path,
                mimeType = stored.mimeType,
                extraction = extraction,
                problem = if (extraction == null) DocumentReadProblem.FAILED else null
            )
        }
    }

    private fun publishScan(
        path: String,
        mimeType: String,
        extraction: DocumentExtraction?,
        problem: DocumentReadProblem?
    ) {
        formState.update {
            it.copy(
                isReading = false,
                readProblem = problem,
                scan = DocumentScan(path, mimeType, extraction),
                scanToken = it.scanToken + 1
            )
        }
    }

    private suspend fun discardPendingFile(except: String) {
        val pending = formState.value.scan?.path ?: return
        val kept = formState.value.editingDocument?.filePath
        if (pending != except && pending != kept) fileStore.delete(pending)
    }
}
