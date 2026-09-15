package com.micarro.app.feature.mileage.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.micarro.app.R
import com.micarro.app.core.ui.theme.BorderLight
import com.micarro.app.core.ui.theme.TextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MileageScreen(
    onBack: () -> Unit,
    viewModel: MileageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mileage_title)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.mileage_current),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = stringResource(
                                    R.string.mileage_km_format,
                                    uiState.vehicle?.currentMileage ?: 0L
                                ),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = uiState.input,
                            onValueChange = viewModel::onInputChange,
                            label = { Text(stringResource(R.string.mileage_new_reading)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = uiState.inputError,
                            supportingText = if (uiState.inputError) {
                                { Text(stringResource(R.string.error_mileage_negative)) }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.register() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.mileage_register))
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.mileage_history),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (uiState.readings.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.mileage_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                items(uiState.readings, key = { it.id }) { reading ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(
                                    R.string.mileage_km_format, reading.mileage
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = Instant.ofEpochMilli(reading.dateEpochMs)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                    .format(dateFormatter),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    HorizontalDivider(color = BorderLight)
                }
            }
        }
    }

    uiState.pendingLowerReading?.let { pending ->
        AlertDialog(
            onDismissRequest = viewModel::dismissLowerReading,
            shape = MaterialTheme.shapes.large,
            title = { Text(stringResource(R.string.mileage_lower_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.mileage_lower_message,
                        pending,
                        uiState.referenceMileage
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmLowerReading) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissLowerReading) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
