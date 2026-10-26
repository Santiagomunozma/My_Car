package com.micarro.feature.documents.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.micarro.R
import com.micarro.domain.model.DocumentStatus
import com.micarro.domain.model.DocumentType
import com.micarro.domain.model.VehicleDocument
import com.micarro.feature.documents.domain.DocumentDraft
import com.micarro.feature.documents.domain.DocumentError
import com.micarro.feature.documents.domain.DocumentField
import com.micarro.feature.documents.domain.DocumentStatusRules
import com.micarro.feature.documents.domain.DocumentWithStatus
import com.micarro.feature.documents.domain.PlateMatch
import com.micarro.ui.components.DateField
import com.micarro.ui.components.EmptyState
import com.micarro.ui.components.MiCarroCard
import com.micarro.ui.components.MiCarroDropdownField
import com.micarro.ui.components.MiCarroTextField
import com.micarro.ui.components.PrimaryButton
import com.micarro.ui.components.StatusChip
import com.micarro.ui.components.StatusTone
import com.micarro.ui.components.formatDate
import com.micarro.ui.components.statusAccent
import com.micarro.ui.components.statusContainer
import java.io.File

fun documentTypeLabel(type: DocumentType): Int = when (type) {
    DocumentType.SOAT -> R.string.document_type_soat
    DocumentType.TECHNICAL_INSPECTION -> R.string.document_type_technical
    DocumentType.INSURANCE -> R.string.document_type_insurance
    DocumentType.TAX -> R.string.document_type_tax
    DocumentType.TRAFFIC_LIGHT -> R.string.document_type_traffic_light
    DocumentType.OTHER -> R.string.document_type_other
}

fun openStoredDocument(context: android.content.Context, path: String, mimeType: String?) {
    val file = File(path)
    if (!file.isFile) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType?.ifBlank { null } ?: "*/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.document_open_file)))
    }
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
    val context = LocalContext.current
    var pendingCamera by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.attachFromUri(uri.toString())
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val path = pendingCamera
        pendingCamera = null
        if (success && path != null) viewModel.attachFromCamera(path) else path?.let { File(it).delete() }
    }
    fun launchCamera() {
        val path = viewModel.createCameraFile()
        pendingCamera = path
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", File(path))
        cameraLauncher.launch(uri)
    }
    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
    }
    val onTakePhoto = {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) launchCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
    }
    val onOpenFile = { path: String, mime: String? ->
        openStoredDocument(context, path, mime)
    }

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
                        },
                        onOpenFile = {
                            item.document.filePath?.let { path ->
                                onOpenFile(path, item.document.mimeType)
                            }
                        }
                    )
                }
            }
        }
    }

    if (state.showForm) {
        DocumentFormDialog(
            editing = state.editingDocument,
            vehicleId = state.vehicle?.id ?: 0L,
            vehiclePlate = state.vehicle?.plate.orEmpty(),
            isSaving = state.isSaving,
            isReading = state.isReading,
            readProblem = state.readProblem,
            scan = state.scan,
            scanToken = state.scanToken,
            saveFailed = state.saveFailed,
            errors = state.formErrors,
            onDismiss = viewModel::dismissForm,
            onSave = viewModel::save,
            onAttach = { filePicker.launch(arrayOf("image/*", "application/pdf")) },
            onTakePhoto = onTakePhoto,
            onOpenFile = onOpenFile
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
    onToggleAlerts: (Boolean) -> Unit,
    onOpenFile: () -> Unit
) {
    val (statusText, statusTone, statusIcon) = when (item.status) {
        DocumentStatus.UP_TO_DATE -> Triple(
            stringResource(R.string.doc_status_valid), StatusTone.Success, Icons.Filled.CheckCircle
        )
        DocumentStatus.UPCOMING -> Triple(
            stringResource(R.string.doc_status_expiring), StatusTone.Warning, Icons.Filled.Warning
        )
        DocumentStatus.EXPIRED -> Triple(
            stringResource(R.string.doc_status_expired), StatusTone.Error, Icons.Filled.Error
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
                        formatDate(item.document.expirationDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(text = statusText, tone = statusTone, icon = statusIcon)
            }

            item.document.issuer?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            item.document.referenceNumber?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.document.filePath != null) {
                TextButton(onClick = onOpenFile) {
                    Text(stringResource(R.string.document_open_file))
                }
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
                            tint = statusAccent(StatusTone.Error)
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
    vehicleId: Long,
    vehiclePlate: String,
    isSaving: Boolean,
    isReading: Boolean,
    readProblem: DocumentReadProblem?,
    scan: DocumentScan?,
    scanToken: Int,
    saveFailed: Boolean,
    errors: Map<DocumentField, DocumentError>,
    onDismiss: () -> Unit,
    onSave: (DocumentDraft) -> Unit,
    onAttach: () -> Unit,
    onTakePhoto: () -> Unit,
    onOpenFile: (String, String?) -> Unit
) {
    var type by remember { mutableStateOf(editing?.type ?: DocumentType.SOAT) }
    var name by remember { mutableStateOf(editing?.name.orEmpty()) }
    var expiration by remember {
        mutableStateOf(editing?.expirationDate?.let { DocumentStatusRules.dateToMillis(it) })
    }
    var issuer by remember { mutableStateOf(editing?.issuer.orEmpty()) }
    var reference by remember { mutableStateOf(editing?.referenceNumber.orEmpty()) }
    var notes by remember { mutableStateOf(editing?.notes.orEmpty()) }
    var alertsEnabled by remember { mutableStateOf(editing?.alertsEnabled ?: true) }
    var filePath by remember { mutableStateOf(editing?.filePath) }
    var mimeType by remember { mutableStateOf(editing?.mimeType) }
    var extractedPlate by remember { mutableStateOf<String?>(null) }
    var plateAcknowledged by remember { mutableStateOf(false) }

    LaunchedEffect(scanToken) {
        val current = scan ?: return@LaunchedEffect
        if (scanToken == 0) return@LaunchedEffect
        filePath = current.path
        mimeType = current.mimeType
        current.extraction?.let { extracted ->
            extracted.type?.let { type = it }
            extracted.name?.let { name = it }
            extracted.expirationDate?.let { expiration = DocumentStatusRules.dateToMillis(it) }
            extracted.issuer?.let { issuer = it }
            extracted.referenceNumber?.let { reference = it }
            extractedPlate = extracted.plate
            plateAcknowledged = false
        }
    }

    val plateMismatch = !PlateMatch.matches(extractedPlate, vehiclePlate)

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onAttach, enabled = !isReading && !isSaving) {
                        Text(stringResource(R.string.document_attach))
                    }
                    TextButton(onClick = onTakePhoto, enabled = !isReading && !isSaving) {
                        Text(stringResource(R.string.document_take_photo))
                    }
                }
                if (filePath != null) {
                    TextButton(onClick = { onOpenFile(filePath!!, mimeType) }) {
                        Text(stringResource(R.string.document_attached))
                    }
                }
                if (isReading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(18.dp)
                        )
                        Text(stringResource(R.string.document_reading))
                    }
                }
                readProblem?.let { problem ->
                    val message = when (problem) {
                        DocumentReadProblem.MISSING_KEY -> R.string.document_read_missing_key
                        DocumentReadProblem.FAILED -> R.string.document_read_failed
                        DocumentReadProblem.TOO_LARGE -> R.string.document_file_too_large
                    }
                    Text(
                        stringResource(message),
                        color = statusAccent(StatusTone.Warning),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                MiCarroDropdownField(
                    label = stringResource(R.string.document_type),
                    options = DocumentType.entries,
                    selected = type,
                    optionLabel = { stringResource(documentTypeLabel(it)) },
                    onSelected = { type = it }
                )

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
                    value = reference,
                    onValueChange = { reference = it },
                    label = stringResource(R.string.document_reference)
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
                if (plateMismatch) {
                    Text(
                        stringResource(
                            R.string.document_plate_mismatch,
                            extractedPlate.orEmpty(),
                            vehiclePlate
                        ),
                        color = statusAccent(StatusTone.Warning),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = plateAcknowledged,
                            onCheckedChange = { plateAcknowledged = it }
                        )
                        Text(stringResource(R.string.document_plate_confirm))
                    }
                }
                if (saveFailed) {
                    Text(
                        stringResource(R.string.error_save_failed),
                        color = statusAccent(StatusTone.Error),
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
                            expirationDate = expiration?.let { DocumentStatusRules.millisToDate(it) },
                            issuer = issuer,
                            alertsEnabled = alertsEnabled,
                            notes = notes,
                            referenceNumber = reference,
                            filePath = filePath,
                            mimeType = mimeType,
                            extractedPlate = extractedPlate,
                            plateAcknowledged = plateAcknowledged || !plateMismatch
                        )
                    )
                },
                enabled = !isSaving && !isReading
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
