package com.micarro.app.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.app.domain.model.Vehicle
import com.micarro.app.feature.vehicle.domain.ObserveVehiclesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val primaryVehicle: Vehicle? = null,
    val vehicleCount: Int = 0,
    val loading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeVehicles: ObserveVehiclesUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = observeVehicles()
        .map { vehicles ->
            DashboardUiState(
                primaryVehicle = vehicles.firstOrNull { it.isPrimary }
                    ?: vehicles.firstOrNull(),
                vehicleCount = vehicles.size,
                loading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
