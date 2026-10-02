package com.micarro.feature.maintenance.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.micarro.R
import com.micarro.domain.model.EvidenceCodec
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import com.micarro.feature.maintenance.domain.RegisterServiceResult
import com.micarro.feature.maintenance.domain.ServiceError
import kotlinx.coroutines.launch

@Composable
fun ServiceFormRoute(
    vehicleId: String,
    planId: String?,
    serviceId: String?,
    lastMileage: Int,
    viewModel: MaintenanceViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var initialService by remember { mutableStateOf<MaintenanceService?>(null) }
    var initialParts by remember { mutableStateOf<List<Part>>(emptyList()) }
    var category by remember { mutableStateOf(DEFAULT_CATEGORIES.first()) }
    var evidencePaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var askEarlierDate by remember { mutableStateOf(false) }
    var pendingSave by remember { mutableStateOf<PendingServiceSave?>(null) }
    var ready by remember { mutableStateOf(serviceId.isNullOrBlank() && planId.isNullOrBlank()) }
    val negativeAmount = stringResource(R.string.service_negative_amount)
    val futureDate = stringResource(R.string.service_future_date)
    val evidenceError = stringResource(R.string.service_evidence_error)

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val path = viewModel.importEvidence(uri.toString())
            if (path == null) snackbarHostState.showSnackbar(evidenceError) else {
                evidencePaths = (evidencePaths + path).distinct()
            }
        }
    }

    LaunchedEffect(serviceId, planId) {
        if (!serviceId.isNullOrBlank()) {
            viewModel.loadService(serviceId)?.let { (service, parts) ->
                initialService = service
                initialParts = parts
                category = service.category
                evidencePaths = EvidenceCodec.decode(service.evidenceUri)
            }
        } else if (!planId.isNullOrBlank()) {
            viewModel.loadPlan(planId)?.let { category = it.category }
        }
        ready = true
    }

    LaunchedEffect(Unit) {
        viewModel.serviceEvents.collect { result ->
            when (result) {
                RegisterServiceResult.Success -> onBack()
                is RegisterServiceResult.Invalid -> {
                    val message = when {
                        ServiceError.FUTURE_DATE in result.errors -> futureDate
                        ServiceError.NEGATIVE_AMOUNT in result.errors -> negativeAmount
                        else -> futureDate
                    }
                    snackbarHostState.showSnackbar(message)
                }
                is RegisterServiceResult.RequiresEarlierDate -> askEarlierDate = true
                else -> Unit
            }
        }
    }

    if (!ready) return

    androidx.compose.foundation.layout.Box {
        ServiceFormScreen(
            vehicleId = vehicleId,
            planId = planId ?: initialService?.planId,
            lastMileage = lastMileage,
            initialCategory = category,
            initialService = initialService,
            initialParts = initialParts,
            evidencePaths = evidencePaths,
            onPickEvidence = { picker.launch("*/*") },
            onRemoveEvidence = { path -> evidencePaths = evidencePaths.filterNot { it == path } },
            onSave = { service, parts, mileageConfirmed, totalConfirmed ->
                pendingSave = PendingServiceSave(service, parts, mileageConfirmed, totalConfirmed)
                viewModel.submitService(
                    service = service,
                    parts = parts,
                    confirmedLowerMileage = mileageConfirmed,
                    confirmedAdjustedTotal = totalConfirmed,
                    confirmedEarlierDate = false,
                    isUpdate = initialService != null
                )
            },
            onBack = onBack
        )
        SnackbarHost(hostState = snackbarHostState)
        if (askEarlierDate) {
            AlertDialog(
                onDismissRequest = { askEarlierDate = false },
                title = { Text(stringResource(R.string.service_earlier_date_title)) },
                text = { Text(stringResource(R.string.service_earlier_date)) },
                confirmButton = {
                    TextButton(onClick = {
                        askEarlierDate = false
                        pendingSave?.let { pending ->
                            viewModel.submitService(
                                service = pending.service,
                                parts = pending.parts,
                                confirmedLowerMileage = pending.mileageConfirmed,
                                confirmedAdjustedTotal = pending.totalConfirmed,
                                confirmedEarlierDate = true,
                                isUpdate = initialService != null
                            )
                        }
                    }) { Text(stringResource(R.string.service_confirm_anyway)) }
                },
                dismissButton = {
                    TextButton(onClick = { askEarlierDate = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    }
}

private data class PendingServiceSave(
    val service: MaintenanceService,
    val parts: List<Part>,
    val mileageConfirmed: Boolean,
    val totalConfirmed: Boolean
)
