package com.micarro.feature.dashboard.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.micarro.R
import com.micarro.domain.model.Vehicle
import com.micarro.domain.model.VehicleType
import com.micarro.ui.components.ActionTile
import com.micarro.ui.components.AquaBackground
import com.micarro.ui.components.AquaCard
import com.micarro.ui.components.LoadingState
import com.micarro.ui.components.PrimaryButton
import com.micarro.ui.theme.PlateTextStyle
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddVehicle: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenMaintenance: () -> Unit = {},
    onOpenMileage: (String) -> Unit = {},
    onOpenDocuments: (String) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DashboardHeader(
                alertCount = state.totalActiveAlerts,
                onOpenAlerts = onOpenAlerts
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        AquaBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                VehicleHeroCard(
                    selectedVehicle = state.selectedVehicle,
                    allVehicles = state.allVehicles,
                    lastMileage = state.lastMileage,
                    monthExpenses = state.currentMonthExpenses,
                    onVehicleSelected = viewModel::onVehicleSelected,
                    onAddVehicle = onAddVehicle
                )

                val vehicle = state.selectedVehicle
                if (vehicle != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionTile(
                            icon = Icons.Default.Build,
                            label = stringResource(R.string.dashboard_quick_service),
                            onClick = onOpenMaintenance,
                            modifier = Modifier.weight(1f)
                        )
                        ActionTile(
                            icon = Icons.Default.Speed,
                            label = stringResource(R.string.dashboard_quick_mileage),
                            onClick = { onOpenMileage(vehicle.id.toString()) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionTile(
                            icon = Icons.Default.Description,
                            label = stringResource(R.string.dashboard_quick_documents),
                            onClick = { onOpenDocuments(vehicle.id.toString()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.dashboard_upcoming),
                    style = MaterialTheme.typography.titleMedium
                )
                val nextTitle = state.nextMaintenanceTitle
                if (nextTitle != null) {
                    AquaCard {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = nextTitle,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (state.nextMaintenanceDetails.isNotBlank()) {
                                Text(
                                    text = state.nextMaintenanceDetails,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else if (state.upcomingMaintenances.isEmpty()) {
                    AquaCard {
                        Text(
                            text = stringResource(R.string.dashboard_upcoming_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    state.upcomingMaintenances.take(2).forEach { plan ->
                        AquaCard {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = plan.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = plan.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.dashboard_recent),
                    style = MaterialTheme.typography.titleMedium
                )
                if (state.recentServices.isEmpty()) {
                    AquaCard {
                        Text(
                            text = stringResource(R.string.dashboard_recent_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    state.recentServices.take(3).forEach { service ->
                        AquaCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = service.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = service.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "$%,.2f", service.totalCost),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(
    alertCount: Int,
    onOpenAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
            )
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        IconButton(onClick = onOpenAlerts, modifier = Modifier.size(48.dp)) {
            if (alertCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(if (alertCount > 99) "99+" else alertCount.toString())
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.dashboard_alerts_cd)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.dashboard_alerts_cd)
                )
            }
        }
    }
}

@Composable
fun VehicleHeroCard(
    selectedVehicle: Vehicle?,
    allVehicles: List<Vehicle>,
    lastMileage: Long,
    monthExpenses: Double,
    onVehicleSelected: (String) -> Unit,
    onAddVehicle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    AquaCard(modifier = modifier) {
        if (selectedVehicle != null) {
            if (selectedVehicle.photoUri != null) {
                AsyncImage(
                    model = File(selectedVehicle.photoUri),
                    contentDescription = stringResource(R.string.cd_vehicle_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedVehicle.type == VehicleType.MOTORCYCLE) {
                                Icons.Default.TwoWheeler
                            } else {
                                Icons.Default.DirectionsCar
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = stringResource(R.string.dashboard_active_vehicle),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        TextButton(
                            onClick = { expanded = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (allVehicles.size > 1) {
                                    stringResource(R.string.dashboard_change_vehicle)
                                } else {
                                    stringResource(R.string.dashboard_vehicle_options)
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = stringResource(R.string.maintenance_select_vehicle)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            allVehicles.forEach { vehicle ->
                                val isSelected = vehicle.id == selectedVehicle.id
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${vehicle.brand} ${vehicle.line.ifEmpty { vehicle.model }} (${vehicle.plate})",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onVehicleSelected(vehicle.id.toString())
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dashboard_register_vehicle)) },
                                onClick = {
                                    expanded = false
                                    onAddVehicle()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "${selectedVehicle.brand} ${selectedVehicle.line.ifEmpty { selectedVehicle.model }}",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = selectedVehicle.plate.uppercase(Locale.getDefault()),
                            style = PlateTextStyle,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.dashboard_vehicle_summary,
                            String.format(Locale.getDefault(), "%,d", lastMileage),
                            String.format(Locale.getDefault(), "$%,.0f", monthExpenses)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(R.string.dashboard_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dashboard_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PrimaryButton(
                    text = stringResource(R.string.dashboard_add_first),
                    onClick = onAddVehicle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
