package com.micarro.feature.mileage.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.micarro.R
import com.micarro.feature.mileage.domain.MileageError
import com.micarro.ui.components.DateField
import com.micarro.ui.components.EmptyState
import com.micarro.ui.components.MiCarroCard
import com.micarro.ui.components.MiCarroTextField
import com.micarro.ui.components.PrimaryButton
import com.micarro.ui.components.formatDate
import com.micarro.ui.theme.StatusError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MileageScreen(
    onBack: () -> Unit,
    viewModel: MileageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.vehicle?.let { "${it.plate} · " }?.plus(
                            stringResource(R.string.mileage_title)
                        ) ?: stringResource(R.string.mileage_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MiCarroCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.mileage_new_reading),
                            style = MaterialTheme.typography.titleMedium
                        )
                        MiCarroTextField(
                            value = state.odometerInput,
                            onValueChange = viewModel::onOdometerChange,
                            label = stringResource(R.string.mileage_odometer_field),
                            keyboardType = KeyboardType.Number,
                            errorMessage = when {
                                MileageError.EMPTY in state.errors ->
                                    stringResource(R.string.error_required)
                                MileageError.NOT_A_NUMBER in state.errors ->
                                    stringResource(R.string.error_number_invalid)
                                MileageError.NEGATIVE in state.errors ->
                                    stringResource(R.string.error_mileage_negative)
                                else -> null
                            }
                        )
                        DateField(
                            label = stringResource(R.string.mileage_date_field),
                            selectedDateMillis = state.dateMillis,
                            onDateSelected = viewModel::onDateChange,
                            errorMessage = if (MileageError.FUTURE_DATE in state.errors)
                                stringResource(R.string.error_future_date) else null
                        )
                        MiCarroTextField(
                            value = state.noteInput,
                            onValueChange = viewModel::onNoteChange,
                            label = stringResource(R.string.mileage_note_field)
                        )
                        if (state.saveFailed) {
                            Text(
                                stringResource(R.string.error_save_failed),
                                color = StatusError,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        PrimaryButton(
                            text = stringResource(R.string.mileage_register),
                            onClick = { viewModel.register() },
                            enabled = !state.isSaving
                        )
                    }
                }
            }

            item {
                Text(
                    stringResource(R.string.mileage_history),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.readings.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Speed,
                        title = stringResource(R.string.mileage_empty_title),
                        message = stringResource(R.string.mileage_empty_message)
                    )
                }
            } else {
                items(state.readings, key = { it.id }) { reading ->
                    MiCarroCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.mileage_reading_value, reading.reading),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                formatDate(reading.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            reading.note?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    state.pendingConfirmation?.let { previous ->
        AlertDialog(
            onDismissRequest = viewModel::dismissConfirmation,
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            title = { Text(stringResource(R.string.mileage_lower_warning_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.mileage_lower_warning_message,
                        state.odometerInput.toLongOrNull() ?: 0L,
                        previous
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmLowerReading) {
                    Text(stringResource(R.string.mileage_lower_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissConfirmation) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
