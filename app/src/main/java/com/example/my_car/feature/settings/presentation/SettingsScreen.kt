package com.example.my_car.feature.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.my_car.ui.theme.StatusError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Configuración y Datos") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Gestión de Almacenamiento", style = MaterialTheme.typography.titleMedium)

            Button(
                onClick = { viewModel.showDeleteDialog(true) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar todos los datos")
            }

            state.message?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }
        }

        if (state.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteDialog(false) },
                title = { Text("¿Eliminar todos los datos?") },
                text = { Text("Esta acción es irreversible y borrará vehículos, mantenimientos y registros.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteAllData() }) {
                        Text("Eliminar", color = StatusError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteDialog(false) }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}