package com.example.my_car.feature.history.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.my_car.domain.model.MaintenanceHistoryItem
import com.example.my_car.feature.maintenance.presentation.ExpensesViewModel
import com.example.my_car.feature.maintenance.presentation.components.ExpensesSummaryCard
import com.example.my_car.ui.components.MiCarroCard
import com.example.my_car.ui.components.MiCarroTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    expensesViewModel: ExpensesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val expensesState by expensesViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher de SAF para seleccionar dónde guardar el CSV de forma segura (RF-38)
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            viewModel.exportToCsv(
                uri = it,
                contentResolver = context.contentResolver
            )
        }
    }

    // Muestra notificaciones tipo Snackbar en pantalla cuando haya mensajes en el estado
    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    val categories = listOf("Motor", "Frenos", "Suspensión", "Transmisión", "Eléctrico", "Otros")

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Historial de Mantenimientos") },
                actions = {
                    IconButton(onClick = {
                        val fileName = "Historial_Mantenimiento_${System.currentTimeMillis()}.csv"
                        exportCsvLauncher.launch(fileName)
                    }) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Exportar a CSV"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sección superior: Resumen visual de gastos agregados (RF-36)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                ExpensesSummaryCard(
                    summary = expensesState.summary,
                    selectedPeriod = expensesState.selectedPeriod,
                    onPeriodSelected = { period ->
                        expensesViewModel.setPeriod(period)
                    }
                )
            }

            // Buscador por texto
            item {
                MiCarroTextField(
                    value = state.filter.query ?: "",
                    onValueChange = { viewModel.onQueryChanged(it) },
                    label = "Buscar",
                    placeholder = "Servicio o taller...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                )
            }

            // Chips interactivos de categoría
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = state.filter.category == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCategorySelected(category) },
                            label = { Text(category) }
                        )
                    }
                }
            }

            // Lista detallada de mantenimientos o estado vacío
            if (state.items.isEmpty()) {
                item {
                    Text(
                        text = "No hay registros en el historial.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(state.items, key = { it.id }) { item ->
                    HistoryCard(item)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HistoryCard(item: MaintenanceHistoryItem) {
    MiCarroCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$${item.totalCost}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.category} • ${item.mileage} km",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            item.workshopName?.takeIf { it.isNotBlank() }?.let { workshop ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Taller: $workshop",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}