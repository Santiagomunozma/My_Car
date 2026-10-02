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
import com.micarro.domain.repository.AlertSettingsRepository
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.MileageRepository
import com.micarro.domain.repository.PartRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.domain.usecase.ExportHistoryToCsvUseCase
import com.micarro.feature.alerts.domain.AlertScheduler
import com.micarro.feature.maintenance.data.LocalMaintenanceCategoryStore
import com.micarro.feature.maintenance.domain.MaintenanceRules
import com.micarro.feature.maintenance.domain.MaintenanceUseCases
import com.micarro.feature.maintenance.domain.RegisterServiceResult
import com.micarro.feature.maintenance.domain.ServiceEvidenceStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val maintenanceRepository: MaintenanceRepository,
    private val partRepository: PartRepository,
    private val maintenanceUseCases: MaintenanceUseCases,
    private val alertScheduler: AlertScheduler,
    private val alertSettingsRepository: AlertSettingsRepository,
    private val exportHistoryToCsvUseCase: ExportHistoryToCsvUseCase,
    private val evidenceStore: ServiceEvidenceStore,
    private val categoryStore: LocalMaintenanceCategoryStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    private val _serviceEvents = MutableSharedFlow<RegisterServiceResult>(extraBufferCapacity = 1)
    val serviceEvents: SharedFlow<RegisterServiceResult> = _serviceEvents.asSharedFlow()

    private var plansJob: Job? = null
    private var partsJob: Job? = null

    init {
        alertSettingsRepository.observeSettings()
            .onEach { settings ->
                _uiState.update {
                    it.copy(
                        alertMarginDays = settings.maintenanceMarginDays,
                        alertMarginKm = settings.maintenanceMarginKm,
                        maintenanceAlertsEnabled = settings.maintenanceAlertsEnabled
                    )
                }
            }
            .launchIn(viewModelScope)
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
                    "${context.packageName}.provider",
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
                    val primary = vehicles.firstOrNull { it.isPrimary } ?: vehicles.first()
                    selectVehicle(primary)
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
        partsJob?.cancel()

        partsJob = partRepository.observeInstalledParts(vehicle.id.toString())
            .onEach { installedParts ->
                _uiState.update { it.copy(installedParts = installedParts) }
            }
            .launchIn(viewModelScope)

        plansJob = combine(
            maintenanceUseCases.observePlans(vehicle.id.toString()),
            maintenanceUseCases.observeServices(vehicle.id.toString()).onStart { emit(emptyList()) },
            mileageRepository.observeMileage(vehicle.id).onStart { emit(emptyList()) }
        ) { plans, services, mileageRecords ->
            val currentMileage = mileageRecords.firstOrNull()?.reading?.toInt()
                ?: vehicle.currentMileage.toInt()
            val currentTime = System.currentTimeMillis()
            val marginDays = _uiState.value.alertMarginDays
            val marginKm = _uiState.value.alertMarginKm

            plans.map { plan ->
                val lastService = services.filter { it.planId == plan.id }.maxByOrNull { it.date }
                val computed = lastService?.let {
                    MaintenanceRules.calculateNextRecurrence(
                        it.date,
                        it.mileage,
                        plan.intervalMonths,
                        plan.intervalMileage
                    )
                }
                val nextDeadlineDate = plan.nextDeadlineDate ?: computed?.first
                val nextLimitMileage = plan.nextLimitMileage ?: computed?.second
                val status = if (!plan.isActive) {
                    com.micarro.feature.maintenance.domain.MaintenanceStatus.AL_DIA
                } else if (nextDeadlineDate == null && nextLimitMileage == null &&
                    (plan.intervalMonths > 0 || plan.intervalMileage > 0)
                ) {
                    com.micarro.feature.maintenance.domain.MaintenanceStatus.AL_DIA
                } else {
                    MaintenanceRules.calculateStatus(
                        deadlineDate = nextDeadlineDate,
                        limitMileage = nextLimitMileage,
                        currentDate = currentTime,
                        currentMileage = currentMileage,
                        marginDays = marginDays,
                        marginKm = marginKm
                    )
                }

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
            schedule(plan)
        }
    }

    fun updatePlan(plan: MaintenancePlan) {
        viewModelScope.launch {
            maintenanceUseCases.updatePlan(plan)
            schedule(plan)
        }
    }

    fun deletePlan(planId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = maintenanceUseCases.deletePlan(planId)
            if (success) {
                alertScheduler.cancelForActivity(planId)
            }
            onResult(success)
        }
    }

    fun togglePlanActiveStatus(planId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            val next = !currentStatus
            maintenanceUseCases.updatePlanActiveStatus(planId, next)
            val plan = maintenanceRepository.getPlanById(planId) ?: return@launch
            if (next) schedule(plan) else alertScheduler.cancelForActivity(planId)
        }
    }

    fun postponePlan(plan: MaintenancePlan, newDate: Long) {
        val plate = _uiState.value.selectedVehicle?.plate ?: plan.vehicleId
        alertScheduler.postpone(plan.id, newDate, plan.title, plate)
    }

    fun submitService(
        service: MaintenanceService,
        parts: List<Part>,
        confirmedLowerMileage: Boolean,
        confirmedAdjustedTotal: Boolean,
        confirmedEarlierDate: Boolean = false,
        isUpdate: Boolean
    ) {
        viewModelScope.launch {
            val vehicleId = service.vehicleId.toLongOrNull()
            val latest = vehicleId?.let { mileageRepository.getLatestMileage(it)?.reading?.toInt() }
            val vehicleMileage = _uiState.value.vehicles
                .find { it.id.toString() == service.vehicleId }
                ?.currentMileage
                ?.toInt()
            val lastKnown = maxOf(latest ?: 0, vehicleMileage ?: 0)
            val result = if (isUpdate) {
                maintenanceUseCases.updateService(
                    service, parts, lastKnown, confirmedLowerMileage, confirmedAdjustedTotal, confirmedEarlierDate
                )
            } else {
                maintenanceUseCases.registerService(
                    service, parts, lastKnown, confirmedLowerMileage, confirmedAdjustedTotal, confirmedEarlierDate
                )
            }
            if (result is RegisterServiceResult.Success) {
                service.planId?.let { planId ->
                    maintenanceRepository.getPlanById(planId)?.let { schedule(it) }
                }
                val reading = service.mileage.toLong()
                if (vehicleId != null && reading >= (vehicleMileage ?: 0)) {
                    vehicleRepository.updateCurrentMileage(vehicleId, reading)
                }
            }
            _serviceEvents.emit(result)
        }
    }

    suspend fun loadService(serviceId: String): Pair<MaintenanceService, List<Part>>? {
        val service = maintenanceRepository.getServiceById(serviceId) ?: return null
        val parts = partRepository.getPartsForService(serviceId)
        return service to parts
    }

    suspend fun loadPlan(planId: String): MaintenancePlan? = maintenanceRepository.getPlanById(planId)

    fun deleteService(serviceId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val plan = maintenanceUseCases.deleteService(serviceId)
            if (plan != null) schedule(plan)
            onDone()
        }
    }

    suspend fun importEvidence(sourceUri: String): String? = runCatching {
        evidenceStore.import(sourceUri)
    }.getOrNull()

    fun updateAlertSettings(marginDays: Int, marginKm: Int, enabled: Boolean) {
        viewModelScope.launch {
            alertSettingsRepository.setMaintenanceAlertsEnabled(enabled)
            alertSettingsRepository.setMaintenanceMargins(marginDays, marginKm)
            _uiState.value.plans.forEach { item ->
                if (enabled && item.plan.isActive) schedule(item.plan) else alertScheduler.cancelForActivity(item.plan.id)
            }
        }
    }

    fun categoryOptions(): List<String> = categoryStore.all()

    fun rememberCategory(name: String) {
        categoryStore.add(name)
    }

    private fun schedule(plan: MaintenancePlan) {
        val vehicle = _uiState.value.vehicles.find { it.id.toString() == plan.vehicleId }
        if (vehicle?.isArchived == true) {
            alertScheduler.cancelForActivity(plan.id)
            return
        }
        val plate = vehicle?.plate ?: plan.vehicleId
        alertScheduler.scheduleForActivity(plan, vehicleLabel = plate)
    }
}
