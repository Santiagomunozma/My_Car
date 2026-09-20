package com.example.my_car.feature.maintenance.presentation

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.my_car.domain.model.MaintenanceService
import com.example.my_car.domain.model.Part
import com.example.my_car.feature.maintenance.domain.MaintenanceRules
import com.example.my_car.feature.parts.presentation.PartFormScreen
import com.example.my_car.ui.components.MiCarroTextField
import com.example.my_car.ui.components.PrimaryButton
import com.example.my_car.ui.theme.StatusError

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
    var workshop by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf(lastMileage.toString()) }
    var laborCost by remember { mutableStateOf("") }
    var parts = remember { mutableStateListOf<Part>() }

    val currentMileage = mileage.toIntOrNull() ?: 0
    val currentTime = System.currentTimeMillis()

    val isMileageValid = MaintenanceRules.isMileageValid(currentMileage, lastMileage)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Servicio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
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
            if (!isMileageValid) {
                Surface(
                    color = StatusError.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = StatusError)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Alerta RN-06: El kilometraje no puede ser menor al último registrado ($lastMileage km).",
                            color = StatusError,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            MiCarroTextField(
                value = title,
                onValueChange = { title = it },
                label = "Descripción del servicio",
                placeholder = "Cambio de aceite y filtros"
            )

            MiCarroTextField(
                value = workshop,
                onValueChange = { workshop = it },
                label = "Taller / Establecimiento",
                placeholder = "Nombre del taller"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiCarroTextField(
                    value = mileage,
                    onValueChange = { mileage = it },
                    label = "Kilometraje",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    isError = !isMileageValid
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            PartFormScreen(
                parts = parts,
                onAddPart = { parts.add(it) },
                onRemovePart = { parts.remove(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                enabled = isMileageValid && title.isNotBlank(),
                onClick = {
                    if (title.isNotBlank() && isMileageValid) {
                        onSave(
                            MaintenanceService(
                                vehicleId = vehicleId,
                                planId = planId,
                                title = title,
                                category = "General",
                                date = currentTime,
                                mileage = currentMileage,
                                totalCost = totalCost,
                                workshopName = workshop
                            ),
                            parts.toList()
                        )
                    }
                }
            )
        }
    }
}
