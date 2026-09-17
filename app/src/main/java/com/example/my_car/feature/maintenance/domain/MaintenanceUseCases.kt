package com.example.my_car.feature.maintenance.domain

import com.example.my_car.domain.model.MaintenancePlan
import com.example.my_car.domain.model.MaintenanceService
import com.example.my_car.domain.model.Part
import com.example.my_car.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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

class DeleteMaintenancePlanUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(planId: String): Boolean =
        repository.deletePlanIfWithoutHistory(planId)
}

class RegisterMaintenanceServiceUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(service: MaintenanceService, parts: List<Part>) =
        repository.registerService(service, parts)
}

class ObserveMaintenanceServicesUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    operator fun invoke(vehicleId: String): Flow<List<MaintenanceService>> =
        repository.observeServices(vehicleId)
}

class MaintenanceUseCases @Inject constructor(
    val observePlans: ObserveMaintenancePlansUseCase,
    val savePlan: SaveMaintenancePlanUseCase,
    val updatePlan: UpdateMaintenancePlanUseCase,
    val deletePlan: DeleteMaintenancePlanUseCase,
    val registerService: RegisterMaintenanceServiceUseCase,
    val observeServices: ObserveMaintenanceServicesUseCase
)
