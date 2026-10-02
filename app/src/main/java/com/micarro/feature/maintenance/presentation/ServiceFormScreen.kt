package com.micarro.feature.maintenance.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.micarro.R
import com.micarro.domain.model.EvidenceCodec
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import com.micarro.domain.model.ServiceType
import com.micarro.feature.maintenance.domain.MaintenanceRules
import com.micarro.feature.parts.presentation.PartFormScreen
import com.micarro.ui.components.DateField
import com.micarro.ui.components.MiCarroDropdownField
import com.micarro.ui.components.MiCarroTextField
import com.micarro.ui.components.PrimaryButton
import com.micarro.ui.components.SecondaryButton
import com.micarro.ui.theme.StatusWarning
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceFormScreen(
    vehicleId: String,
    planId: String?,
    lastMileage: Int,
    initialCategory: String,
    initialService: MaintenanceService? = null,
    initialParts: List<Part> = emptyList(),
    evidencePath: String? = null,
    evidencePaths: List<String> = emptyList(),
    onPickEvidence: () -> Unit = {},
    onRemoveEvidence: (String) -> Unit = {},
    onSave: (MaintenanceService, List<Part>, Boolean, Boolean) -> Unit,
    onBack: () -> Unit
) {
    var title by remember(initialService?.id) { mutableStateOf(initialService?.title ?: "") }
    var serviceType by remember(initialService?.id) {
        mutableStateOf(initialService?.serviceType ?: if (planId != null) ServiceType.PREVENTIVE else ServiceType.CORRECTIVE)
    }
    var category by remember(initialService?.id, initialCategory) {
        mutableStateOf(initialService?.category ?: initialCategory.ifBlank { DEFAULT_CATEGORIES.first() })
    }
    var customCategory by remember { mutableStateOf("") }
    var workshop by remember(initialService?.id) { mutableStateOf(initialService?.workshopName ?: "") }
    var mileage by remember(initialService?.id) {
        mutableStateOf((initialService?.mileage ?: lastMileage).toString())
    }
    var laborCost by remember(initialService?.id) {
        mutableStateOf(initialService?.laborCost?.takeIf { it != 0.0 }?.toString() ?: "")
    }
    var otherCost by remember(initialService?.id) {
        mutableStateOf(initialService?.otherCosts?.takeIf { it != 0.0 }?.toString() ?: "")
    }
    var totalOverride by remember(initialService?.id) { mutableStateOf("") }
    var selectedDateMillis by remember(initialService?.id) {
        mutableStateOf(initialService?.date ?: System.currentTimeMillis())
    }
    val parts = remember(initialService?.id) { mutableStateListOf<Part>().apply { addAll(initialParts) } }
    var showMileageConfirmDialog by remember { mutableStateOf(false) }
    var showTotalConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialParts) {
        if (parts.isEmpty() && initialParts.isNotEmpty()) {
            parts.clear()
            parts.addAll(initialParts)
        }
    }

    val currentMileage = mileage.toIntOrNull() ?: 0
    val currentTime = System.currentTimeMillis()
    val isDateValid = MaintenanceRules.isDateValid(selectedDateMillis, currentTime)
    val isMileageLower = !MaintenanceRules.isMileageValid(currentMileage, lastMileage)
    val labor = laborCost.toDoubleOrNull() ?: 0.0
    val other = otherCost.toDoubleOrNull() ?: 0.0
    val partsCost = parts.sumOf { it.cost * it.quantity }
    val calculatedTotal = MaintenanceRules.calculateTotalCost(labor, partsCost, other)
    val enteredTotal = totalOverride.toDoubleOrNull()
    val totalAdjusted = enteredTotal != null && abs(enteredTotal - calculatedTotal) > 0.009
    val totalToStore = if (totalAdjusted) enteredTotal!! else calculatedTotal
    val amountsValid = MaintenanceRules.isAmountValid(labor) &&
        MaintenanceRules.isAmountValid(other) &&
        parts.all { it.quantity >= 0 && MaintenanceRules.isAmountValid(it.cost) }
    val resolvedCategory = customCategory.trim().ifBlank { category }
    val currency = stringResource(R.string.currency_symbol)

    fun buildService() = MaintenanceService(
        id = initialService?.id ?: java.util.UUID.randomUUID().toString(),
        vehicleId = vehicleId,
        planId = planId ?: initialService?.planId,
        title = title.trim(),
        category = resolvedCategory,
        date = selectedDateMillis,
        mileage = currentMileage,
        laborCost = labor,
        otherCosts = other,
        totalCost = totalToStore,
        workshopName = workshop.trim(),
        serviceType = serviceType,
        evidenceUri = EvidenceCodec.encode(
            evidencePaths.ifEmpty { EvidenceCodec.decode(evidencePath ?: initialService?.evidenceUri) }
        ),
        description = title.trim()
    )

    if (showMileageConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showMileageConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning) },
            title = { Text(stringResource(R.string.service_mileage_warning_title)) },
            text = { Text(stringResource(R.string.service_mileage_warning, currentMileage, lastMileage)) },
            confirmButton = {
                TextButton(onClick = {
                    showMileageConfirmDialog = false
                    if (totalAdjusted) showTotalConfirmDialog = true
                    else onSave(buildService(), parts.toList(), true, false)
                }) { Text(stringResource(R.string.service_confirm_anyway)) }
            },
            dismissButton = {
                TextButton(onClick = { showMileageConfirmDialog = false }) {
                    Text(stringResource(R.string.service_fix))
                }
            }
        )
    }

    if (showTotalConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showTotalConfirmDialog = false },
            title = { Text(stringResource(R.string.service_total_warning_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.service_total_warning,
                        formatMoney(calculatedTotal, currency),
                        formatMoney(totalToStore, currency)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showTotalConfirmDialog = false
                    onSave(buildService(), parts.toList(), isMileageLower, true)
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showTotalConfirmDialog = false }) {
                    Text(stringResource(R.string.service_fix))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (initialService == null) R.string.service_register_title
                            else R.string.service_edit_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isMileageLower && currentMileage > 0) {
                Surface(
                    color = StatusWarning.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.service_mileage_banner),
                            color = StatusWarning,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Text(stringResource(R.string.service_type), style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = serviceType == ServiceType.PREVENTIVE,
                    onClick = { serviceType = ServiceType.PREVENTIVE },
                    label = { Text(stringResource(R.string.service_preventive)) }
                )
                FilterChip(
                    selected = serviceType == ServiceType.CORRECTIVE,
                    onClick = { serviceType = ServiceType.CORRECTIVE },
                    label = { Text(stringResource(R.string.service_corrective)) }
                )
            }

            DateField(
                label = stringResource(R.string.service_date),
                selectedDateMillis = selectedDateMillis,
                onDateSelected = { selectedDateMillis = it },
                errorMessage = if (!isDateValid) stringResource(R.string.service_future_date) else null
            )

            MiCarroTextField(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.service_description),
                placeholder = stringResource(R.string.maintenance_optional)
            )
            MiCarroDropdownField(
                label = stringResource(R.string.maintenance_category),
                options = DEFAULT_CATEGORIES,
                selected = category,
                optionLabel = { it },
                onSelected = { category = it }
            )
            MiCarroTextField(
                value = customCategory,
                onValueChange = { customCategory = it },
                label = stringResource(R.string.maintenance_custom_category)
            )
            MiCarroTextField(
                value = workshop,
                onValueChange = { workshop = it },
                label = stringResource(R.string.service_workshop)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiCarroTextField(
                    value = mileage,
                    onValueChange = { mileage = it.filter { ch -> ch.isDigit() } },
                    label = stringResource(R.string.service_mileage),
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    isError = isMileageLower
                )
                MiCarroTextField(
                    value = laborCost,
                    onValueChange = { laborCost = it },
                    label = stringResource(R.string.service_labor),
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }
            MiCarroTextField(
                value = otherCost,
                onValueChange = { otherCost = it },
                label = stringResource(R.string.service_other),
                keyboardType = KeyboardType.Decimal
            )

            HorizontalDivider()
            PartFormScreen(
                parts = parts,
                onAddPart = { parts.add(it) },
                onRemovePart = { parts.remove(it) }
            )

            Text(
                text = stringResource(R.string.service_total, formatMoney(calculatedTotal, currency)),
                style = MaterialTheme.typography.titleMedium
            )
            MiCarroTextField(
                value = totalOverride,
                onValueChange = { totalOverride = it },
                label = stringResource(R.string.service_adjust_total),
                keyboardType = KeyboardType.Decimal
            )
            if (!amountsValid) {
                Text(
                    stringResource(R.string.service_negative_amount),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            val attached = evidencePaths.ifEmpty { EvidenceCodec.decode(evidencePath ?: initialService?.evidenceUri) }
            attached.forEach { path ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        path.substringAfterLast('/'),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onRemoveEvidence(path) }) {
                        Text(stringResource(R.string.action_delete))
                    }
                }
            }
            SecondaryButton(
                text = stringResource(
                    if (attached.isEmpty()) R.string.service_evidence else R.string.service_evidence_add
                ),
                onClick = onPickEvidence
            )

            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = stringResource(
                    if (initialService == null) R.string.service_confirm else R.string.service_save_changes
                ),
                enabled = title.isNotBlank() && isDateValid && amountsValid,
                onClick = {
                    when {
                        isMileageLower -> showMileageConfirmDialog = true
                        totalAdjusted -> showTotalConfirmDialog = true
                        else -> onSave(buildService(), parts.toList(), false, false)
                    }
                }
            )
        }
    }
}

private fun formatMoney(value: Double, symbol: String): String =
    symbol + String.format(Locale.US, "%.2f", value)
