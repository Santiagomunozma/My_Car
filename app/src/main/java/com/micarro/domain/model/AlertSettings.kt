package com.micarro.domain.model

data class AlertSettings(
    val globalAlertsEnabled: Boolean = true,
    val anticipationDays: Int = 30,
    val maintenanceAlertsEnabled: Boolean = true,
    val maintenanceMarginDays: Int = 15,
    val maintenanceMarginKm: Int = 500
) {
    init {
        require(anticipationDays in 1..180) { "anticipationDays debe estar entre 1 y 180" }
        require(maintenanceMarginDays in 0..365) { "maintenanceMarginDays fuera de rango" }
        require(maintenanceMarginKm in 0..1_000_000) { "maintenanceMarginKm fuera de rango" }
    }

    companion object {
        const val MIN_ANTICIPATION_DAYS = 1
        const val MAX_ANTICIPATION_DAYS = 180
        const val DEFAULT_ANTICIPATION_DAYS = 30
    }
}
