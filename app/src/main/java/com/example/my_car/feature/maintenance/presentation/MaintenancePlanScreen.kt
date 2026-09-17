package com.example.my_car.feature.maintenance.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.my_car.feature.maintenance.domain.MaintenanceStatus
import com.example.my_car.ui.components.StatusChip
import com.example.my_car.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenancePlanScreen(
    viewModel: MaintenanceViewModel,
    onAddPlan: () -> Unit,
    onRegisterService: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan de Mantenimiento", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlan,
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Plan")
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Selector de vehículo
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
    ScrollableTabRow(
        selectedTabIndex = vehicles.indexOf(selectedVehicle).coerceAtLeast(0),
        containerColor = SurfaceWhite,
        contentColor = PrimaryBlue,
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

@Composable
fun MaintenancePlanCard(
    planWithStatus: MaintenancePlanWithStatus,
    dateFormat: SimpleDateFormat,
    onRegisterService: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = planWithStatus.plan.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
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
                color = TextSecondary
            )
            
            planWithStatus.nextDeadlineDate?.let {
                Text(
                    text = "Próxima fecha: ${dateFormat.format(Date(it))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            planWithStatus.nextLimitMileage?.let {
                Text(
                    text = "Próximo km: $it km",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onRegisterService,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Registrar Servicio")
            }
        }
    }
}
