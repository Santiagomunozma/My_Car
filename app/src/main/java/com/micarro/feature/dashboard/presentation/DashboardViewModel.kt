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
        val nextMaintenance = summary?.upcomingMaintenances?.firstOrNull()
        val maintenanceAlerts = summary?.activeAlertsCount ?: 0

        // Generar detalles del próximo mantenimiento
        val nextMaintenanceDetails = nextMaintenance?.let { plan ->
            val details = mutableListOf<String>()
            details.add("Categoría: ${plan.category}")
            plan.intervalMileage.takeIf { it > 0 }?.let { details.add("Intervalo: $it km") }
            plan.intervalMonths.takeIf { it > 0 }?.let { details.add("Cada: $it meses") }
            plan.nextDeadlineDate?.let { details.add("Fecha límite: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))}") }
            if (details.isEmpty()) "No hay información adicional disponible" else details.joinToString("\n")
        } ?: "No hay mantenimiento programado"

        // Generar detalles de kilometraje
        val mileageDetails = vehicle?.let { v ->
            val details = mutableListOf<String>()
            details.add("Placa: ${v.plate}")
            details.add("Marca: ${v.brand}")
            details.add("Modelo: ${v.line.ifEmpty { v.model }}")
            details.add("Kilometraje actual: ${String.format(java.util.Locale.US, "%,d", v.currentMileage)} km")
            details.joinToString("\n")
        } ?: "No hay vehículo seleccionado"

        // Generar detalles de gastos
        val expensesDetails = if (vehicle != null) {
            val details = mutableListOf<String>()
            details.add("Placa: ${vehicle.plate}")
            details.add("Gastos del mes actual: ${String.format(java.util.Locale.US, "%,.2f", summary?.totalRecentExpenses ?: 0.0)}")
            details.add("Total de servicios recientes: ${summary?.recentServices?.size ?: 0}")
            if (details.isEmpty()) "No hay información de gastos disponible" else details.joinToString("\n")
        } else {
            "No hay vehículo seleccionado"
        }

        DashboardUiState(
            isLoading = false,
            selectedVehicle = vehicle,
            allVehicles = vehicles,
            totalActiveAlerts = docAlerts + maintenanceAlerts,
            lastMileage = vehicle?.currentMileage ?: 0L,
            mileageDetails = mileageDetails,
            nextMaintenanceTitle = nextMaintenance?.title ?: "Sin pendientes",
            nextMaintenanceDetails = nextMaintenanceDetails,
            currentMonthExpenses = summary?.totalRecentExpenses ?: 0.0,
            expensesDetails = expensesDetails,
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
