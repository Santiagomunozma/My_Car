package com.micarro.domain.model

data class AlertSettings(
    val globalAlertsEnabled: Boolean = true,
    val anticipationDays: Int = 30
) {
    init {
        require(anticipationDays in 1..180) { "anticipationDays debe estar entre 1 y 180" }
    }

    companion object {
        const val MIN_ANTICIPATION_DAYS = 1
        const val MAX_ANTICIPATION_DAYS = 180
        const val DEFAULT_ANTICIPATION_DAYS = 30
    }
}
