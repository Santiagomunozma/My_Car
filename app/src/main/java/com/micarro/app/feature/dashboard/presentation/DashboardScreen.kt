package com.micarro.app.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.micarro.app.R
import com.micarro.app.core.ui.components.StatusChip
import com.micarro.app.core.ui.theme.TextSecondary
import com.micarro.app.feature.vehicle.presentation.icon
import com.micarro.app.feature.vehicle.presentation.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onVehicleClick: (Long) -> Unit,
    onAddVehicle: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val primary = uiState.primaryVehicle
            if (primary == null && !uiState.loading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.dashboard_no_primary),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.dashboard_add_first),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddVehicle,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.vehicle_add))
                        }
                    }
                }
            } else if (primary != null) {
                Card(
                    onClick = { onVehicleClick(primary.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
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
                                imageVector = primary.type.icon(),
                                contentDescription = primary.type.label(),
                                modifier = Modifier
                                    .size(56.dp)
                                    .padding(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = primary.plate,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (primary.isPrimary) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusChip(
                                        text = stringResource(R.string.vehicle_primary_badge),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "${primary.brand} ${primary.line} ${primary.model}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = stringResource(
                                    R.string.dashboard_mileage, primary.currentMileage
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.dashboard_vehicle_count, uiState.vehicleCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
