package com.micarro.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.model.DashboardSummary
import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.VehicleRepository
import com.micarro.domain.usecase.GetDashboardSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _selectedVehicleId = MutableStateFlow<String?>(null)
    val selectedVehicleId: StateFlow<String?> = _selectedVehicleId.asStateFlow()

    // El stream de datos del resumen responde reactivamente a cualquier cambio en _selectedVehicleId
    val summaryState: StateFlow<DashboardSummary?> = getDashboardSummaryUseCase(_selectedVehicleId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onVehicleSelected(vehicleId: String) {
        _selectedVehicleId.value = vehicleId
    }
}