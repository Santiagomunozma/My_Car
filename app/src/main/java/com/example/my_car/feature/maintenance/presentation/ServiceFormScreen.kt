package com.example.my_car.feature.maintenance.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.my_car.domain.model.MaintenanceService
import com.example.my_car.domain.model.Part
import com.example.my_car.feature.maintenance.domain.MaintenanceRules
import com.example.my_car.feature.parts.presentation.PartFormScreen
import com.example.my_car.ui.theme.*
import java.util.*

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
    
    // RN-05 Validation
    val isDateValid = true // Simplificado: Asumimos fecha actual para el registro
    
    // RN-06 Validation
    val isMileageValid = MaintenanceRules.isMileageValid(currentMileage, lastMileage)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Servicio", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundLight
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

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Descripción del servicio") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = workshop,
                onValueChange = { workshop = it },
                label = { Text("Taller / Establecimiento") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = mileage,
                    onValueChange = { mileage = it },
                    label = { Text("Kilometraje") },
                    modifier = Modifier.weight(1f),
                    isError = !isMileageValid
                )
                OutlinedTextField(
                    value = laborCost,
                    onValueChange = { laborCost = it },
                    label = { Text("Costo Mano de Obra") },
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = BorderGray)

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
                color = PrimaryBlue,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (title.isNotBlank() && isMileageValid) {
                        onSave(
                            MaintenanceService(
                                vehicleId = vehicleId,
                                planId = planId,
                                title = title,
                                category = "General", // O inferir del plan
                                date = currentTime,
                                mileage = currentMileage,
                                totalCost = totalCost,
                                workshopName = workshop
                            ),
                            parts.toList()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isMileageValid && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Confirmar Registro", color = Color.White)
            }
        }
    }
}
