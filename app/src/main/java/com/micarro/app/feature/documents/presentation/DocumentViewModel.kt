package com.micarro.app.feature.documents.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.app.domain.model.DocumentStatus
import com.micarro.app.domain.model.DocumentType
import com.micarro.app.domain.model.VehicleDocument
import com.micarro.app.feature.documents.domain.DeleteDocumentUseCase
import com.micarro.app.feature.documents.domain.DocumentField
import com.micarro.app.feature.documents.domain.DocumentStatusUseCase
import com.micarro.app.feature.documents.domain.ObserveDocumentsUseCase
import com.micarro.app.feature.documents.domain.SaveDocumentResult
import com.micarro.app.feature.documents.domain.SaveDocumentUseCase
import com.micarro.app.feature.documents.domain.ToggleDocumentAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentItem(
    val document: VehicleDocument,
    val status: DocumentStatus
)

data class DocumentFormState(
    val visible: Boolean = false,
    val editingId: Long = 0L,
    val type: DocumentType = DocumentType.SOAT,
    val name: String = "",
    val expiryDateEpochMs: Long? = null,
    val notes: String = "",
    val alertsEnabled: Boolean = true,
    val errors: Set<DocumentField> = emptySet()
)

data class DocumentsUiState(
    val documents: List<DocumentItem> = emptyList(),
    val form: DocumentFormState = DocumentFormState(),
    val pendingDelete: VehicleDocument? = null,
    val loading: Boolean = true
)

@HiltViewModel
class DocumentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDocuments: ObserveDocumentsUseCase,
    private val documentStatus: DocumentStatusUseCase,
    private val saveDocument: SaveDocumentUseCase,
    private val deleteDocument: DeleteDocumentUseCase,
    private val toggleAlerts: ToggleDocumentAlertsUseCase
) : ViewModel() {

    private val vehicleId: Long = checkNotNull(savedStateHandle["vehicleId"])

    private val local = MutableStateFlow(DocumentsUiState(loading = false))

    val uiState: StateFlow<DocumentsUiState> = combine(
        observeDocuments(vehicleId),
        local
    ) { documents, l ->
        l.copy(
            documents = documents.map { DocumentItem(it, documentStatus(it.expiryDateEpochMs)) },
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DocumentsUiState())

    fun openForm(document: VehicleDocument? = null) {
        local.update {
            it.copy(
                form = if (document == null) {
                    DocumentFormState(visible = true)
                } else {
                    DocumentFormState(
                        visible = true,
                        editingId = document.id,
                        type = document.type,
                        name = document.name.orEmpty(),
                        expiryDateEpochMs = document.expiryDateEpochMs,
                        notes = document.notes.orEmpty(),
                        alertsEnabled = document.alertsEnabled
                    )
                }
            )
        }
    }

    fun closeForm() {
        local.update { it.copy(form = DocumentFormState()) }
    }

    fun updateForm(transform: (DocumentFormState) -> DocumentFormState) {
        local.update { it.copy(form = transform(it.form)) }
    }

    fun saveForm() {
        val f = local.value.form
        val document = VehicleDocument(
            id = f.editingId,
            vehicleId = vehicleId,
            type = f.type,
            name = f.name,
            expiryDateEpochMs = f.expiryDateEpochMs ?: 0L,
            notes = f.notes,
            alertsEnabled = f.alertsEnabled
        )
        viewModelScope.launch {
            when (val result = saveDocument(document)) {
                is SaveDocumentResult.Success -> closeForm()
                is SaveDocumentResult.Invalid -> local.update {
                    it.copy(form = it.form.copy(errors = result.errors.map { e -> e.field }.toSet()))
                }
            }
        }
    }

    fun requestDelete(document: VehicleDocument) {
        local.update { it.copy(pendingDelete = document) }
    }

    fun dismissDelete() {
        local.update { it.copy(pendingDelete = null) }
    }

    fun confirmDelete() {
        val document = local.value.pendingDelete ?: return
        local.update { it.copy(pendingDelete = null) }
        viewModelScope.launch { deleteDocument(document.id) }
    }

    fun toggleAlerts(documentId: Long, enabled: Boolean) {
        viewModelScope.launch { toggleAlerts(documentId, enabled) }
    }
}
