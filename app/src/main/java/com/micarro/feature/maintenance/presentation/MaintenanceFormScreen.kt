package com.micarro.feature.maintenance.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.Vehicle

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceFormScreen(
    viewModel: MaintenanceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Estado del vehículo seleccionado (toma por defecto el activo o el primero de la lista)
    var selectedVehicle by remember(uiState.vehicles, uiState.selectedVehicle) {
        mutableStateOf<Vehicle?>(uiState.selectedVehicle ?: uiState.vehicles.firstOrNull())
    }
    var expandedVehicleDropdown by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(DEFAULT_CATEGORIES.first()) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var intervalKmText by remember { mutableStateOf("") }
    var intervalMonthsText by remember { mutableStateOf("") }

    val kmInt = intervalKmText.toIntOrNull() ?: 0
    val monthsInt = intervalMonthsText.toIntOrNull() ?: 0
    val isFormValid = title.isNotBlank() && (kmInt > 0 || monthsInt > 0) && selectedVehicle != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Actividad") },
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
                .padding(16.dp),
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
                        label = { Text("Vehículo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicleDropdown) },
                        modifier = Modifier
                            .menuAnchor()
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
                    label = { Text("Vehículo asignado") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2. Título de la actividad
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de la actividad") },
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
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategoryDropdown,
                    onDismissRequest = { expandedCategoryDropdown = false }
                ) {
                    DEFAULT_CATEGORIES.forEach { option ->
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

            // 4. Intervalos (Km / Meses opcionales)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = intervalKmText,
                    onValueChange = { input -> intervalKmText = input.filter { it.isDigit() } },
                    label = { Text("Cada (km)") },
                    placeholder = { Text("Opcional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = intervalMonthsText,
                    onValueChange = { input -> intervalMonthsText = input.filter { it.isDigit() } },
                    label = { Text("Cada (meses)") },
                    placeholder = { Text("Opcional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. Botón de guardado
            Button(
                onClick = {
                    val targetVehicle = selectedVehicle
                    if (targetVehicle == null) {
                        Toast.makeText(context, "Debes registrar o seleccionar un vehículo", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val newPlan = MaintenancePlan(
                        vehicleId = targetVehicle.plate,
                        title = title.trim(),
                        category = category,
                        intervalMileage = kmInt,
                        intervalMonths = monthsInt
                    )

                    viewModel.savePlan(newPlan)
                    Toast.makeText(context, "Actividad guardada para ${targetVehicle.plate}", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Guardar Plan")
            }
        }
    }
}