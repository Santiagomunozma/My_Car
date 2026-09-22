package com.micarro.feature.alerts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.micarro.feature.maintenance.presentation.MaintenanceViewModel
import com.micarro.ui.components.MiCarroTextField
import com.micarro.ui.components.PrimaryButton

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
                title = { Text("Configuración de Alertas") },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Establece con qué anticipación deseas recibir avisos de mantenimiento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            MiCarroTextField(
                value = days,
                onValueChange = { days = it },
                label = "Días de anticipación",
                placeholder = "15",
                keyboardType = KeyboardType.Number,
                suffix = { Text("días") }
            )

            MiCarroTextField(
                value = km,
                onValueChange = { km = it },
                label = "Kilometraje de anticipación",
                placeholder = "500",
                keyboardType = KeyboardType.Number,
                suffix = { Text("km") }
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Guardar Cambios",
                onClick = {
                    val d = days.toIntOrNull() ?: 15
                    val k = km.toIntOrNull() ?: 500
                    viewModel.updateAlertSettings(d, k)
                    onBack()
                }
            )
        }
    }
}
