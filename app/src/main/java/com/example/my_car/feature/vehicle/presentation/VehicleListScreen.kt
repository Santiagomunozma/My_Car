package com.example.my_car.feature.vehicle.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.my_car.R
import com.example.my_car.domain.model.Vehicle
import com.example.my_car.domain.model.VehicleType
import com.example.my_car.ui.components.EmptyState
import com.example.my_car.ui.components.MiCarroCard
import com.example.my_car.ui.components.StatusChip
import com.example.my_car.ui.theme.StatusSuccess
import java.io.File

fun vehicleTypeLabel(type: VehicleType): Int = when (type) {
    VehicleType.CAR -> R.string.vehicle_type_car
    VehicleType.TRUCK -> R.string.vehicle_type_truck
    VehicleType.MOTORCYCLE -> R.string.vehicle_type_motorcycle
}

fun vehicleTypeIcon(type: VehicleType) = when (type) {
    VehicleType.CAR -> Icons.Filled.DirectionsCar
    VehicleType.TRUCK -> Icons.Filled.LocalShipping
    VehicleType.MOTORCYCLE -> Icons.Filled.TwoWheeler
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    onAddVehicle: () -> Unit,
    onEditVehicle: (String) -> Unit,
    onOpenMileage: (String) -> Unit,
    onOpenDocuments: (String) -> Unit,
    onOpenAlerts: () -> Unit,
    viewModel: VehiclesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vehicles_title)) },
                actions = {
                    IconButton(onClick = onOpenAlerts) {
                        BadgedBox(
                            badge = {
                                if (state.pendingAlertsCount > 0) {
                                    Badge { Text(state.pendingAlertsCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = stringResource(R.string.vehicle_alerts_icon)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddVehicle) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.vehicles_add)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            FilterChip(
                selected = state.showArchived,
                onClick = viewModel::toggleShowArchived,
                label = {
                    Text(
                        stringResource(
                            if (state.showArchived) R.string.vehicles_filter_archived
                            else R.string.vehicles_filter_active
                        )
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.vehicles.isEmpty() -> EmptyState(
                    icon = Icons.Filled.DirectionsCar,
                    title = stringResource(R.string.vehicles_empty_title),
                    message = stringResource(R.string.vehicles_empty_message)
                )

                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.vehicles, key = { it.id }) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            onEdit = { onEditVehicle(vehicle.id) },
                            onMileage = { onOpenMileage(vehicle.id) },
                            onDocuments = { onOpenDocuments(vehicle.id) },
                            onSetMain = { viewModel.setMain(vehicle.id) },
                            onArchive = { viewModel.requestArchive(vehicle) },
                            onReactivate = { viewModel.reactivate(vehicle.id) }
                        )
                    }
                }
            }
        }
    }

    state.vehicleToArchive?.let { vehicle ->
        AlertDialog(
            onDismissRequest = viewModel::dismissArchive,
            title = { Text(stringResource(R.string.vehicle_archive_confirm_title)) },
            text = { Text(stringResource(R.string.vehicle_archive_confirm_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmArchive) {
                    Text(stringResource(R.string.vehicle_archive))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissArchive) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onEdit: () -> Unit,
    onMileage: () -> Unit,
    onDocuments: () -> Unit,
    onSetMain: () -> Unit,
    onArchive: () -> Unit,
    onReactivate: () -> Unit
) {
    MiCarroCard {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (vehicle.photoUri != null) {
                AsyncImage(
                    model = File(vehicle.photoUri),
                    contentDescription = stringResource(R.string.cd_vehicle_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small)
                )
            } else {
                Icon(
                    imageVector = vehicleTypeIcon(vehicle.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(56.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(vehicle.plate, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${vehicle.brand} ${vehicle.line} ${vehicle.model}".trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.vehicle_mileage_value, vehicle.currentMileage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (vehicle.isMainVehicle) {
                StatusChip(
                    text = stringResource(R.string.vehicle_main_badge),
                    containerColor = StatusSuccess,
                    icon = Icons.Filled.Star
                )
            }
            if (vehicle.isArchived) {
                StatusChip(
                    text = stringResource(R.string.vehicle_archived_badge),
                    containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = Icons.Filled.Archive
                )
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            TextButton(onClick = onEdit) { Text(stringResource(R.string.action_edit)) }
            TextButton(onClick = onMileage) {
                Text(stringResource(R.string.vehicle_mileage_button))
            }
            TextButton(onClick = onDocuments) {
                Text(stringResource(R.string.vehicle_documents_button))
            }
            if (!vehicle.isArchived) {
                if (!vehicle.isMainVehicle) {
                    TextButton(onClick = onSetMain) {
                        Text(stringResource(R.string.vehicle_set_main))
                    }
                }
                TextButton(onClick = onArchive) {
                    Text(stringResource(R.string.vehicle_archive))
                }
            } else {
                TextButton(onClick = onReactivate) {
                    Text(stringResource(R.string.vehicle_reactivate))
                }
            }
        }
    }
}
