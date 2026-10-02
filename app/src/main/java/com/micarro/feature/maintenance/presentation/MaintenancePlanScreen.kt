package com.micarro.feature.maintenance.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.micarro.R
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.Part
import com.micarro.domain.model.Vehicle
import com.micarro.feature.maintenance.domain.MaintenanceStatus
import com.micarro.ui.components.EmptyState
import com.micarro.ui.components.MiCarroCard
import com.micarro.ui.components.StatusChip
import com.micarro.ui.theme.StatusError
import com.micarro.ui.theme.StatusInfo
import com.micarro.ui.theme.StatusSuccess
import com.micarro.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenancePlanScreen(
    viewModel: MaintenanceViewModel,
    onAddPlan: () -> Unit,
    onRegisterService: (String) -> Unit,
    onEditPlan: (String) -> Unit,
    onOpenAlertSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var planToPostpone by remember { mutableStateOf<MaintenancePlan?>(null) }
    var planToDelete by remember { mutableStateOf<MaintenancePlan?>(null) }
    var showDeleteRejectedDialog by remember { mutableStateOf(false) }

    planToPostpone?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToPostpone = null },
            title = { Text(stringResource(R.string.maintenance_postpone)) },
            text = { Text(stringResource(R.string.maintenance_postpone_message)) },
            confirmButton = {
                Row {
                    listOf(1 to R.string.maintenance_postpone_1, 3 to R.string.maintenance_postpone_3, 7 to R.string.maintenance_postpone_7)
                        .forEach { (days, label) ->
                            TextButton(onClick = {
                                viewModel.postponePlan(
                                    plan,
                                    System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L
                                )
                                planToPostpone = null
                            }) { Text(stringResource(label)) }
                        }
                }
            },
            dismissButton = {
                TextButton(onClick = { planToPostpone = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Dialogo de confirmación para eliminar plan (RF-15)
    planToDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusError) },
            title = { Text(stringResource(R.string.maintenance_delete_title)) },
            text = { Text(stringResource(R.string.maintenance_delete_message, plan.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val currentPlan = plan
                        planToDelete = null
                        viewModel.deletePlan(currentPlan.id) { success ->
                            if (!success) {
                                showDeleteRejectedDialog = true
                            } else {
                                Toast.makeText(context, context.getString(R.string.plan_deleted), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = StatusError)
                }
            },
            dismissButton = {
                TextButton(onClick = { planToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Dialogo cuando se rechaza la eliminación por tener historial (RF-15)
    if (showDeleteRejectedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteRejectedDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning) },
            title = { Text(stringResource(R.string.plan_delete_blocked_title)) },
            text = {
                Text(stringResource(R.string.plan_delete_blocked))
            },
            confirmButton = {
                TextButton(onClick = { showDeleteRejectedDialog = false }) {
                    Text(stringResource(R.string.plan_understood))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.plan_screen_title)) },
                actions = {
                    IconButton(onClick = onOpenAlertSettings) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.plan_alerts_settings)
                        )
                    }
                    IconButton(
                        onClick = {
                            uiState.selectedVehicle?.let { vehicle ->
                                viewModel.exportHistory(context, vehicle.id.toString())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.plan_export_csv)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onAddPlan,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.plan_new_fab))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            VehicleSelector(
                vehicles = uiState.vehicles,
                selectedVehicle = uiState.selectedVehicle,
                onVehicleSelected = { viewModel.selectVehicle(it) }
            )

            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.plan_tab_plans, uiState.plans.size)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.plan_tab_parts, uiState.installedParts.size)) }
                )
            }

            if (selectedTab == 0) {
                if (uiState.plans.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Build,
                        title = stringResource(R.string.plan_empty_title),
                        message = stringResource(R.string.plan_empty_message)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.plans, key = { it.plan.id }) { planWithStatus ->
                            MaintenancePlanCard(
                                planWithStatus = planWithStatus,
                                dateFormat = dateFormat,
                                onRegisterService = { onRegisterService(planWithStatus.plan.id) },
                                onEdit = { onEditPlan(planWithStatus.plan.id) },
                                onPostpone = { planToPostpone = planWithStatus.plan },
                                onToggleActive = {
                                    viewModel.togglePlanActiveStatus(
                                        planWithStatus.plan.id,
                                        planWithStatus.plan.isActive
                                    )
                                },
                                onDelete = { planToDelete = planWithStatus.plan }
                            )
                        }
                    }
                }
            } else {
                // RF-27: Visualización de repuestos actualmente instalados
                if (uiState.installedParts.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.ShoppingBag,
                        title = stringResource(R.string.plan_parts_empty_title),
                        message = stringResource(R.string.plan_parts_empty_message)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.installedParts, key = { it.id }) { part ->
                            InstalledPartCard(part = part)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelector(
    vehicles: List<Vehicle>,
    selectedVehicle: Vehicle?,
    onVehicleSelected: (Vehicle) -> Unit
) {
    if (vehicles.isNotEmpty()) {
        val safeIndex = vehicles.indexOf(selectedVehicle).coerceIn(0, vehicles.size - 1)

        PrimaryScrollableTabRow(
            selectedTabIndex = safeIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            vehicles.forEach { vehicle ->
                Tab(
                    selected = vehicle == selectedVehicle,
                    onClick = { onVehicleSelected(vehicle) },
                    text = { Text(vehicle.plate) }
                )
            }
        }
    }
}

@Composable
fun MaintenancePlanCard(
    planWithStatus: MaintenancePlanWithStatus,
    dateFormat: SimpleDateFormat,
    onRegisterService: () -> Unit,
    onEdit: () -> Unit,
    onPostpone: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val plan = planWithStatus.plan

    MiCarroCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.plan_category_line, plan.category),
                        style = MaterialTheme.typography.bodySmall,
                        color = muted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!plan.isActive) {
                        StatusChip(
                            text = stringResource(R.string.plan_status_paused),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            icon = Icons.Default.PauseCircle
                        )
                    } else {
                        val statusInfo = when (planWithStatus.status) {
                            MaintenanceStatus.AL_DIA -> Triple(stringResource(R.string.plan_status_ok), StatusSuccess, Icons.Default.CheckCircle)
                            MaintenanceStatus.PROXIMA -> Triple(stringResource(R.string.plan_status_soon), StatusWarning, Icons.Default.Warning)
                            MaintenanceStatus.VENCIDA -> Triple(stringResource(R.string.plan_status_overdue), StatusError, Icons.Default.Info)
                            MaintenanceStatus.SIN_PROGRAMACION -> Triple(stringResource(R.string.plan_status_none), StatusInfo, Icons.Default.Info)
                        }

                        StatusChip(
                            text = statusInfo.first,
                            containerColor = statusInfo.second,
                            icon = statusInfo.third
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            planWithStatus.nextDeadlineDate?.let {
                Text(
                    text = stringResource(R.string.plan_next_date, dateFormat.format(Date(it))),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }

            planWithStatus.nextLimitMileage?.let {
                Text(
                    text = stringResource(R.string.plan_next_km, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }

            if (plan.intervalMileage > 0 || plan.intervalMonths > 0) {
                val everyKm = if (plan.intervalMileage > 0) {
                    stringResource(R.string.plan_every_km, plan.intervalMileage)
                } else {
                    null
                }
                val everyMonths = if (plan.intervalMonths > 0) {
                    stringResource(R.string.plan_every_months, plan.intervalMonths)
                } else {
                    null
                }
                val intervalDesc = listOfNotNull(everyKm, everyMonths).joinToString(" · ")
                Text(
                    text = stringResource(R.string.plan_frequency, intervalDesc),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.maintenance_edit),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onPostpone) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.maintenance_postpone),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onToggleActive) {
                        Icon(
                            imageVector = if (plan.isActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (plan.isActive) stringResource(R.string.plan_pause) else stringResource(R.string.plan_resume),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Botón Eliminar (RF-15)
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.plan_delete_cd),
                            tint = StatusError
                        )
                    }
                }

                Button(
                    onClick = onRegisterService,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(stringResource(R.string.plan_register_service))
                }
            }
        }
    }
}

@Composable
fun InstalledPartCard(part: Part) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    MiCarroCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = part.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.currency_symbol) + String.format(Locale.US, "%.2f", part.cost * part.quantity),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!part.brand.isNullOrBlank() || !part.reference.isNullOrBlank()) {
                val brandRef = listOfNotNull(part.brand, part.reference).joinToString(" • ")
                Text(
                    text = brandRef,
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }
            part.originTitle?.let { origin ->
                Text(
                    text = stringResource(R.string.maintenance_origin, origin),
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }

            Text(
                text = stringResource(
                    R.string.plan_part_qty,
                    part.quantity,
                    stringResource(R.string.currency_symbol) + String.format(Locale.US, "%.2f", part.cost)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )
        }
    }
}