package com.micarro.app.feature.vehicle.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.micarro.app.R
import com.micarro.app.domain.model.FuelType
import com.micarro.app.domain.model.VehicleType
import com.micarro.app.feature.vehicle.domain.VehicleError
import com.micarro.app.feature.vehicle.domain.VehicleField
import com.micarro.app.feature.vehicle.domain.VehicleFieldError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFormScreen(
    onBack: () -> Unit,
    viewModel: VehicleFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = uiState.editingId != null

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isEditing) R.string.vehicle_edit else R.string.vehicle_add
                        )
                    )
                },
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
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.vehicle_section_data),
                style = MaterialTheme.typography.titleMedium
            )

            FormTextField(
                value = uiState.plate,
                onValueChange = { v -> viewModel.update { it.copy(plate = v.uppercase()) } },
                label = stringResource(R.string.field_plate),
                error = uiState.fieldErrors[VehicleField.PLATE]
            )

            EnumDropdown(
                label = stringResource(R.string.field_vehicle_type),
                options = VehicleType.entries.toList(),
                selected = uiState.type,
                optionLabel = { it.label() },
                onSelect = { v -> v?.let { t -> viewModel.update { s -> s.copy(type = t) } } }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextField(
                    value = uiState.brand,
                    onValueChange = { v -> viewModel.update { it.copy(brand = v) } },
                    label = stringResource(R.string.field_brand),
                    error = uiState.fieldErrors[VehicleField.BRAND],
                    modifier = Modifier.weight(1f)
                )
                FormTextField(
                    value = uiState.line,
                    onValueChange = { v -> viewModel.update { it.copy(line = v) } },
                    label = stringResource(R.string.field_line),
                    error = uiState.fieldErrors[VehicleField.LINE],
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextField(
                    value = uiState.model,
                    onValueChange = { v -> viewModel.update { it.copy(model = v) } },
                    label = stringResource(R.string.field_model),
                    error = uiState.fieldErrors[VehicleField.MODEL],
                    modifier = Modifier.weight(1f)
                )
                FormTextField(
                    value = uiState.year,
                    onValueChange = { v -> viewModel.update { it.copy(year = v.filter(Char::isDigit)) } },
                    label = stringResource(R.string.field_year),
                    error = uiState.fieldErrors[VehicleField.YEAR],
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            FormTextField(
                value = uiState.mileage,
                onValueChange = { v -> viewModel.update { it.copy(mileage = v.filter(Char::isDigit)) } },
                label = stringResource(R.string.field_mileage),
                error = uiState.fieldErrors[VehicleField.MILEAGE],
                keyboardType = KeyboardType.Number
            )

            Text(
                text = stringResource(R.string.vehicle_section_optional),
                style = MaterialTheme.typography.titleMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormTextField(
                    value = uiState.color,
                    onValueChange = { v -> viewModel.update { it.copy(color = v) } },
                    label = stringResource(R.string.field_color),
                    error = null,
                    modifier = Modifier.weight(1f)
                )
                FormTextField(
                    value = uiState.engineCc,
                    onValueChange = { v -> viewModel.update { it.copy(engineCc = v.filter(Char::isDigit)) } },
                    label = stringResource(R.string.field_engine_cc),
                    error = null,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            FormTextField(
                value = uiState.vin,
                onValueChange = { v -> viewModel.update { it.copy(vin = v.uppercase()) } },
                label = stringResource(R.string.field_vin),
                error = uiState.fieldErrors[VehicleField.VIN]
            )

            EnumDropdown(
                label = stringResource(R.string.field_fuel_type),
                options = FuelType.entries.toList(),
                selected = uiState.fuelType,
                optionLabel = { it.label() },
                onSelect = { v -> viewModel.update { it.copy(fuelType = v) } },
                allowNull = true
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.isPrimary,
                    onCheckedChange = { v -> viewModel.update { it.copy(isPrimary = v) } }
                )
                Text(
                    text = stringResource(R.string.vehicle_primary_badge),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSaving
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: VehicleFieldError?,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(fieldErrorText(it)) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun fieldErrorText(error: VehicleFieldError): String = when (error.error) {
    VehicleError.REQUIRED -> stringResource(R.string.error_required)
    VehicleError.PLATE_TAKEN -> stringResource(R.string.error_plate_taken)
    VehicleError.INVALID_VALUE -> when (error.field) {
        VehicleField.YEAR -> stringResource(R.string.error_year_invalid)
        VehicleField.MILEAGE -> stringResource(R.string.error_mileage_negative)
        VehicleField.VIN -> stringResource(R.string.error_vin_invalid)
        else -> stringResource(R.string.error_generic)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: @Composable (T) -> String,
    onSelect: (T?) -> Unit,
    allowNull: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let { optionLabel(it) }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (allowNull) {
                DropdownMenuItem(
                    text = { Text("—") },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    }
                )
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
