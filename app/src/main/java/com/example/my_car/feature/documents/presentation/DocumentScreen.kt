package com.example.my_car.feature.documents.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.my_car.R
import com.example.my_car.domain.model.DocumentStatus
import com.example.my_car.domain.model.DocumentType
import com.example.my_car.domain.model.VehicleDocument
import com.example.my_car.feature.documents.domain.DocumentDraft
import com.example.my_car.feature.documents.domain.DocumentField
import com.example.my_car.feature.documents.domain.DocumentWithStatus
import com.example.my_car.ui.components.DateField
import com.example.my_car.ui.components.EmptyState
import com.example.my_car.ui.components.MiCarroCard
import com.example.my_car.ui.components.MiCarroTextField
import com.example.my_car.ui.components.PrimaryButton
import com.example.my_car.ui.components.StatusChip
import com.example.my_car.ui.components.formatUtcMillis
import com.example.my_car.ui.theme.StatusError
import com.example.my_car.ui.theme.StatusSuccess
import com.example.my_car.ui.theme.StatusWarning
import com.example.my_car.ui.theme.TextSecondary

fun documentTypeLabel(type: DocumentType): Int = when (type) {
    DocumentType.SOAT -> R.string.document_type_soat
    DocumentType.TECHNICAL_INSPECTION -> R.string.document_type_technical
    DocumentType.INSURANCE -> R.string.document_type_insurance
    DocumentType.OTHER -> R.string.document_type_other
}

@Composable
fun documentDisplayName(type: DocumentType, name: String): String =
    name.ifBlank { stringResource(documentTypeLabel(type)) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    onBack: () -> Unit,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.vehicle?.let {
                            stringResource(R.string.documents_of_vehicle, it.plate)
                        } ?: stringResource(R.string.documents_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openAddForm) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.documents_add)
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.documents.isEmpty() -> EmptyState(
                icon = Icons.Filled.Description,
                title = stringResource(R.string.documents_empty_title),
                message = stringResource(R.string.documents_empty_message),
                modifier = Modifier.padding(padding)
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.documents, key = { it.document.id }) { item ->
                    DocumentCard(
                        item = item,
                        onEdit = { viewModel.openEditForm(item.document) },
                        onDelete = { viewModel.requestDelete(item.document) },
                        onToggleAlerts = { enabled ->
                            viewModel.setAlertsEnabled(item.document, enabled)
                        }
                    )
                }
            }
        }
    }

    if (state.showForm) {
        DocumentFormDialog(
            editing = state.editingDocument,
            vehicleId = state.vehicle?.id.orEmpty(),
            isSaving = state.isSaving,
            saveFailed = state.saveFailed,
            errors = state.formErrors,
            onDismiss = viewModel::dismissForm,
            onSave = viewModel::save
        )
    }

    state.documentToDelete?.let {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text(stringResource(R.string.document_delete_confirm_title)) },
            text = { Text(stringResource(R.string.document_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun DocumentCard(
    item: DocumentWithStatus,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAlerts: (Boolean) -> Unit
) {
    val (statusText, statusColor, statusIcon) = when (item.status) {
        DocumentStatus.UP_TO_DATE -> Triple(
            stringResource(R.string.doc_status_valid), StatusSuccess, Icons.Filled.CheckCircle
        )
        DocumentStatus.UPCOMING -> Triple(
            stringResource(R.string.doc_status_expiring), StatusWarning, Icons.Filled.Warning
        )
        DocumentStatus.EXPIRED -> Triple(
            stringResource(R.string.doc_status_expired), StatusError, Icons.Filled.Error
        )
    }

    MiCarroCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        documentDisplayName(item.document.type, item.document.name),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        formatUtcMillis(item.document.expirationDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                StatusChip(text = statusText, containerColor = statusColor, icon = statusIcon)
            }

            item.document.issuer?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.document_alerts_switch),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = item.document.alertsEnabled,
                        onCheckedChange = onToggleAlerts
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.action_edit)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = StatusError
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentFormDialog(
    editing: VehicleDocument?,
    vehicleId: String,
    isSaving: Boolean,
    saveFailed: Boolean,
    errors: Map<DocumentField, com.example.my_car.feature.documents.domain.DocumentError>,
    onDismiss: () -> Unit,
    onSave: (DocumentDraft) -> Unit
) {
    var type by remember { mutableStateOf(editing?.type ?: DocumentType.SOAT) }
    var name by remember { mutableStateOf(editing?.name.orEmpty()) }
    var expiration by remember { mutableStateOf(editing?.expirationDate) }
    var issuer by remember { mutableStateOf(editing?.issuer.orEmpty()) }
    var notes by remember { mutableStateOf(editing?.notes.orEmpty()) }
    var alertsEnabled by remember { mutableStateOf(editing?.alertsEnabled ?: true) }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (editing != null) R.string.documents_edit else R.string.documents_add
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { typeExpanded = true },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(documentTypeLabel(type)))
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        DocumentType.entries.forEach { option ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(documentTypeLabel(option))) },
                                onClick = {
                                    type = option
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                MiCarroTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.document_name),
                    errorMessage = if (errors.containsKey(DocumentField.NAME))
                        stringResource(R.string.error_required) else null
                )
                DateField(
                    label = stringResource(R.string.document_expiration),
                    selectedDateMillis = expiration,
                    onDateSelected = { expiration = it },
                    errorMessage = if (errors.containsKey(DocumentField.EXPIRATION_DATE))
                        stringResource(R.string.error_required) else null
                )
                MiCarroTextField(
                    value = issuer,
                    onValueChange = { issuer = it },
                    label = stringResource(R.string.document_issuer)
                )
                MiCarroTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = stringResource(R.string.document_notes),
                    singleLine = false
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.document_alerts_switch))
                    Switch(checked = alertsEnabled, onCheckedChange = { alertsEnabled = it })
                }
                if (saveFailed) {
                    Text(
                        stringResource(R.string.error_save_failed),
                        color = StatusError,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    onSave(
                        DocumentDraft(
                            id = editing?.id,
                            vehicleId = vehicleId,
                            type = type,
                            name = name,
                            expirationDate = expiration,
                            issuer = issuer,
                            alertsEnabled = alertsEnabled,
                            notes = notes
                        )
                    )
                },
                enabled = !isSaving
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
