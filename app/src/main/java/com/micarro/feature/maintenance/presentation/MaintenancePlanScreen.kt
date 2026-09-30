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
import androidx.compose.ui.unit.dp
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
    onOpenAlertSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var planToDelete by remember { mutableStateOf<MaintenancePlan?>(null) }
    var showDeleteRejectedDialog by remember { mutableStateOf(false) }

    // Dialogo de confirmación para eliminar plan (RF-15)
    planToDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusError) },
            title = { Text("Eliminar Actividad") },
            text = { Text("¿Deseas eliminar '${plan.title}'? Recuerda que solo se puede eliminar si no tiene un historial de servicios registrado.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val currentPlan = plan
                        planToDelete = null
                        viewModel.deletePlan(currentPlan.id) { success ->
                            if (!success) {
                                showDeleteRejectedDialog = true
                            } else {
                                Toast.makeText(context, "Actividad eliminada", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Eliminar", color = StatusError)
                }
            },
            dismissButton = {
                TextButton(onClick = { planToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialogo cuando se rechaza la eliminación por tener historial (RF-15)
    if (showDeleteRejectedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteRejectedDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning) },
            title = { Text("Acción no permitida") },
            text = {
                Text("No es posible eliminar esta actividad porque ya cuenta con un historial de servicios registrado. Puedes pausarla para que no genere más avisos.")
            },
            confirmButton = {
                TextButton(onClick = { showDeleteRejectedDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mantenimiento") },
                actions = {
                    IconButton(onClick = onOpenAlertSettings) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Configuración de Alertas"
                        )
                    }
                    IconButton(
                        onClick = {
                            uiState.selectedVehicle?.let { vehicle ->
                                viewModel.exportHistory(context, vehicle.plate)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Exportar CSV"
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
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Plan")
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
                    text = { Text("Planes (${uiState.plans.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Repuestos Instalados (${uiState.installedParts.size})") }
                )
            }

            if (selectedTab == 0) {
                if (uiState.plans.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Build,
                        title = "Sin planes registrados",
                        message = "Pulsa el botón + para crear un plan de mantenimiento preventivo para este vehículo."
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
                        title = "Sin repuestos instalados",
                        message = "Los repuestos y piezas asociados a los servicios realizados en este vehículo aparecerán aquí."
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
                        text = "Categoría: ${plan.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = muted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!plan.isActive) {
                        StatusChip(
                            text = "Pausado",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            icon = Icons.Default.PauseCircle
                        )
                    } else {
                        val statusInfo = when (planWithStatus.status) {
                            MaintenanceStatus.AL_DIA -> Triple("Al día", StatusSuccess, Icons.Default.CheckCircle)
                            MaintenanceStatus.PROXIMA -> Triple("Próxima", StatusWarning, Icons.Default.Warning)
                            MaintenanceStatus.VENCIDA -> Triple("Vencida", StatusError, Icons.Default.Info)
                            MaintenanceStatus.SIN_PROGRAMACION -> Triple("Sin programar", StatusInfo, Icons.Default.Info)
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
                    text = "Próxima fecha: ${dateFormat.format(Date(it))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }

            planWithStatus.nextLimitMileage?.let {
                Text(
                    text = "Próximo km: $it km",
                    style = MaterialTheme.typography.bodySmall,
                    color = muted
                )
            }

            if (plan.intervalMileage > 0 || plan.intervalMonths > 0) {
                val intervalDesc = buildList {
                    if (plan.intervalMileage > 0) add("cada ${plan.intervalMileage} km")
                    if (plan.intervalMonths > 0) add("cada ${plan.intervalMonths} meses")
                }.joinToString(" o ")
                Text(
                    text = "Frecuencia: $intervalDesc",
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
                    // Botón Pausar / Reactivar (RF-15)
                    IconButton(onClick = onToggleActive) {
                        Icon(
                            imageVector = if (plan.isActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (plan.isActive) "Pausar actividad" else "Reactivar actividad",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Botón Eliminar (RF-15)
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar actividad",
                            tint = StatusError
                        )
                    }
                }

                Button(
                    onClick = onRegisterService,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Registrar Servicio")
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
                    text = "$${String.format(Locale.US, "%.2f", part.cost * part.quantity)}",
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

            Text(
                text = "Cantidad: ${part.quantity}  (Unitario: $${String.format(Locale.US, "%.2f", part.cost)})",
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )
        }
    }
}