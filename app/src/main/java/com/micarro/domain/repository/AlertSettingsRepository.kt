package com.micarro.domain.repository

import com.micarro.domain.model.AlertSettings
import kotlinx.coroutines.flow.Flow

interface AlertSettingsRepository {
    fun observeSettings(): Flow<AlertSettings>
    fun current(): AlertSettings
    suspend fun setGlobalAlertsEnabled(enabled: Boolean)
    suspend fun setAnticipationDays(days: Int)
    suspend fun setMaintenanceAlertsEnabled(enabled: Boolean)
    suspend fun setMaintenanceMargins(days: Int, km: Int)
}
