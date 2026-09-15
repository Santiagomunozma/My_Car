package com.micarro.app.feature.vehicle.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.micarro.app.R
import com.micarro.app.core.ui.components.EmptyState
import com.micarro.app.core.ui.components.StatusChip
import com.micarro.app.core.ui.theme.TextSecondary
import com.micarro.app.domain.model.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    onVehicleClick: (Long) -> Unit,
    onAddVehicle: () -> Unit,
    onEditVehicle: (Long) -> Unit,
    viewModel: VehiclesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var archiveCandidate by remember { mutableStateOf<Vehicle?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_vehicles)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddVehicle) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.vehicle_add))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !uiState.showArchived,
                    onClick = { viewModel.onFilterChange(false) },
                    label = { Text(stringResource(R.string.vehicle_filter_active)) }
                )
                FilterChip(
                    selected = uiState.showArchived,
                    onClick = { viewModel.onFilterChange(true) },
                    label = { Text(stringResource(R.string.vehicle_filter_archived)) }
                )
            }

            if (!uiState.loading && uiState.visibleVehicles.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.DirectionsCar,
                    title = stringResource(R.string.vehicles_empty_title),
                    message = stringResource(R.string.vehicles_empty_message)
                )
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.visibleVehicles, key = { it.id }) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            onClick = { onVehicleClick(vehicle.id) },
                            onEdit = { onEditVehicle(vehicle.id) },
                            onSetPrimary = { viewModel.setPrimary(vehicle.id) },
                            onArchive = { archiveCandidate = vehicle },
                            onReactivate = { viewModel.reactivate(vehicle.id) }
                        )
                    }
                }
            }
        }
    }

    archiveCandidate?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { archiveCandidate = null },
            shape = MaterialTheme.shapes.large,
            title = { Text(stringResource(R.string.vehicle_archive_confirm_title)) },
            text = { Text(stringResource(R.string.vehicle_archive_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.archive(vehicle.id)
                    archiveCandidate = null
                }) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { archiveCandidate = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onSetPrimary: () -> Unit,
    onArchive: () -> Unit,
    onReactivate: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = vehicle.type.icon(),
                    contentDescription = vehicle.type.label(),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = vehicle.plate,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (vehicle.isPrimary) {
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusChip(
                            text = stringResource(R.string.vehicle_primary_badge),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "${vehicle.brand} ${vehicle.line} ${vehicle.model}".trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${vehicle.year} · " + stringResource(
                        R.string.mileage_km_format, vehicle.currentMileage
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = null)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_edit)) },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        }
                    )
                    if (!vehicle.isArchived && !vehicle.isPrimary) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.vehicle_set_primary)) },
                            onClick = {
                                menuOpen = false
                                onSetPrimary()
                            }
                        )
                    }
                    if (vehicle.isArchived) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.vehicle_reactivate)) },
                            onClick = {
                                menuOpen = false
                                onReactivate()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.vehicle_archive)) },
                            onClick = {
                                menuOpen = false
                                onArchive()
                            }
                        )
                    }
                }
            }
        }
    }
}
