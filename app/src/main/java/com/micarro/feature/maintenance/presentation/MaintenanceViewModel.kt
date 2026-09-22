package com.micarro.feature.maintenance.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.MileageRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.domain.usecase.ExportHistoryToCsvUseCase
import com.micarro.feature.maintenance.domain.MaintenanceRules
import com.micarro.feature.maintenance.domain.MaintenanceUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val mileageRepository: MileageRepository,
    private val maintenanceUseCases: MaintenanceUseCases,
    private val exportHistoryToCsvUseCase: ExportHistoryToCsvUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    private var plansJob: Job? = null

    init {
        loadVehicles()
    }

    fun exportHistory(context: Context, vehicleId: String) {
        viewModelScope.launch {
            val fileName = "historial_mantenimiento_${System.currentTimeMillis()}.csv"
            val exportDir = java.io.File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
            val file = java.io.File(exportDir, fileName)
            
            runCatching {
                java.io.FileOutputStream(file).use { outputStream ->
                    exportHistoryToCsvUseCase(outputStream, vehicleId).getOrThrow()
                }
            }.onSuccess {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                shareFile(context, uri)
            }.onFailure {
                Toast.makeText(context, "Error al exportar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareFile(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir Historial CSV"))
    }

    private fun loadVehicles() {
        vehicleRepository.observeVehicles()
            .onEach { vehicles ->
                _uiState.update { it.copy(vehicles = vehicles) }
                if (_uiState.value.selectedVehicle == null && vehicles.isNotEmpty()) {
                    selectVehicle(vehicles.first())
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectVehicle(vehicle: Vehicle) {
        _uiState.update { it.copy(selectedVehicle = vehicle) }
        observePlans(vehicle)
    }

    private fun observePlans(vehicle: Vehicle) {
        plansJob?.cancel()
        plansJob = combine(
            maintenanceUseCases.observePlans(vehicle.plate),
            maintenanceUseCases.observeServices(vehicle.plate).onStart { emit(emptyList()) },
            mileageRepository.observeMileage(vehicle.id).onStart { emit(emptyList()) }
        ) { plans, services, mileageRecords ->
            val currentMileage = mileageRecords.firstOrNull()?.reading
                ?: vehicle.currentMileage.toInt()
            val currentTime = System.currentTimeMillis()

            plans.map { plan ->
                val planServices = services.filter { it.planId == plan.id }.sortedByDescending { it.date }
                val lastService = planServices.firstOrNull()

                val nextDeadlineDate = lastService?.let {
                    MaintenanceRules.calculateNextRecurrence(it.date, it.mileage, plan.intervalMonths, plan.intervalMileage).first
                }
                val nextLimitMileage = lastService?.let {
                    MaintenanceRules.calculateNextRecurrence(it.date, it.mileage, plan.intervalMonths, plan.intervalMileage).second
                }

                val status = MaintenanceRules.calculateStatus(
                    deadlineDate = nextDeadlineDate,
                    limitMileage = nextLimitMileage,
                    currentDate = currentTime,
                    currentMileage = currentMileage,
                    marginDays = _uiState.value.alertMarginDays,
                    marginKm = _uiState.value.alertMarginKm
                )

                MaintenancePlanWithStatus(
                    plan = plan,
                    status = status,
                    lastServiceDate = lastService?.date,
                    lastServiceMileage = lastService?.mileage,
                    nextDeadlineDate = nextDeadlineDate,
                    nextLimitMileage = nextLimitMileage
                )
            }
        }.onEach { plansWithStatus ->
            _uiState.update { it.copy(plans = plansWithStatus) }
        }.launchIn(viewModelScope)
    }

    fun savePlan(plan: MaintenancePlan) {
        viewModelScope.launch {
            maintenanceUseCases.savePlan(plan)
        }
    }

    fun registerService(service: MaintenanceService, parts: List<Part>) {
        viewModelScope.launch {
            maintenanceUseCases.registerService(service, parts)
        }
    }

    fun updateAlertSettings(marginDays: Int, marginKm: Int) {
        _uiState.update { it.copy(alertMarginDays = marginDays, alertMarginKm = marginKm) }
    }
}