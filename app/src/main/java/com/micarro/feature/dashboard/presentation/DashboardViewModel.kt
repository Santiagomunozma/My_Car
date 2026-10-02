package com.micarro.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.DashboardSummary
import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.VehicleRepository
import com.micarro.domain.usecase.GetDashboardSummaryUseCase
import com.micarro.feature.documents.domain.ObserveDocumentAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    vehicleRepository: VehicleRepository,
    getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    observeDocumentAlerts: ObserveDocumentAlertsUseCase
) : ViewModel() {

    private val _selectedVehicleId = MutableStateFlow<String?>(null)
    val selectedVehicleId: StateFlow<String?> = _selectedVehicleId.asStateFlow()

    val allVehicles: StateFlow<List<Vehicle>> = vehicleRepository.observeVehicles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summaryState: StateFlow<DashboardSummary?> = getDashboardSummaryUseCase(_selectedVehicleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val documentAlertCount: StateFlow<Int> = observeDocumentAlerts()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val uiState: StateFlow<DashboardUiState> = combine(
        allVehicles,
        summaryState,
        documentAlertCount
    ) { vehicles, summary, docAlerts ->
        val vehicle = summary?.selectedVehicle
        val nextMaintenance = summary?.upcomingMaintenances?.firstOrNull()?.title
        val maintenanceAlerts = summary?.activeAlertsCount ?: 0

        DashboardUiState(
            isLoading = false,
            selectedVehicle = vehicle,
            allVehicles = vehicles,
            totalActiveAlerts = docAlerts + maintenanceAlerts,
            lastMileage = vehicle?.currentMileage ?: 0L,
            nextMaintenanceTitle = nextMaintenance,
            currentMonthExpenses = summary?.totalRecentExpenses ?: 0.0,
            upcomingMaintenances = summary?.upcomingMaintenances.orEmpty(),
            recentServices = summary?.recentServices.orEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun onVehicleSelected(vehicleId: String) {
        _selectedVehicleId.value = vehicleId
    }
}
