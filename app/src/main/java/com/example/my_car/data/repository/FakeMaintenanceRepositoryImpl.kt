package com.example.my_car.data.repository

import com.example.my_car.domain.model.HistoryFilter
import com.example.my_car.domain.model.MaintenanceHistoryItem
import com.example.my_car.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeMaintenanceRepositoryImpl @Inject constructor() : MaintenanceRepository {
    override fun observeHistory(
        vehicleId: String?,
        filter: HistoryFilter
    ): Flow<List<MaintenanceHistoryItem>> = flowOf(emptyList())
}