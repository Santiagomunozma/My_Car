package com.micarro.feature.maintenance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micarro.domain.usecase.ExpensesSummary
import com.micarro.domain.usecase.GetExpensesSummaryUseCase
import com.micarro.domain.usecase.TimePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

data class ExpensesUiState(
    val selectedPeriod: TimePeriod = TimePeriod.THIS_MONTH,
    val summary: ExpensesSummary? = null
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val getExpensesSummaryUseCase: GetExpensesSummaryUseCase
) : ViewModel() {

    private val _selectedVehicleId = MutableStateFlow<String?>(null)
    private val _selectedPeriod = MutableStateFlow(TimePeriod.THIS_MONTH)

    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ExpensesUiState> = combine(
        _selectedVehicleId,
        _selectedPeriod
    ) { vehicleId, period ->
        Pair(vehicleId, period)
    }.flatMapLatest { (vehicleId, period) ->
        getExpensesSummaryUseCase(vehicleId ?: "", period).map { summary ->
            ExpensesUiState(selectedPeriod = period, summary = summary)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpensesUiState()
    )

    fun setVehicleId(vehicleId: String?) {
        _selectedVehicleId.value = vehicleId
    }

    fun setPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }
}