package com.example.my_car.feature.history.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.my_car.core.util.FileExportHelper
import com.example.my_car.domain.model.MaintenanceHistoryItem
import com.example.my_car.domain.usecase.ExportHistoryToCsvUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val exportUseCase = remember { ExportHistoryToCsvUseCase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Mantenimientos") },
                actions = {
                    IconButton(onClick = {
                        val csvData = exportUseCase(state.items)
                        FileExportHelper.shareCsvFile(context, csvData)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Exportar a CSV"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.filter.query,
                onValueChange = { viewModel.onQueryChanged(it) },
                label = { Text("Buscar por servicio o taller...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.items.isEmpty()) {
                Text(
                    text = "No hay registros en el historial.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.items) { item ->
                        HistoryCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(item: MaintenanceHistoryItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Costo: $${item.totalCost}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}