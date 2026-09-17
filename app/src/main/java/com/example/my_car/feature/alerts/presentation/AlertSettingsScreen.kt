package com.example.my_car.feature.alerts.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.my_car.feature.maintenance.presentation.MaintenanceViewModel
import com.example.my_car.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSettingsScreen(
    viewModel: MaintenanceViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var days by remember { mutableStateOf(uiState.alertMarginDays.toString()) }
    var km by remember { mutableStateOf(uiState.alertMarginKm.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Alertas", color = TextPrimary) },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Establece con qué anticipación deseas recibir avisos de mantenimiento.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            OutlinedTextField(
                value = days,
                onValueChange = { days = it },
                label = { Text("Días de anticipación") },
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("días") }
            )

            OutlinedTextField(
                value = km,
                onValueChange = { km = it },
                label = { Text("Kilometraje de anticipación") },
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("km") }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val d = days.toIntOrNull() ?: 15
                    val k = km.toIntOrNull() ?: 500
                    viewModel.updateAlertSettings(d, k)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}
