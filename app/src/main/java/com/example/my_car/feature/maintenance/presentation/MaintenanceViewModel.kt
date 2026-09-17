package com.example.my_car.feature.maintenance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_car.domain.model.MaintenancePlan
import com.example.my_car.domain.model.MaintenanceService
import com.example.my_car.domain.model.Part
import com.example.my_car.domain.model.Vehicle
import com.example.my_car.domain.repository.MileageRepository
import com.example.my_car.domain.repository.VehicleRepository
import com.example.my_car.feature.maintenance.domain.MaintenanceRules
import com.example.my_car.feature.maintenance.domain.MaintenanceUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val mileageRepository: MileageRepository,
    private val maintenanceUseCases: MaintenanceUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    init {
        loadVehicles()
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
        observePlans(vehicle.id)
    }

    private fun observePlans(vehicleId: String) {
        combine(
            maintenanceUseCases.observePlans(vehicleId),
            maintenanceUseCases.observeServices(vehicleId),
            mileageRepository.observeMileage(vehicleId)
        ) { plans, services, mileageRecords ->
            val currentMileage = mileageRecords.firstOrNull()?.reading ?: _uiState.value.selectedVehicle?.currentMileage ?: 0
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
        // Re-trigger calculation if needed or wait for next emission
    }
}
