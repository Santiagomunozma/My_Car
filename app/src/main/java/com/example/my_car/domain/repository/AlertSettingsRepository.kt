package com.example.my_car.domain.repository

import com.example.my_car.domain.model.AlertSettings
import kotlinx.coroutines.flow.Flow

interface AlertSettingsRepository {
    fun observeSettings(): Flow<AlertSettings>
    suspend fun setGlobalAlertsEnabled(enabled: Boolean)
    suspend fun setAnticipationDays(days: Int)
}
