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
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.micarro.ui.components.PrimaryButton
import com.micarro.ui.components.StatusChip
import com.micarro.ui.components.StatusTone
import com.micarro.ui.components.statusAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenancePlanScreen(
    viewModel: MaintenanceViewModel,
    onAddPlan: () -> Unit,
    onRegisterService: (String?) -> Unit,
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
            icon = { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = statusAccent(StatusTone.Error)) },
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
                    Text(stringResource(R.string.action_delete), color = statusAccent(StatusTone.Error))
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
            icon = { Icon(Icons.Default.Warning, contentDescription = stringResource(R.string.maintenance_postpone), tint = statusAccent(StatusTone.Warning)) },
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
                    com.micarro.ui.components.OverflowMenu(
                        actions = listOf(
                            com.micarro.ui.components.MenuAction(
                                label = stringResource(R.string.plan_alerts_settings),
                                icon = Icons.Default.Notifications,
                                onClick = onOpenAlertSettings
                            ),
                            com.micarro.ui.components.MenuAction(
                                label = stringResource(R.string.plan_export_csv),
                                icon = Icons.Default.Share,
                                onClick = {
                                    uiState.selectedVehicle?.let { vehicle ->
                                        viewModel.exportHistory(context, vehicle.id.toString())
                                    }
                                }
                            )
                        )
                    )
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
            } else if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { onRegisterService(null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Registrar repuesto directo")
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

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                val tabs = listOf(
                    stringResource(R.string.plan_tab_plans, uiState.plans.size),
                    stringResource(R.string.plan_tab_parts, uiState.installedParts.size)
                )
                tabs.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = tabs.size
                        )
                    ) { Text(label) }
                }
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
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(vehicles, key = { it.id }) { vehicle ->
                androidx.compose.material3.FilterChip(
                    selected = vehicle == selectedVehicle,
                    onClick = { onVehicleSelected(vehicle) },
                    label = { Text(vehicle.plate) }
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
                            tone = StatusTone.Neutral,
                            icon = Icons.Default.PauseCircle
                        )
                    } else {
                        val statusInfo = when (planWithStatus.status) {
                            MaintenanceStatus.AL_DIA -> Triple(stringResource(R.string.plan_status_ok), StatusTone.Success, Icons.Default.CheckCircle)
                            MaintenanceStatus.PROXIMA -> Triple(stringResource(R.string.plan_status_soon), StatusTone.Warning, Icons.Default.Warning)
                            MaintenanceStatus.VENCIDA -> Triple(stringResource(R.string.plan_status_overdue), StatusTone.Error, Icons.Default.Error)
                            MaintenanceStatus.SIN_PROGRAMACION -> Triple(stringResource(R.string.plan_status_none), StatusTone.Info, Icons.Default.Info)
                        }

                        StatusChip(
                            text = statusInfo.first,
                            tone = statusInfo.second,
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
                com.micarro.ui.components.OverflowMenu(
                    actions = listOf(
                        com.micarro.ui.components.MenuAction(
                            label = stringResource(R.string.maintenance_edit),
                            icon = Icons.Default.Edit,
                            onClick = onEdit
                        ),
                        com.micarro.ui.components.MenuAction(
                            label = stringResource(R.string.maintenance_postpone),
                            icon = Icons.Default.Notifications,
                            onClick = onPostpone
                        ),
                        com.micarro.ui.components.MenuAction(
                            label = stringResource(
                                if (plan.isActive) R.string.plan_pause else R.string.plan_resume
                            ),
                            icon = if (plan.isActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            onClick = onToggleActive
                        ),
                        com.micarro.ui.components.MenuAction(
                            label = stringResource(R.string.action_delete),
                            icon = Icons.Default.Delete,
                            destructive = true,
                            onClick = onDelete
                        )
                    )
                )
                PrimaryButton(
                    text = stringResource(R.string.plan_register_service),
                    onClick = onRegisterService,
                    icon = Icons.Default.Build,
                    fillWidth = false
                )
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