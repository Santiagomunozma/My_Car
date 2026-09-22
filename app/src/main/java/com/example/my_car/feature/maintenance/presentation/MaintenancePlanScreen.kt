package com.example.my_car.feature.maintenance.presentation

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.my_car.feature.maintenance.domain.MaintenanceStatus
import com.example.my_car.ui.components.MiCarroCard
import com.example.my_car.ui.components.StatusChip
import com.example.my_car.ui.theme.StatusError
import com.example.my_car.ui.theme.StatusInfo
import com.example.my_car.ui.theme.StatusSuccess
import com.example.my_car.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenancePlanScreen(
    viewModel: MaintenanceViewModel,
    onAddPlan: () -> Unit,
    onRegisterService: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan de Mantenimiento") },
                actions = {
                    IconButton(
                        onClick = {
                            uiState.selectedVehicle?.plate?.let { plate ->
                                viewModel.exportHistory(context, plate)
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
            FloatingActionButton(
                onClick = onAddPlan,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Plan")
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.plans) { planWithStatus ->
                    MaintenancePlanCard(
                        planWithStatus = planWithStatus,
                        dateFormat = dateFormat,
                        onRegisterService = { onRegisterService(planWithStatus.plan.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleSelector(
    vehicles: List<com.example.my_car.domain.model.Vehicle>,
    selectedVehicle: com.example.my_car.domain.model.Vehicle?,
    onVehicleSelected: (com.example.my_car.domain.model.Vehicle) -> Unit
) {
    if (vehicles.isNotEmpty()) {
        val safeIndex = vehicles.indexOf(selectedVehicle).coerceIn(0, vehicles.size - 1)

        ScrollableTabRow(
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
    onRegisterService: () -> Unit
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    MiCarroCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = planWithStatus.plan.title,
                    style = MaterialTheme.typography.titleMedium
                )

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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Categoría: ${planWithStatus.plan.category}",
                style = MaterialTheme.typography.bodyMedium,
                color = muted
            )

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

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRegisterService,
                modifier = Modifier.align(Alignment.End),
                shape = MaterialTheme.shapes.small
            ) {
                Text("Registrar Servicio")
            }
        }
    }
}