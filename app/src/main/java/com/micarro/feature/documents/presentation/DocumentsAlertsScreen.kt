package com.micarro.feature.documents.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.micarro.R
import com.micarro.domain.model.DocumentStatus
import com.micarro.feature.documents.domain.DocumentAlert
import com.micarro.ui.components.EmptyState
import com.micarro.ui.components.MiCarroCard
import com.micarro.ui.components.MiCarroTopBar
import com.micarro.ui.components.StatusChip
import com.micarro.ui.components.StatusTone
import com.micarro.ui.components.statusAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsAlertsScreen(
    onBack: () -> Unit,
    onOpenVehicleDocuments: (String) -> Unit,
    viewModel: DocumentsAlertsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            MiCarroTopBar(
                title = stringResource(R.string.alerts_title),
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MiCarroCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.alerts_global_switch))
                            Switch(
                                checked = state.settings.globalAlertsEnabled,
                                onCheckedChange = viewModel::setGlobalAlertsEnabled
                            )
                        }
                        Text(
                            stringResource(R.string.alerts_two_reminders),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            if (state.isLoading && state.alerts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (state.alerts.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.CheckCircle,
                        title = stringResource(R.string.alerts_empty_title),
                        message = stringResource(R.string.alerts_empty_message)
                    )
                }
            } else {
                items(state.alerts, key = { it.documentId }) { alert ->
                    AlertCard(alert = alert, onClick = {
                        onOpenVehicleDocuments(alert.vehicleId.toString())
                    })
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: DocumentAlert, onClick: () -> Unit) {
    val isExpired = alert.status == DocumentStatus.EXPIRED
    val cause = when {
        alert.daysUntilExpiration == 0L -> stringResource(R.string.alert_expires_today)
        alert.daysUntilExpiration < 0 -> pluralStringResource(
            R.plurals.alert_days_expired,
            (-alert.daysUntilExpiration).toInt(),
            (-alert.daysUntilExpiration).toInt()
        )
        else -> pluralStringResource(
            R.plurals.alert_days_remaining,
            alert.daysUntilExpiration.toInt(),
            alert.daysUntilExpiration.toInt()
        )
    }

    MiCarroCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isExpired) Icons.Filled.Error else Icons.Filled.Warning,
                contentDescription = null,
                tint = statusAccent(if (isExpired) StatusTone.Error else StatusTone.Warning)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    documentDisplayName(alert.documentType, alert.documentName),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    alert.vehiclePlate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    cause,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusAccent(if (isExpired) StatusTone.Error else StatusTone.Warning)
                )
            }
            StatusChip(
                text = stringResource(
                    if (isExpired) R.string.doc_status_expired
                    else R.string.doc_status_expiring
                ),
                tone = if (isExpired) StatusTone.Error else StatusTone.Warning,
                icon = if (isExpired) Icons.Filled.Error else Icons.Filled.Warning
            )
        }
    }
}
