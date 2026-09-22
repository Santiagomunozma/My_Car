package com.micarro.domain.usecase

import com.micarro.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

enum class TimePeriod {
    THIS_MONTH,
    LAST_6_MONTHS,
    THIS_YEAR,
    ALL_TIME
}

data class CategoryExpense(
    val category: String,
    val totalAmount: Double,
    val percentage: Float
)

data class ExpensesSummary(
    val totalSpent: Double,
    val breakdown: List<CategoryExpense>
)

class GetExpensesSummaryUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    operator fun invoke(vehicleId: String, period: TimePeriod): Flow<ExpensesSummary> {
        val startTimestamp = calculateStartDate(period)

        return repository.observeExpensesByCategory(vehicleId, startTimestamp).map { list ->
            val totalSpent = list.sumOf { it.totalAmount }
            val breakdown = list.map { item ->
                CategoryExpense(
                    category = item.category,
                    totalAmount = item.totalAmount,
                    percentage = if (totalSpent > 0) ((item.totalAmount / totalSpent) * 100).toFloat() else 0f
                )
            }
            ExpensesSummary(totalSpent = totalSpent, breakdown = breakdown)
        }
    }

    private fun calculateStartDate(period: TimePeriod): Long {
        val calendar = Calendar.getInstance()
        return when (period) {
            TimePeriod.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.timeInMillis
            }
            TimePeriod.LAST_6_MONTHS -> {
                calendar.add(Calendar.MONTH, -6)
                calendar.timeInMillis
            }
            TimePeriod.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.timeInMillis
            }
            TimePeriod.ALL_TIME -> 0L
        }
    }
}