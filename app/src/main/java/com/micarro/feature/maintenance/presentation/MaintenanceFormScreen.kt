package com.micarro.feature.maintenance.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.micarro.R
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.Vehicle
import com.micarro.ui.components.DateField

val DEFAULT_CATEGORIES = listOf(
    "Aceite",
    "Filtros",
    "Frenos",
    "Llantas",
    "Batería",
    "Refrigeración",
    "Suspensión",
    "Transmisión",
    "Otros"
)

data class MaintenanceTemplate(
    val name: String,
    val category: String,
    val km: Int,
    val months: Int
)

val COMMON_TEMPLATES = listOf(
    MaintenanceTemplate("Cambio de Aceite", "Aceite", 5000, 6),
    MaintenanceTemplate("Filtros (Aire/Combustible)", "Filtros", 10000, 12),
    MaintenanceTemplate("Revisión de Frenos", "Frenos", 15000, 12),
    MaintenanceTemplate("Batería y Eléctrico", "Batería", 0, 24)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceFormScreen(
    viewModel: MaintenanceViewModel,
    initialVehicleId: String? = null,
    planId: String? = null,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Estado del vehículo seleccionado (toma por defecto el que coincida con initialVehicleId o activo)
    var selectedVehicle by remember(uiState.vehicles, uiState.selectedVehicle, initialVehicleId) {
        mutableStateOf<Vehicle?>(
            uiState.vehicles.find { it.id.toString() == initialVehicleId }
                ?: uiState.selectedVehicle
                ?: uiState.vehicles.firstOrNull()
        )
    }
    var expandedVehicleDropdown by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(DEFAULT_CATEGORIES.first()) }
    var customCategory by remember { mutableStateOf("") }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var intervalKmText by remember { mutableStateOf("") }
    var intervalMonthsText by remember { mutableStateOf("") }
    var deadlineMillis by remember { mutableStateOf<Long?>(null) }
    var loadedPlan by remember { mutableStateOf<MaintenancePlan?>(null) }
    var categories by remember { mutableStateOf(DEFAULT_CATEGORIES) }

    LaunchedEffect(Unit) {
        categories = viewModel.categoryOptions()
    }

    LaunchedEffect(planId) {
        if (!planId.isNullOrBlank()) {
            viewModel.loadPlan(planId)?.let { plan ->
                loadedPlan = plan
                title = plan.title
                description = plan.description
                if (plan.category in categories) {
                    category = plan.category
                    customCategory = ""
                } else {
                    category = categories.last()
                    customCategory = plan.category
                }
                intervalKmText = plan.intervalMileage.takeIf { it > 0 }?.toString().orEmpty()
                intervalMonthsText = plan.intervalMonths.takeIf { it > 0 }?.toString().orEmpty()
                deadlineMillis = plan.nextDeadlineDate
            }
        }
    }

    val kmInt = intervalKmText.toIntOrNull() ?: 0
    val monthsInt = intervalMonthsText.toIntOrNull() ?: 0
    val resolvedCategory = customCategory.trim().ifBlank { category }
    val isFormValid = title.isNotBlank() && selectedVehicle != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (loadedPlan == null) R.string.plan_new else R.string.plan_edit)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Selector/Indicador de Vehículo segun cantidad
            if (uiState.vehicles.size > 1) {
                ExposedDropdownMenuBox(
                    expanded = expandedVehicleDropdown,
                    onExpandedChange = { expandedVehicleDropdown = !expandedVehicleDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedVehicle?.let { "${it.plate} - ${it.brand} ${it.model}" } ?: "Seleccionar vehículo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.plan_vehicle)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicleDropdown) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedVehicleDropdown,
                        onDismissRequest = { expandedVehicleDropdown = false }
                    ) {
                        uiState.vehicles.forEach { vehicle ->
                            DropdownMenuItem(
                                text = { Text("${vehicle.plate} - ${vehicle.brand} ${vehicle.model}") },
                                onClick = {
                                    selectedVehicle = vehicle
                                    expandedVehicleDropdown = false
                                }
                            )
                        }
                    }
                }
            } else if (uiState.vehicles.size == 1) {
                OutlinedTextField(
                    value = "${selectedVehicle?.plate} (${selectedVehicle?.brand} ${selectedVehicle?.model})",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.plan_vehicle_assigned)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Sugerencias / Plantillas rápidas (RF-16)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Plantillas rápidas:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    COMMON_TEMPLATES.forEach { template ->
                        androidx.compose.material3.FilterChip(
                            selected = title == template.name,
                            onClick = {
                                title = template.name
                                category = template.category
                                intervalKmText = if (template.km > 0) template.km.toString() else ""
                                intervalMonthsText = if (template.months > 0) template.months.toString() else ""
                            },
                            label = { Text(template.name) }
                        )
                    }
                }
            }

            // 2. Título de la actividad
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.plan_title_field)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.plan_description)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = customCategory,
                onValueChange = { customCategory = it },
                label = { Text(stringResource(R.string.plan_other_category)) },
                placeholder = { Text(stringResource(R.string.plan_other_category_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 3. Selector de Categoría
            ExposedDropdownMenuBox(
                expanded = expandedCategoryDropdown,
                onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.plan_category_field)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategoryDropdown,
                    onDismissRequest = { expandedCategoryDropdown = false }
                ) {
                    categories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                expandedCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            DateField(
                label = stringResource(R.string.plan_deadline),
                selectedDateMillis = deadlineMillis,
                onDateSelected = { deadlineMillis = it }
            )
            if (deadlineMillis != null) {
                androidx.compose.material3.TextButton(onClick = { deadlineMillis = null }) {
                    Text(stringResource(R.string.plan_clear_deadline))
                }
            }

            // 4. Intervalos (Km / Meses opcionales)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = intervalKmText,
                    onValueChange = { input -> intervalKmText = input.filter { it.isDigit() } },
                    label = { Text(stringResource(R.string.plan_every_km_field)) },
                    placeholder = { Text(stringResource(R.string.plan_optional)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = intervalMonthsText,
                    onValueChange = { input -> intervalMonthsText = input.filter { it.isDigit() } },
                    label = { Text(stringResource(R.string.plan_every_months_field)) },
                    placeholder = { Text(stringResource(R.string.plan_optional)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // 5. Botón de guardado
            Button(
                onClick = {
                    val targetVehicle = selectedVehicle
                    if (targetVehicle == null) {
                        Toast.makeText(context, context.getString(R.string.plan_need_vehicle), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.rememberCategory(resolvedCategory)
                    categories = viewModel.categoryOptions()
                    val base = loadedPlan
                    val newPlan = MaintenancePlan(
                        id = base?.id ?: java.util.UUID.randomUUID().toString(),
                        vehicleId = targetVehicle.id.toString(),
                        title = title.trim(),
                        category = resolvedCategory,
                        description = description.trim(),
                        intervalMileage = kmInt,
                        intervalMonths = monthsInt,
                        isActive = base?.isActive ?: true,
                        nextDeadlineDate = deadlineMillis,
                        nextLimitMileage = base?.nextLimitMileage,
                        marginDays = base?.marginDays ?: 15,
                        marginKm = base?.marginKm ?: 500,
                        alertsEnabled = base?.alertsEnabled ?: true
                    )

                    if (base == null) viewModel.savePlan(newPlan) else viewModel.updatePlan(newPlan)
                    Toast.makeText(context, context.getString(R.string.plan_saved, targetVehicle.plate), Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.plan_save))
            }
        }
    }
}