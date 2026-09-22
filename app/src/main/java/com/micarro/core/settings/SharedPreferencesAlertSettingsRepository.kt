package com.micarro.core.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.micarro.domain.model.AlertSettings
import com.micarro.domain.repository.AlertSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesAlertSettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) : AlertSettingsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun observeSettings(): Flow<AlertSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(read())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(read())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun setGlobalAlertsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_GLOBAL_ENABLED, enabled) }
    }

    override suspend fun setAnticipationDays(days: Int) {
        val coerced = days.coerceIn(
            AlertSettings.MIN_ANTICIPATION_DAYS,
            AlertSettings.MAX_ANTICIPATION_DAYS
        )
        prefs.edit { putInt(KEY_ANTICIPATION_DAYS, coerced) }
    }

    private fun read() = AlertSettings(
        globalAlertsEnabled = prefs.getBoolean(KEY_GLOBAL_ENABLED, true),
        anticipationDays = prefs.getInt(KEY_ANTICIPATION_DAYS, AlertSettings.DEFAULT_ANTICIPATION_DAYS)
            .coerceIn(AlertSettings.MIN_ANTICIPATION_DAYS, AlertSettings.MAX_ANTICIPATION_DAYS)
    )

    companion object {
        private const val PREFS_NAME = "alert_settings"
        private const val KEY_GLOBAL_ENABLED = "global_alerts_enabled"
        private const val KEY_ANTICIPATION_DAYS = "anticipation_days"
    }
}
