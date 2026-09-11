package com.example.my_car.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_car.domain.model.HistoryFilter
import com.example.my_car.domain.repository.MaintenanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: MaintenanceRepository
) : ViewModel() {

    val filterState = MutableStateFlow(HistoryFilter())

    val uiState: StateFlow<HistoryUiState> = filterState
        .flatMapLatest { filter ->
            repository.observeHistory(vehicleId = null, filter = filter)
                .map { items -> HistoryUiState(isLoading = false, items = items, filter = filter) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState(isLoading = true)
        )

    fun onQueryChanged(newQuery: String) {
        filterState.value = filterState.value.copy(query = newQuery)
    }
}