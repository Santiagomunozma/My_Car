package com.micarro.feature.maintenance.domain

import com.micarro.domain.model.MaintenancePlan
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import com.micarro.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.abs

enum class ServiceError {
    FUTURE_DATE,
    NEGATIVE_MILEAGE,
    NEGATIVE_AMOUNT,
    EMPTY_DESCRIPTION
}

sealed interface RegisterServiceResult {
    data object Success : RegisterServiceResult
    data class Invalid(val errors: Set<ServiceError>) : RegisterServiceResult
    data class RequiresMileageConfirmation(val lastMileage: Int) : RegisterServiceResult
    data class RequiresTotalConfirmation(val calculatedTotal: Double) : RegisterServiceResult
    data class RequiresEarlierDate(val previousDate: Long) : RegisterServiceResult
}

class ObserveMaintenancePlansUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    operator fun invoke(vehicleId: String): Flow<List<MaintenancePlan>> =
        repository.observePlans(vehicleId)
}

class SaveMaintenancePlanUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(plan: MaintenancePlan) =
        repository.savePlan(plan)
}

class UpdateMaintenancePlanUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(plan: MaintenancePlan) =
        repository.updatePlan(plan)
}

class UpdateMaintenancePlanActiveStatusUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(planId: String, isActive: Boolean) =
        repository.updatePlanActiveStatus(planId, isActive)
}

class DeleteMaintenancePlanUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(planId: String): Boolean =
        repository.deletePlanIfWithoutHistory(planId)
}

class RegisterMaintenanceServiceUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(
        service: MaintenanceService,
        parts: List<Part>,
        lastKnownMileage: Int = service.mileage,
        confirmedLowerMileage: Boolean = false,
        confirmedAdjustedTotal: Boolean = false,
        confirmedEarlierDate: Boolean = false,
        now: Long = System.currentTimeMillis()
    ): RegisterServiceResult {
        val prepared = prepare(service, parts, lastKnownMileage, confirmedLowerMileage, confirmedAdjustedTotal, now)
        if (prepared !is Prepared.Ready) return prepared.toResult()
        earlierThanPrevious(repository, service, confirmedEarlierDate)?.let { return it }

        repository.registerService(prepared.service, prepared.parts)
        persistNextRecurrence(prepared.service)
        return RegisterServiceResult.Success
    }

    private suspend fun persistNextRecurrence(service: MaintenanceService) {
        val planId = service.planId ?: return
        val plan = repository.getPlanById(planId) ?: return
        val (nextDate, nextMileage) = MaintenanceRules.calculateNextRecurrence(
            actualDate = service.date,
            actualMileage = service.mileage,
            intervalMonths = plan.intervalMonths,
            intervalMileage = plan.intervalMileage
        )
        repository.updatePlan(
            plan.copy(nextDeadlineDate = nextDate, nextLimitMileage = nextMileage)
        )
    }
}

class UpdateMaintenanceServiceUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(
        service: MaintenanceService,
        parts: List<Part>,
        lastKnownMileage: Int = service.mileage,
        confirmedLowerMileage: Boolean = false,
        confirmedAdjustedTotal: Boolean = false,
        confirmedEarlierDate: Boolean = false,
        now: Long = System.currentTimeMillis()
    ): RegisterServiceResult {
        val prepared = prepare(service, parts, lastKnownMileage, confirmedLowerMileage, confirmedAdjustedTotal, now)
        if (prepared !is Prepared.Ready) return prepared.toResult()
        earlierThanPrevious(repository, service, confirmedEarlierDate)?.let { return it }
        repository.updateService(prepared.service, prepared.parts)
        val planId = prepared.service.planId
        if (planId != null) {
            val plan = repository.getPlanById(planId)
            if (plan != null) {
                val (nextDate, nextMileage) = MaintenanceRules.calculateNextRecurrence(
                    prepared.service.date,
                    prepared.service.mileage,
                    plan.intervalMonths,
                    plan.intervalMileage
                )
                repository.updatePlan(plan.copy(nextDeadlineDate = nextDate, nextLimitMileage = nextMileage))
            }
        }
        return RegisterServiceResult.Success
    }
}

class DeleteMaintenanceServiceUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(serviceId: String): MaintenancePlan? {
        val removed = repository.getServiceById(serviceId)
        repository.deleteService(serviceId)
        val planId = removed?.planId ?: return null
        val plan = repository.getPlanById(planId) ?: return null
        val remaining = repository.observeServices(removed.vehicleId).first()
            .filter { it.planId == planId }
        val updated = if (remaining.isEmpty()) {
            if (plan.intervalMonths > 0 || plan.intervalMileage > 0) {
                plan.copy(nextDeadlineDate = null, nextLimitMileage = null)
            } else {
                plan
            }
        } else {
            val last = remaining.maxBy { it.date }
            val (nextDate, nextMileage) = MaintenanceRules.calculateNextRecurrence(
                last.date,
                last.mileage,
                plan.intervalMonths,
                plan.intervalMileage
            )
            plan.copy(nextDeadlineDate = nextDate, nextLimitMileage = nextMileage)
        }
        if (updated != plan) repository.updatePlan(updated)
        return updated
    }
}

class MaintenanceUseCases @Inject constructor(
    val observePlans: ObserveMaintenancePlansUseCase,
    val savePlan: SaveMaintenancePlanUseCase,
    val updatePlan: UpdateMaintenancePlanUseCase,
    val updatePlanActiveStatus: UpdateMaintenancePlanActiveStatusUseCase,
    val deletePlan: DeleteMaintenancePlanUseCase,
    val registerService: RegisterMaintenanceServiceUseCase,
    val updateService: UpdateMaintenanceServiceUseCase,
    val deleteService: DeleteMaintenanceServiceUseCase,
    val observeServices: ObserveMaintenanceServicesUseCase
)

class ObserveMaintenanceServicesUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    operator fun invoke(vehicleId: String): Flow<List<MaintenanceService>> =
        repository.observeServices(vehicleId)
}

private sealed interface Prepared {
    data class Ready(val service: MaintenanceService, val parts: List<Part>) : Prepared
    data class Invalid(val errors: Set<ServiceError>) : Prepared
    data class NeedsMileage(val lastMileage: Int) : Prepared
    data class NeedsTotal(val calculatedTotal: Double) : Prepared
}

private fun Prepared.toResult(): RegisterServiceResult = when (this) {
    is Prepared.Ready -> RegisterServiceResult.Success
    is Prepared.Invalid -> RegisterServiceResult.Invalid(errors)
    is Prepared.NeedsMileage -> RegisterServiceResult.RequiresMileageConfirmation(lastMileage)
    is Prepared.NeedsTotal -> RegisterServiceResult.RequiresTotalConfirmation(calculatedTotal)
}

private suspend fun earlierThanPrevious(
    repository: MaintenanceRepository,
    service: MaintenanceService,
    confirmed: Boolean
): RegisterServiceResult? {
    if (confirmed) return null
    val planId = service.planId ?: return null
    val previous = repository.observeServices(service.vehicleId).first()
        .filter { it.planId == planId && it.id != service.id }
        .maxByOrNull { it.date }
        ?: return null
    return if (service.date < previous.date) {
        RegisterServiceResult.RequiresEarlierDate(previous.date)
    } else {
        null
    }
}

private fun prepare(
    service: MaintenanceService,
    parts: List<Part>,
    lastKnownMileage: Int,
    confirmedLowerMileage: Boolean,
    confirmedAdjustedTotal: Boolean,
    now: Long
): Prepared {
    val errors = mutableSetOf<ServiceError>()
    if (service.title.isBlank()) errors += ServiceError.EMPTY_DESCRIPTION
    if (!MaintenanceRules.isDateValid(service.date, now)) errors += ServiceError.FUTURE_DATE
    if (service.mileage < 0) errors += ServiceError.NEGATIVE_MILEAGE

    val partsCost = parts.sumOf { it.cost * it.quantity }
    val amountsInvalid = !MaintenanceRules.isAmountValid(service.laborCost) ||
        !MaintenanceRules.isAmountValid(service.otherCosts) ||
        !MaintenanceRules.isAmountValid(service.totalCost) ||
        parts.any { it.quantity < 0 || !MaintenanceRules.isAmountValid(it.cost) }
    if (amountsInvalid) errors += ServiceError.NEGATIVE_AMOUNT
    if (errors.isNotEmpty()) return Prepared.Invalid(errors)

    if (!MaintenanceRules.isMileageValid(service.mileage, lastKnownMileage) && !confirmedLowerMileage) {
        return Prepared.NeedsMileage(lastKnownMileage)
    }

    val calculated = MaintenanceRules.calculateTotalCost(service.laborCost, partsCost, service.otherCosts)
    if (abs(service.totalCost - calculated) > 0.009 && !confirmedAdjustedTotal) {
        return Prepared.NeedsTotal(calculated)
    }

    val storedParts = parts.map { it.copy(serviceId = service.id) }
    return Prepared.Ready(service, storedParts)
}
