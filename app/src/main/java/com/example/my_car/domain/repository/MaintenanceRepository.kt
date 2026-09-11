package com.example.my_car.domain.repository

import com.example.my_car.domain.model.HistoryFilter
import com.example.my_car.domain.model.MaintenanceHistoryItem
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    fun observeHistory(vehicleId: String?, filter: HistoryFilter): Flow<List<MaintenanceHistoryItem>>
}