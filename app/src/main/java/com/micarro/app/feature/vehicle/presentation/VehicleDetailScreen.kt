package com.micarro.app.feature.vehicle.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onMileage: (Long) -> Unit,
    onDocuments: (Long) -> Unit,
    viewModel: VehicleDetailViewModel = hiltViewModel()
) {
    val vehicle by viewModel.vehicle.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vehicle_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    vehicle?.let { v ->
                        IconButton(onClick = { onEdit(v.id) }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.action_edit)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        val v = vehicle ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                imageVector = v.type.icon(),
                                contentDescription = v.type.label(),
                                modifier = Modifier
                                    .size(56.dp)
                                    .padding(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.padding(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = v.plate,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (v.isPrimary) {
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    StatusChip(
                                        text = stringResource(R.string.vehicle_primary_badge),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (v.isArchived) {
                                    Spacer(modifier = Modifier.padding(4.dp))
                                    StatusChip(
                                        text = stringResource(R.string.vehicle_archived_badge),
                                        color = TextSecondary
                                    )
                                }
                            }
                            Text(
                                text = v.type.label(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow(stringResource(R.string.field_brand), v.brand)
                    InfoRow(stringResource(R.string.field_line), v.line)
                    InfoRow(stringResource(R.string.field_model), v.model)
                    InfoRow(stringResource(R.string.field_year), v.year.toString())
                    InfoRow(
                        stringResource(R.string.field_mileage),
                        stringResource(R.string.mileage_km_format, v.currentMileage)
                    )
                    v.color?.let { InfoRow(stringResource(R.string.field_color), it) }
                    v.vin?.let { InfoRow(stringResource(R.string.field_vin), it) }
                    v.fuelType?.let { InfoRow(stringResource(R.string.field_fuel_type), it.label()) }
                    v.engineCc?.let {
                        InfoRow(stringResource(R.string.field_engine_cc), it.toString())
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onMileage(v.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Speed, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(stringResource(R.string.mileage_title))
                }
                OutlinedButton(
                    onClick = { onDocuments(v.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(stringResource(R.string.documents_title))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
