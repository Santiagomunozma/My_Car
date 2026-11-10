package com.micarro.feature.documents.domain

enum class DocumentReminder {
    MONTH,
    FORTNIGHT,
    EXPIRED
}

data class ReminderDecision(
    val reminder: DocumentReminder?,
    val remindedMonth: Boolean,
    val remindedFortnight: Boolean,
    val remindedExpired: Boolean
)

object DocumentReminderRules {
    const val MONTH_DAYS = 30
    const val FORTNIGHT_DAYS = 14

    fun decide(
        daysUntil: Long,
        remindedMonth: Boolean,
        remindedFortnight: Boolean,
        remindedExpired: Boolean
    ): ReminderDecision = when {
        daysUntil <= 0 && !remindedExpired -> ReminderDecision(
            reminder = DocumentReminder.EXPIRED,
            remindedMonth = remindedMonth,
            remindedFortnight = remindedFortnight,
            remindedExpired = true
        )
        daysUntil in 1..FORTNIGHT_DAYS && !remindedFortnight -> ReminderDecision(
            reminder = DocumentReminder.FORTNIGHT,
            remindedMonth = true,
            remindedFortnight = true,
            remindedExpired = remindedExpired
        )
        daysUntil in (FORTNIGHT_DAYS + 1)..MONTH_DAYS && !remindedMonth -> ReminderDecision(
            reminder = DocumentReminder.MONTH,
            remindedMonth = true,
            remindedFortnight = remindedFortnight,
            remindedExpired = remindedExpired
        )
        else -> ReminderDecision(
            reminder = null,
            remindedMonth = remindedMonth,
            remindedFortnight = remindedFortnight,
            remindedExpired = remindedExpired
        )
    }
}
