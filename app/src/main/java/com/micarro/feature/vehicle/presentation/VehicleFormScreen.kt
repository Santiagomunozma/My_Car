package com.micarro.feature.vehicle.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.micarro.R
import com.micarro.domain.model.FuelType
import com.micarro.feature.vehicle.domain.VehicleError
import com.micarro.feature.vehicle.domain.VehicleField
import com.micarro.feature.vehicle.domain.VehicleRules
import com.micarro.ui.components.MiCarroDropdownField
import com.micarro.ui.components.MiCarroTextField
import com.micarro.ui.components.PrimaryButton
import com.micarro.ui.components.SecondaryButton
import com.micarro.ui.theme.StatusError
import java.io.File

fun fuelTypeLabel(type: FuelType): Int = when (type) {
    FuelType.GASOLINE -> R.string.fuel_type_gasoline
    FuelType.DIESEL -> R.string.fuel_type_diesel
    FuelType.GAS -> R.string.fuel_type_gas
    FuelType.ELECTRIC -> R.string.fuel_type_electric
    FuelType.HYBRID -> R.string.fuel_type_hybrid
}

@Composable
fun ColorDot(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = CircleShape
            )
    )
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Fotografía del Vehículo
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

                // 2. Selector de Tipo de Vehículo (CARRO / MOTO)
                Text(
                    text = "Tipo de Vehículo",
                    style = MaterialTheme.typography.titleMedium
                )
                val typeOptions = listOf(
                    "CARRO" to "Automóvil / Camioneta",
                    "MOTO" to "Motocicleta"
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    typeOptions.forEachIndexed { index, (key, label) ->
                        SegmentedButton(
                            selected = state.typeKey == key,
                            onClick = { viewModel.onTypeSelected(key) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = typeOptions.size
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // 3. Campo de Placa
                val plateErrorText = when {
                    state.errors.containsKey(VehicleField.PLATE) -> {
                        when (state.errors[VehicleField.PLATE]) {
                            VehicleError.REQUIRED -> stringResource(R.string.error_required)
                            VehicleError.DUPLICATE_PLATE -> stringResource(R.string.error_plate_duplicate)
                            else -> if (state.typeKey == "MOTO") {
                                "Formato inválido. Ejemplo: AAA12A (3 letras, 2 números y 1 letra)"
                            } else {
                                "Formato inválido. Ejemplo: AAA123 (3 letras y 3 números)"
                            }
                        }
                    }
                    state.draft.plate.isNotBlank() && !VehicleRules.isPlateValid(state.draft.plate, state.typeKey) -> {
                        if (state.typeKey == "MOTO") {
                            "Formato inválido. Ejemplo: AAA12A (3 letras, 2 números y 1 letra)"
                        } else {
                            "Formato inválido. Ejemplo: AAA123 (3 letras y 3 números)"
                        }
                    }
                    else -> null
                }

                MiCarroTextField(
                    value = state.draft.plate,
                    onValueChange = viewModel::onPlateChanged,
                    label = stringResource(R.string.field_plate),
                    errorMessage = plateErrorText,
                    supportingText = if (plateErrorText == null) {
                        if (state.typeKey == "MOTO") "Ejemplo: AAA12A (máximo 6 caracteres)"
                        else "Ejemplo: AAA123 (máximo 6 caracteres)"
                    } else null,
                    singleLine = true
                )

                // 4. Desplegables en Cascada: Marca, Línea, Año del Modelo
                MiCarroDropdownField(
                    label = stringResource(R.string.field_brand),
                    options = state.availableBrands,
                    selected = state.draft.brand.ifEmpty { null },
                    placeholder = "Selecciona una marca",
                    optionLabel = { it },
                    onSelected = viewModel::onBrandSelected,
                    errorMessage = state.errors[VehicleField.BRAND]?.let { errorText(it) }
                )

                MiCarroDropdownField(
                    label = stringResource(R.string.field_line),
                    options = state.availableLines,
                    selected = state.draft.line.ifEmpty { null },
                    enabled = state.draft.brand.isNotBlank(),
                    placeholder = if (state.draft.brand.isBlank()) "Selecciona una marca primero" else "Selecciona una línea",
                    optionLabel = { it },
                    onSelected = viewModel::onLineSelected,
                    errorMessage = state.errors[VehicleField.LINE]?.let { errorText(it) }
                )

                MiCarroDropdownField(
                    label = stringResource(R.string.field_year),
                    options = state.availableYears,
                    selected = state.draft.year.ifEmpty { null },
                    placeholder = "Selecciona el año",
                    optionLabel = { it },
                    onSelected = viewModel::onYearSelected,
                    errorMessage = state.errors[VehicleField.YEAR]?.let { errorText(it) }
                )

                // 5. Desplegable de Color Visual
                val selectedColorOption = VehicleColors.findByName(state.draft.color)
                MiCarroDropdownField(
                    label = stringResource(R.string.field_color),
                    options = VehicleColors.options,
                    selected = selectedColorOption,
                    placeholder = "Selecciona un color",
                    optionLabel = { it.name },
                    onSelected = { viewModel.onColorSelected(it.name) },
                    leadingIcon = selectedColorOption?.let { colorOpt ->
                        { ColorDot(color = colorOpt.color) }
                    },
                    optionContent = { colorOpt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ColorDot(color = colorOpt.color)
                            Text(
                                text = colorOpt.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                )

                // 6. Kilometraje actual (sólo editable en alta)
                MiCarroTextField(
                    value = state.draft.mileage,
                    onValueChange = viewModel::onMileageChanged,
                    label = stringResource(R.string.field_mileage),
                    keyboardType = KeyboardType.Number,
                    readOnly = state.isEditing,
                    supportingText = if (state.isEditing)
                        stringResource(R.string.field_mileage_readonly_hint) else null,
                    errorMessage = state.errors[VehicleField.MILEAGE]?.let { errorText(it) }
                )

                // 7. Tipo de Combustible
                MiCarroDropdownField(
                    label = stringResource(R.string.field_fuel_type),
                    options = listOf<FuelType?>(null) + FuelType.entries,
                    selected = state.draft.fuelType,
                    optionLabel = {
                        it?.let { t -> stringResource(fuelTypeLabel(t)) }
                            ?: stringResource(R.string.field_none)
                    },
                    onSelected = viewModel::onFuelTypeSelected
                )

                // 8. VIN / Número de Chasis
                MiCarroTextField(
                    value = state.draft.vin,
                    onValueChange = viewModel::onVinChanged,
                    label = stringResource(R.string.field_vin),
                    supportingText = "17 caracteres alfanuméricos (opcional)",
                    errorMessage = state.errors[VehicleField.VIN]?.let { errorText(it) }
                )

                // 9. Cilindraje (cc)
                MiCarroTextField(
                    value = state.draft.engineCc,
                    onValueChange = viewModel::onEngineCcChanged,
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

                // 10. Botón Guardar (habilitado únicamente si el formulario es válido)
                PrimaryButton(
                    text = stringResource(R.string.action_save),
                    onClick = viewModel::onSave,
                    enabled = state.isFormValid && !state.isSaving && !state.isImportingPhoto
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
