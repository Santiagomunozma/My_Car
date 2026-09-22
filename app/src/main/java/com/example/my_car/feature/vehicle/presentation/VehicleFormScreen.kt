package com.example.my_car.feature.vehicle.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.my_car.R
import com.example.my_car.domain.model.FuelType
import com.example.my_car.domain.model.VehicleType
import com.example.my_car.feature.vehicle.domain.VehicleError
import com.example.my_car.feature.vehicle.domain.VehicleField
import com.example.my_car.ui.components.MiCarroDropdownField
import com.example.my_car.ui.components.MiCarroTextField
import com.example.my_car.ui.components.PrimaryButton
import com.example.my_car.ui.components.SecondaryButton
import com.example.my_car.ui.theme.StatusError
import java.io.File

fun fuelTypeLabel(type: FuelType): Int = when (type) {
    FuelType.GASOLINE -> R.string.fuel_type_gasoline
    FuelType.DIESEL -> R.string.fuel_type_diesel
    FuelType.GAS -> R.string.fuel_type_gas
    FuelType.ELECTRIC -> R.string.fuel_type_electric
    FuelType.HYBRID -> R.string.fuel_type_hybrid
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFormScreen(
    onBack: () -> Unit,
    viewModel: VehicleFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.onPhotoPicked(it.toString()) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isEditing) R.string.vehicles_edit_title
                            else R.string.vehicles_add
                        )
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
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fotografía (RF-02)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.draft.photoUri != null) {
                        AsyncImage(
                            model = File(state.draft.photoUri!!),
                            contentDescription = stringResource(R.string.vehicle_photo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxSize(0.4f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(
                        text = stringResource(R.string.vehicle_photo_pick),
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        enabled = !state.isImportingPhoto,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.draft.photoUri != null) {
                        SecondaryButton(
                            text = stringResource(R.string.vehicle_photo_remove),
                            onClick = viewModel::removePhoto,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (state.photoImportFailed) {
                    Text(
                        stringResource(R.string.vehicle_photo_error),
                        color = StatusError,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                MiCarroDropdownField(
                    label = stringResource(R.string.field_type),
                    options = VehicleType.entries,
                    selected = state.draft.type,
                    optionLabel = { stringResource(vehicleTypeLabel(it)) },
                    onSelected = { type ->
                        viewModel.updateDraft { it.copy(type = type) }
                    }
                )

                MiCarroTextField(
                    value = state.draft.plate,
                    onValueChange = {
                        viewModel.updateDraft { d -> d.copy(plate = it) }
                        viewModel.clearFieldError(VehicleField.PLATE)
                    },
                    label = stringResource(R.string.field_plate),
                    errorMessage = state.errors[VehicleField.PLATE]?.let { errorText(it) }
                )
                MiCarroTextField(
                    value = state.draft.brand,
                    onValueChange = {
                        viewModel.updateDraft { d -> d.copy(brand = it) }
                        viewModel.clearFieldError(VehicleField.BRAND)
                    },
                    label = stringResource(R.string.field_brand),
                    errorMessage = state.errors[VehicleField.BRAND]?.let { errorText(it) }
                )
                MiCarroTextField(
                    value = state.draft.line,
                    onValueChange = {
                        viewModel.updateDraft { d -> d.copy(line = it) }
                        viewModel.clearFieldError(VehicleField.LINE)
                    },
                    label = stringResource(R.string.field_line),
                    errorMessage = state.errors[VehicleField.LINE]?.let { errorText(it) }
                )
                MiCarroTextField(
                    value = state.draft.model,
                    onValueChange = {
                        viewModel.updateDraft { d -> d.copy(model = it) }
                        viewModel.clearFieldError(VehicleField.MODEL)
                    },
                    label = stringResource(R.string.field_model),
                    errorMessage = state.errors[VehicleField.MODEL]?.let { errorText(it) }
                )
                MiCarroTextField(
                    value = state.draft.year,
                    onValueChange = {
                        viewModel.updateDraft { d -> d.copy(year = it) }
                        viewModel.clearFieldError(VehicleField.YEAR)
                    },
                    label = stringResource(R.string.field_year),
                    keyboardType = KeyboardType.Number,
                    errorMessage = state.errors[VehicleField.YEAR]?.let { errorText(it) }
                )
                MiCarroTextField(
                    value = state.draft.mileage,
                    onValueChange = {
                        viewModel.updateDraft { d -> d.copy(mileage = it) }
                        viewModel.clearFieldError(VehicleField.MILEAGE)
                    },
                    label = stringResource(R.string.field_mileage),
                    keyboardType = KeyboardType.Number,
                    readOnly = state.isEditing,
                    supportingText = if (state.isEditing)
                        stringResource(R.string.field_mileage_readonly_hint) else null,
                    errorMessage = state.errors[VehicleField.MILEAGE]?.let { errorText(it) }
                )

                MiCarroDropdownField(
                    label = stringResource(R.string.field_fuel_type),
                    options = listOf<FuelType?>(null) + FuelType.entries,
                    selected = state.draft.fuelType,
                    optionLabel = {
                        it?.let { t -> stringResource(fuelTypeLabel(t)) }
                            ?: stringResource(R.string.field_none)
                    },
                    onSelected = { fuel ->
                        viewModel.updateDraft { it.copy(fuelType = fuel) }
                    }
                )

                MiCarroTextField(
                    value = state.draft.color,
                    onValueChange = { viewModel.updateDraft { d -> d.copy(color = it) } },
                    label = stringResource(R.string.field_color)
                )
                MiCarroTextField(
                    value = state.draft.vin,
                    onValueChange = {
                        viewModel.updateDraft { d -> d.copy(vin = it) }
                        viewModel.clearFieldError(VehicleField.VIN)
                    },
                    label = stringResource(R.string.field_vin),
                    errorMessage = state.errors[VehicleField.VIN]?.let { errorText(it) }
                )
                MiCarroTextField(
                    value = state.draft.engineCc,
                    onValueChange = {
                        viewModel.updateDraft { d -> d.copy(engineCc = it) }
                        viewModel.clearFieldError(VehicleField.ENGINE_CC)
                    },
                    label = stringResource(R.string.field_engine_cc),
                    keyboardType = KeyboardType.Number,
                    errorMessage = state.errors[VehicleField.ENGINE_CC]?.let { errorText(it) }
                )

                if (state.saveFailed) {
                    Text(
                        stringResource(
                            if (state.vehicleGone) R.string.error_vehicle_gone
                            else R.string.error_save_failed
                        ),
                        color = StatusError,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                PrimaryButton(
                    text = stringResource(R.string.action_save),
                    onClick = viewModel::onSave,
                    enabled = !state.isSaving && !state.isImportingPhoto
                )
            }
        }
    }
}

@Composable
private fun errorText(error: VehicleError): String = stringResource(
    when (error) {
        VehicleError.REQUIRED -> R.string.error_required
        VehicleError.INVALID_FORMAT -> R.string.error_plate_format
        VehicleError.INVALID_VALUE -> R.string.error_number_invalid
        VehicleError.DUPLICATE_PLATE -> R.string.error_plate_duplicate
    }
)
