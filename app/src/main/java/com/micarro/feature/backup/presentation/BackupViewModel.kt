package com.micarro.feature.backup.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.R
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.domain.usecase.BackupUseCase
import com.micarro.feature.alerts.domain.AlertScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupUseCase: BackupUseCase,
    private val maintenanceRepository: MaintenanceRepository,
    private val vehicleRepository: VehicleRepository,
    private val alertScheduler: AlertScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = context.getString(R.string.backup_open_error)) }
                return@launch
            }

            backupUseCase.export(outputStream)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, successMessage = context.getString(R.string.backup_export_ok)) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = context.getString(R.string.backup_export_error, error.message ?: "")) }
                }
        }
    }

    fun restoreFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = context.getString(R.string.backup_open_error)) }
                return@launch
            }

            backupUseCase.restore(inputStream)
                .onSuccess {
                    rescheduleRestoredAlerts()
                    _uiState.update { it.copy(isLoading = false, successMessage = context.getString(R.string.backup_restore_ok)) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = context.getString(R.string.backup_restore_error, error.message ?: "")) }
                }
        }
    }

    private suspend fun rescheduleRestoredAlerts() {
        val vehicles = vehicleRepository.observeAllVehicles().first()
        vehicles.forEach { vehicle ->
            val plans = maintenanceRepository.observePlans(vehicle.id.toString()).first()
            plans.forEach { plan ->
                if (vehicle.isArchived) {
                    alertScheduler.cancelForActivity(plan.id)
                } else {
                    alertScheduler.scheduleForActivity(plan, vehicleLabel = vehicle.plate)
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}