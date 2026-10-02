package com.micarro.feature.alerts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.micarro.R
import com.micarro.feature.maintenance.presentation.MaintenanceViewModel
import com.micarro.ui.components.MiCarroTextField
import com.micarro.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSettingsScreen(
    viewModel: MaintenanceViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var days by remember(uiState.alertMarginDays) { mutableStateOf(uiState.alertMarginDays.toString()) }
    var km by remember(uiState.alertMarginKm) { mutableStateOf(uiState.alertMarginKm.toString()) }
    var enabled by remember(uiState.maintenanceAlertsEnabled) { mutableStateOf(uiState.maintenanceAlertsEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.maintenance_alerts_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.maintenance_alerts_help),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.maintenance_alerts_switch),
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
            MiCarroTextField(
                value = days,
                onValueChange = { days = it.filter { ch -> ch.isDigit() } },
                label = stringResource(R.string.maintenance_alerts_days),
                keyboardType = KeyboardType.Number,
                enabled = enabled
            )
            MiCarroTextField(
                value = km,
                onValueChange = { km = it.filter { ch -> ch.isDigit() } },
                label = stringResource(R.string.maintenance_alerts_km),
                keyboardType = KeyboardType.Number,
                enabled = enabled
            )
            PrimaryButton(
                text = stringResource(R.string.maintenance_alerts_save),
                onClick = {
                    viewModel.updateAlertSettings(
                        marginDays = days.toIntOrNull() ?: 15,
                        marginKm = km.toIntOrNull() ?: 500,
                        enabled = enabled
                    )
                    onBack()
                }
            )
        }
    }
}
