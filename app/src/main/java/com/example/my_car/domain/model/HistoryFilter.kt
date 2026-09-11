package com.example.my_car.domain.model

data class HistoryFilter(
    val query: String = "",
    val category: String? = null,
    val minCost: Double? = null,
    val maxCost: Double? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)