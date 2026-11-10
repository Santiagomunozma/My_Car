package com.micarro.feature.premium.data

import android.content.Context
import androidx.core.content.edit
import com.micarro.feature.premium.domain.PremiumRecord
import com.micarro.feature.premium.domain.PremiumRules
import com.micarro.feature.premium.domain.PremiumStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _status = MutableStateFlow(statusAt())
    val status: StateFlow<PremiumStatus> = _status.asStateFlow()

    fun refresh(now: Long = System.currentTimeMillis()) {
        _status.value = PremiumRules.status(read(), now)
    }

    fun startTrial(now: Long = System.currentTimeMillis()): PremiumStatus {
        val next = PremiumRules.startTrial(read(), now)
        write(next)
        return publish(next, now)
    }

    fun purchase(now: Long = System.currentTimeMillis()): PremiumStatus {
        val next = PremiumRules.purchase(read(), now)
        write(next)
        return publish(next, now)
    }

    private fun publish(record: PremiumRecord, now: Long): PremiumStatus {
        val status = PremiumRules.status(record, now)
        _status.value = status
        return status
    }

    private fun statusAt(now: Long = System.currentTimeMillis()) =
        PremiumRules.status(read(), now)

    private fun read(): PremiumRecord = PremiumRecord(
        purchasedAt = prefs.getLong(KEY_PURCHASED, 0L).takeIf { it > 0L },
        trialStartedAt = prefs.getLong(KEY_TRIAL_STARTED, 0L).takeIf { it > 0L },
        trialEndsAt = prefs.getLong(KEY_TRIAL_ENDS, 0L).takeIf { it > 0L }
    )

    private fun write(record: PremiumRecord) {
        prefs.edit {
            putLong(KEY_PURCHASED, record.purchasedAt ?: 0L)
            putLong(KEY_TRIAL_STARTED, record.trialStartedAt ?: 0L)
            putLong(KEY_TRIAL_ENDS, record.trialEndsAt ?: 0L)
        }
    }

    private companion object {
        const val PREFS = "premium_prefs"
        const val KEY_PURCHASED = "purchased_at"
        const val KEY_TRIAL_STARTED = "trial_started_at"
        const val KEY_TRIAL_ENDS = "trial_ends_at"
    }
}
