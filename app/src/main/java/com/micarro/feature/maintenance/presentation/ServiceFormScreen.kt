package com.micarro.feature.maintenance.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import com.micarro.feature.maintenance.domain.MaintenanceRules
import com.micarro.feature.parts.presentation.PartFormScreen
import com.micarro.ui.components.DateField
import com.micarro.ui.components.MiCarroTextField
import com.micarro.ui.components.PrimaryButton
import com.micarro.ui.theme.StatusError
import com.micarro.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceFormScreen(
    vehicleId: String,
    planId: String?,
    lastMileage: Int,
    onSave: (MaintenanceService, List<Part>) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf(if (planId != null) "Preventivo" else "Correctivo") }
    var workshop by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf(lastMileage.toString()) }
    var laborCost by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var evidenceUri by remember { mutableStateOf<String?>(null) }
    val parts = remember { mutableStateListOf<Part>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> evidenceUri = uri?.toString() }
    )

    var showMileageConfirmDialog by remember { mutableStateOf(false) }

    val currentMileage = mileage.toIntOrNull() ?: 0
    val currentTime = System.currentTimeMillis()

    // Regla RN-05: No permitir fecha futura
    val isDateValid = MaintenanceRules.isDateValid(selectedDateMillis, currentTime)
    // Regla RN-06: Kilometraje menor al último exige advertencia y confirmación
    val isMileageLower = !MaintenanceRules.isMileageValid(currentMileage, lastMileage)

    fun doSave() {
        val totalPartsCost = parts.sumOf { it.cost * it.quantity }
        val totalCost = MaintenanceRules.calculateTotalCost(
            laborCost.toDoubleOrNull() ?: 0.0,
            totalPartsCost,
            0.0
        )
        onSave(
            MaintenanceService(
                vehicleId = vehicleId,
                planId = planId,
                title = title.trim(),
                category = serviceType,
                date = selectedDateMillis,
                mileage = currentMileage,
                totalCost = totalCost,
                workshopName = workshop.trim(),
                evidenceUri = evidenceUri
            ),
            parts.toList()
        )
    }

    if (showMileageConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showMileageConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning) },
            title = { Text("Advertencia de Kilometraje (RN-06)") },
            text = {
                Text(
                    "El kilometraje ingresado ($currentMileage km) es menor al último registrado del vehículo ($lastMileage km).\n\n" +
                            "¿Confirmas que este valor es correcto y deseas registrar el servicio?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMileageConfirmDialog = false
                        doSave()
                    }
                ) {
                    Text("Confirmar de todos modos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMileageConfirmDialog = false }) {
                    Text("Corregir")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Servicio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Alerta visual de kilometraje menor (RN-06)
            if (isMileageLower && currentMileage > 0) {
                Surface(
                    color = StatusWarning.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Aviso RN-06: El kilometraje ingresado ($currentMileage km) es menor al último registrado ($lastMileage km). Se solicitará confirmación al guardar.",
                            color = StatusWarning,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Selector Tipo de Servicio: Preventivo / Correctivo (RF-17)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Tipo de servicio",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = serviceType == "Preventivo",
                        onClick = { serviceType = "Preventivo" },
                        label = { Text("Preventivo") }
                    )
                    FilterChip(
                        selected = serviceType == "Correctivo",
                        onClick = { serviceType = "Correctivo" },
                        label = { Text("Correctivo") }
                    )
                }
            }

            // Fecha de Realización (RN-05)
            DateField(
                label = "Fecha de realización",
                selectedDateMillis = selectedDateMillis,
                onDateSelected = { selectedDateMillis = it },
                errorMessage = if (!isDateValid) "La fecha de realización no puede ser futura (RN-05)" else null
            )

            MiCarroTextField(
                value = title,
                onValueChange = { title = it },
                label = "Descripción del servicio",
                placeholder = "Cambio de aceite y filtros"
            )

            MiCarroTextField(
                value = workshop,
                onValueChange = { workshop = it },
                label = "Taller / Responsable",
                placeholder = "Nombre del taller o mecánico"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiCarroTextField(
                    value = mileage,
                    onValueChange = { mileage = it.filter { ch -> ch.isDigit() } },
                    label = "Kilometraje",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    isError = isMileageLower
                )
                MiCarroTextField(
                    value = laborCost,
                    onValueChange = { laborCost = it },
                    label = "Costo Mano de Obra",
                    placeholder = "0.00",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Adjuntar Evidencia (RF-20)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Evidencia (opcional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (evidenceUri != null) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = com.micarro.ui.theme.StatusSuccess)
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(onClick = { evidenceUri = null }) {
                            Text("Quitar", color = StatusError)
                        }
                    }
                } else {
                    TextButton(onClick = { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adjuntar foto")
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            PartFormScreen(
                parts = parts,
                onAddPart = { parts.add(it) },
                onRemovePart = { parts.remove(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            val totalPartsCost = parts.sumOf { it.cost * it.quantity }
            val totalCost = MaintenanceRules.calculateTotalCost(
                laborCost.toDoubleOrNull() ?: 0.0,
                totalPartsCost,
                0.0
            )

            Text(
                text = "Costo Total: $${String.format("%.2f", totalCost)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = "Confirmar Registro",
                enabled = title.isNotBlank() && isDateValid,
                onClick = {
                    if (title.isNotBlank() && isDateValid) {
                        if (isMileageLower) {
                            showMileageConfirmDialog = true
                        } else {
                            doSave()
                        }
                    }
                }
            )
        }
    }
}
