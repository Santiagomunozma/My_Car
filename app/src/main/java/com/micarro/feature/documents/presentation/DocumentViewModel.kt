package com.micarro.feature.documents.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.Vehicle
import com.micarro.domain.model.VehicleDocument
import com.micarro.feature.documents.domain.DeleteDocumentUseCase
import com.micarro.feature.documents.domain.DocumentDraft
import com.micarro.feature.documents.domain.DocumentError
import com.micarro.feature.documents.domain.DocumentField
import com.micarro.feature.documents.domain.DocumentWithStatus
import com.micarro.feature.documents.domain.ObserveVehicleDocumentsUseCase
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

data class DocumentUiState(
    val vehicle: Vehicle? = null,
    val documents: List<DocumentWithStatus> = emptyList(),
    val showForm: Boolean = false,
    val editingDocument: VehicleDocument? = null,
    val formErrors: Map<DocumentField, DocumentError> = emptyMap(),
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
    val documentToDelete: VehicleDocument? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DocumentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDocuments: ObserveVehicleDocumentsUseCase,
    private val getVehicle: GetVehicleUseCase,
    private val saveDocument: SaveDocumentUseCase,
    private val deleteDocument: DeleteDocumentUseCase,
    private val toggleAlerts: ToggleDocumentAlertsUseCase
) : ViewModel() {

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val vehicle = MutableStateFlow<Vehicle?>(null)
    private val formState = MutableStateFlow(FormState())

    data class FormState(
        val showForm: Boolean = false,
        val editingDocument: VehicleDocument? = null,
        val errors: Map<DocumentField, DocumentError> = emptyMap(),
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false,
        val documentToDelete: VehicleDocument? = null
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
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DocumentUiState())

    init {
        viewModelScope.launch { vehicle.value = getVehicle(vehicleId) }
    }

    fun openAddForm() {
        formState.update {
            FormState(showForm = true, editingDocument = null)
        }
    }

    fun openEditForm(document: VehicleDocument) {
        formState.update {
            FormState(showForm = true, editingDocument = document)
        }
    }

    fun dismissForm() {
        formState.update { it.copy(showForm = false, editingDocument = null, errors = emptyMap()) }
    }

    fun save(draft: DocumentDraft) {
        if (formState.value.isSaving) return
        formState.update { it.copy(isSaving = true, saveFailed = false) }
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
}
