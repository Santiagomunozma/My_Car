package com.micarro.domain.model

data class HistoryFilter(
    val query: String? = "",
    val category: String? = null,
    val serviceType: String? = null,
    val workshop: String? = null,
    val vehicleId: String? = null,
    val minCost: Double? = null,
    val maxCost: Double? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)