package com.micarro.feature.history.presentation

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
import androidx.compose.material.icons.filled.PictureAsPdf
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.micarro.R
import com.micarro.ui.components.DateField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.micarro.domain.model.MaintenanceHistoryItem
import com.micarro.feature.maintenance.presentation.ExpensesViewModel
import com.micarro.feature.maintenance.presentation.components.ExpensesSummaryCard
import com.micarro.ui.components.MiCarroCard
import com.micarro.ui.components.MiCarroTextField
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    expensesViewModel: ExpensesViewModel = hiltViewModel(),
    onEditService: (MaintenanceHistoryItem) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val expensesState by expensesViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher de SAF para seleccionar dónde guardar el CSV de forma segura (RF-38)
    val currency = stringResource(R.string.currency_symbol)
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            viewModel.exportToCsv(
                uri = it,
                contentResolver = context.contentResolver,
                currencySymbol = currency
            )
        }
    }
    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let {
            viewModel.exportToPdf(uri = it, contentResolver = context.contentResolver, currencySymbol = currency)
        }
    }

    // Muestra notificaciones tipo Snackbar en pantalla cuando haya mensajes en el estado
    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    val categories = com.micarro.feature.maintenance.presentation.DEFAULT_CATEGORIES
    val serviceTypes = listOf("PREVENTIVE" to "Preventivo", "CORRECTIVE" to "Correctivo")
    var minCost by remember { mutableStateOf("") }
    var maxCost by remember { mutableStateOf("") }
    var workshop by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<MaintenanceHistoryItem?>(null) }
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()

    LaunchedEffect(state.filter.vehicleId) {
        expensesViewModel.setVehicleId(state.filter.vehicleId ?: "")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                actions = {
                    IconButton(onClick = {
                        val fileName = "Historial_Mantenimiento_${System.currentTimeMillis()}.csv"
                        exportCsvLauncher.launch(fileName)
                    }) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = stringResource(R.string.history_export_csv)
                        )
                    }
                    IconButton(onClick = {
                        val fileName = "Historial_Mantenimiento_${System.currentTimeMillis()}.pdf"
                        exportPdfLauncher.launch(fileName)
                    }) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = stringResource(R.string.history_export_pdf)
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
                    label = stringResource(R.string.history_search),
                    placeholder = stringResource(R.string.history_search_hint),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.filter.vehicleId == null,
                            onClick = { viewModel.onVehicleSelected(null) },
                            label = { Text(stringResource(R.string.history_all_vehicles)) }
                        )
                    }
                    items(vehicles, key = { it.id }) { vehicle ->
                        FilterChip(
                            selected = state.filter.vehicleId == vehicle.id.toString(),
                            onClick = { viewModel.onVehicleSelected(vehicle.id.toString()) },
                            label = { Text(vehicle.plate) }
                        )
                    }
                }
            }

            item {
                Text(stringResource(R.string.maintenance_category), style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = state.filter.category == category,
                            onClick = { viewModel.onCategorySelected(category) },
                            label = { Text(category) }
                        )
                    }
                }
            }

            item {
                Text(stringResource(R.string.history_type), style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(serviceTypes) { (type, label) ->
                        FilterChip(
                            selected = state.filter.serviceType == type,
                            onClick = { viewModel.onServiceTypeSelected(type) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            item {
                MiCarroTextField(
                    value = workshop,
                    onValueChange = {
                        workshop = it
                        viewModel.onWorkshopChanged(it)
                    },
                    label = stringResource(R.string.history_workshop)
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiCarroTextField(
                        value = minCost,
                        onValueChange = {
                            minCost = it
                            viewModel.onCostRangeChanged(it, maxCost)
                        },
                        label = stringResource(R.string.history_min_cost),
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    MiCarroTextField(
                        value = maxCost,
                        onValueChange = {
                            maxCost = it
                            viewModel.onCostRangeChanged(minCost, it)
                        },
                        label = stringResource(R.string.history_max_cost),
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField(
                        label = stringResource(R.string.history_from),
                        selectedDateMillis = state.filter.startDate,
                        onDateSelected = { viewModel.onDateRangeChanged(it, state.filter.endDate) },
                        modifier = Modifier.weight(1f)
                    )
                    DateField(
                        label = stringResource(R.string.history_to),
                        selectedDateMillis = state.filter.endDate,
                        onDateSelected = { viewModel.onDateRangeChanged(state.filter.startDate, it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                TextButton(onClick = {
                    workshop = ""
                    minCost = ""
                    maxCost = ""
                    viewModel.clearFilters()
                }) { Text(stringResource(R.string.history_clear_filters)) }
            }

            // Lista detallada de mantenimientos o estado vacío
            if (state.items.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(state.items, key = { it.id }) { item ->
                    HistoryCard(
                        item = item,
                        onEdit = { onEditService(item) },
                        onDelete = { pendingDelete = item }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.service_delete_title)) },
            text = { Text(stringResource(R.string.service_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteService(item.id)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun HistoryCard(
    item: MaintenanceHistoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                    text = stringResource(R.string.currency_symbol) + String.format(Locale.US, "%.2f", item.totalCost),
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
            Text(
                text = stringResource(R.string.history_workshop_label, item.workshopName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row {
                TextButton(onClick = onEdit) { Text(stringResource(R.string.action_edit)) }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete)) }
            }
        }
    }
}