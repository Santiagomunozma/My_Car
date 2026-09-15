package com.micarro.app.feature.documents.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.micarro.app.R
import com.micarro.app.core.ui.components.DocumentStatusChip
import com.micarro.app.core.ui.components.EmptyState
import com.micarro.app.core.ui.theme.TextSecondary
import com.micarro.app.domain.model.DocumentType
import com.micarro.app.domain.model.VehicleDocument
import com.micarro.app.feature.documents.domain.DocumentField
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es"))

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
fun documentTypeLabel(type: DocumentType): String = when (type) {
    DocumentType.SOAT -> stringResource(R.string.doc_soat)
    DocumentType.TECHNICAL_REVIEW -> stringResource(R.string.doc_technical_review)
    DocumentType.INSURANCE -> stringResource(R.string.doc_insurance)
    DocumentType.OTHER -> stringResource(R.string.doc_other)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(
    onBack: () -> Unit,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.documents_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openForm() }) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.document_add)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!uiState.loading && uiState.documents.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Description,
                    title = stringResource(R.string.documents_empty_title),
                    message = stringResource(R.string.documents_empty_message)
                )
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.documents, key = { it.document.id }) { item ->
                        DocumentCard(
                            item = item,
                            onEdit = { viewModel.openForm(item.document) },
                            onDelete = { viewModel.requestDelete(item.document) },
                            onToggleAlerts = { enabled ->
                                viewModel.toggleAlerts(item.document.id, enabled)
                            }
                        )
                    }
                }
            }
        }
    }

    if (uiState.form.visible) {
        DocumentFormDialog(
            form = uiState.form,
            onDismiss = viewModel::closeForm,
            onUpdate = viewModel::updateForm,
            onSave = viewModel::saveForm
        )
    }

    uiState.pendingDelete?.let {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            shape = MaterialTheme.shapes.large,
            title = { Text(stringResource(R.string.document_delete_title)) },
            text = { Text(stringResource(R.string.document_delete_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text(
                        stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
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
    item: DocumentItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAlerts: (Boolean) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val document = item.document
    val expiry = document.expiryDateEpochMs.toLocalDate()
    val days = ChronoUnit.DAYS.between(LocalDate.now(), expiry)
    val relative = when {
        days < 0 -> stringResource(R.string.doc_expired_since, -days)
        days == 0L -> stringResource(R.string.doc_expires_today)
        else -> stringResource(R.string.doc_days_remaining, days)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (document.type == DocumentType.OTHER && document.name != null) {
                            document.name
                        } else {
                            documentTypeLabel(document.type)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${expiry.format(dateFormatter)} · $relative",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                DocumentStatusChip(status = item.status)
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_edit)) },
                            onClick = {
                                menuOpen = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete)) },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            document.notes?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = document.alertsEnabled,
                    onCheckedChange = onToggleAlerts
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.document_alerts),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentFormDialog(
    form: DocumentFormState,
    onDismiss: () -> Unit,
    onUpdate: ((DocumentFormState) -> DocumentFormState) -> Unit,
    onSave: () -> Unit
) {
    var typeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = {
            Text(
                stringResource(
                    if (form.editingId == 0L) R.string.document_add else R.string.document_edit
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = documentTypeLabel(form.type),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.document_type)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        DocumentType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(documentTypeLabel(type)) },
                                onClick = {
                                    onUpdate { it.copy(type = type) }
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                if (form.type == DocumentType.OTHER) {
                    OutlinedTextField(
                        value = form.name,
                        onValueChange = { v -> onUpdate { it.copy(name = v) } },
                        label = { Text(stringResource(R.string.document_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = DocumentField.NAME in form.errors,
                        supportingText = if (DocumentField.NAME in form.errors) {
                            { Text(stringResource(R.string.error_required)) }
                        } else null
                    )
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        form.expiryDateEpochMs?.let {
                            it.toLocalDate().format(dateFormatter)
                        } ?: stringResource(R.string.action_select_date)
                    )
                }
                if (DocumentField.EXPIRY_DATE in form.errors) {
                    Text(
                        text = stringResource(R.string.error_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = form.notes,
                    onValueChange = { v -> onUpdate { it.copy(notes = v) } },
                    label = { Text(stringResource(R.string.document_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = form.alertsEnabled,
                        onCheckedChange = { v -> onUpdate { it.copy(alertsEnabled = v) } }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.document_alerts),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = form.expiryDateEpochMs
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onUpdate { it.copy(expiryDateEpochMs = millis) }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
