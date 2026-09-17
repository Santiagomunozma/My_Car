package com.example.my_car.feature.maintenance.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.my_car.domain.model.MaintenancePlan
import com.example.my_car.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceFormScreen(
    vehicleId: String,
    onSave: (MaintenancePlan) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var intervalKm by remember { mutableStateOf("") }
    var intervalMonths by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Actividad", color = TextPrimary) },
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de la actividad") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría (e.g., Motor, Frenos)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = intervalKm,
                    onValueChange = { intervalKm = it },
                    label = { Text("Cada (km)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = intervalMonths,
                    onValueChange = { intervalMonths = it },
                    label = { Text("Cada (meses)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            MaintenancePlan(
                                vehicleId = vehicleId,
                                title = title,
                                category = category,
                                intervalMileage = intervalKm.toIntOrNull() ?: 0,
                                intervalMonths = intervalMonths.toIntOrNull() ?: 0
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Guardar Plan", color = Color.White)
            }
        }
    }
}
