package com.micarro.feature.premium.domain

import java.text.NumberFormat
import java.util.Locale

enum class PremiumTier {
    FREE,
    TRIAL,
    PREMIUM
}

data class PremiumRecord(
    val purchasedAt: Long? = null,
    val trialStartedAt: Long? = null,
    val trialEndsAt: Long? = null
)

data class PremiumStatus(
    val tier: PremiumTier,
    val hasAccess: Boolean,
    val canStartTrial: Boolean,
    val trialDaysLeft: Int
)

object PremiumRules {
    const val PRICE_COP = 20_000
    const val TRIAL_DAYS = 30L
    private const val DAY_MILLIS = 24L * 60 * 60 * 1000

    fun status(record: PremiumRecord, now: Long): PremiumStatus {
        if (record.purchasedAt != null) {
            return PremiumStatus(
                tier = PremiumTier.PREMIUM,
                hasAccess = true,
                canStartTrial = false,
                trialDaysLeft = 0
            )
        }
        val endsAt = record.trialEndsAt
        if (record.trialStartedAt != null && endsAt != null && now < endsAt) {
            return PremiumStatus(
                tier = PremiumTier.TRIAL,
                hasAccess = true,
                canStartTrial = false,
                trialDaysLeft = daysLeft(endsAt, now)
            )
        }
        return PremiumStatus(
            tier = PremiumTier.FREE,
            hasAccess = false,
            canStartTrial = record.trialStartedAt == null,
            trialDaysLeft = 0
        )
    }

    fun startTrial(record: PremiumRecord, now: Long): PremiumRecord {
        if (!status(record, now).canStartTrial) return record
        return record.copy(
            trialStartedAt = now,
            trialEndsAt = now + TRIAL_DAYS * DAY_MILLIS
        )
    }

    fun purchase(record: PremiumRecord, now: Long): PremiumRecord {
        if (record.purchasedAt != null) return record
        return record.copy(purchasedAt = now)
    }

    fun formatPrice(amount: Int = PRICE_COP): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"))
        format.maximumFractionDigits = 0
        format.minimumFractionDigits = 0
        return format.format(amount)
    }

    private fun daysLeft(endsAt: Long, now: Long): Int {
        val remaining = endsAt - now
        if (remaining <= 0L) return 0
        return ((remaining + DAY_MILLIS - 1) / DAY_MILLIS).toInt()
    }
}
