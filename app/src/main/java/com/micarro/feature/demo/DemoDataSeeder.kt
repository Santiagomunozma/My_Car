package com.micarro.feature.demo

import android.content.Context
import androidx.room.withTransaction
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.micarro.core.database.AppDatabase
import com.micarro.core.worker.DocumentWorker
import com.micarro.core.worker.WarrantyWorker
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.domain.usecase.ClearAllDataUseCase
import com.micarro.feature.alerts.domain.AlertScheduler
import com.micarro.feature.documents.data.DocumentEntity
import com.micarro.feature.documents.domain.DocumentStatusRules
import com.micarro.feature.maintenance.data.MaintenancePlanEntity
import com.micarro.feature.maintenance.data.MaintenanceServiceEntity
import com.micarro.feature.mileage.data.MileageEntity
import com.micarro.feature.mileage.domain.MileageRules
import com.micarro.feature.parts.data.PartEntity
import com.micarro.feature.vehicle.data.VehicleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoDataSeeder @Inject constructor(
    private val database: AppDatabase,
    private val clearAllDataUseCase: ClearAllDataUseCase,
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val alertScheduler: AlertScheduler,
    @ApplicationContext private val context: Context
) {
    private val gate = Mutex()

    suspend fun seedIfEmpty() = gate.withLock {
        if (database.vehicleDao().getAll().isNotEmpty()) return
        insert(DemoScenario.build())
        scheduleAlerts()
    }

    suspend fun replaceWithDemo() = gate.withLock {
        clearAllDataUseCase()
        insert(DemoScenario.build())
        scheduleAlerts()
    }

    private suspend fun insert(snapshot: DemoScenario.Snapshot) {
        database.withTransaction {
            snapshot.vehicles.forEach { vehicle ->
                database.vehicleDao().insert(
                    VehicleEntity(
                        id = vehicle.id,
                        plate = vehicle.plate,
                        type = vehicle.type.name,
                        brand = vehicle.brand,
                        line = vehicle.line,
                        model = vehicle.model,
                        year = vehicle.year,
                        currentMileage = vehicle.currentMileage,
                        color = vehicle.color,
                        vin = vehicle.vin,
                        fuelType = vehicle.fuelType?.name,
                        engineCc = vehicle.engineCc,
                        photoUri = null,
                        isArchived = vehicle.isArchived,
                        isPrimary = vehicle.isPrimary
                    )
                )
            }
            snapshot.readings.forEach { reading ->
                database.mileageDao().insert(
                    MileageEntity(
                        id = reading.id,
                        vehicleId = reading.vehicleId,
                        date = MileageRules.dateToMillis(reading.date),
                        reading = reading.mileage,
                        note = reading.note
                    )
                )
            }
            snapshot.documents.forEach { document ->
                database.documentDao().upsert(
                    DocumentEntity(
                        id = document.id,
                        vehicleId = document.vehicleId,
                        type = document.type.name,
                        name = document.name,
                        expirationDate = DocumentStatusRules.dateToMillis(document.expirationDate),
                        issuer = document.issuer,
                        alertsEnabled = document.alertsEnabled,
                        notes = null,
                        referenceNumber = document.referenceNumber
                    )
                )
            }
            snapshot.plans.forEach { plan ->
                database.maintenancePlanDao().insert(
                    MaintenancePlanEntity(
                        id = plan.id,
                        vehicleId = plan.vehicleId.toString(),
                        title = plan.title,
                        category = plan.category,
                        intervalMileage = plan.intervalMileage,
                        intervalMonths = plan.intervalMonths,
                        isActive = plan.isActive,
                        description = plan.description,
                        nextDeadlineDate = plan.deadline?.let(DocumentStatusRules::dateToMillis),
                        nextLimitMileage = plan.limitMileage,
                        marginDays = plan.marginDays,
                        marginKm = plan.marginKm,
                        alertsEnabled = plan.alertsEnabled
                    )
                )
            }
            snapshot.services.forEach { service ->
                database.maintenanceServiceDao().insert(
                    MaintenanceServiceEntity(
                        id = service.id,
                        vehicleId = service.vehicleId.toString(),
                        planId = service.planId,
                        title = service.title,
                        category = service.category,
                        date = DocumentStatusRules.dateToMillis(service.date),
                        mileage = service.mileage,
                        totalCost = service.totalCost,
                        workshopName = service.workshopName,
                        serviceType = service.serviceType.name,
                        laborCost = service.laborCost,
                        otherCosts = service.otherCosts,
                        description = service.description
                    )
                )
            }
            if (snapshot.parts.isNotEmpty()) {
                database.partDao().insertAll(
                    snapshot.parts.map { part ->
                        PartEntity(
                            id = part.id,
                            serviceId = part.serviceId,
                            name = part.name,
                            quantity = part.quantity,
                            cost = part.cost,
                            brand = part.brand,
                            reference = part.reference,
                            provider = part.provider,
                            installationDate = part.installationDate?.let(DocumentStatusRules::dateToMillis),
                            warranty = part.warranty,
                            notes = part.notes
                        )
                    }
                )
            }
        }
    }

    private suspend fun scheduleAlerts() {
        val vehicles = vehicleRepository.observeAllVehicles().first()
        vehicles.forEach { vehicle ->
            val plans = maintenanceRepository.observePlans(vehicle.id.toString()).first()
            plans.forEach { plan ->
                if (vehicle.isArchived) {
                    alertScheduler.cancelForActivity(plan.id)
                } else {
                    alertScheduler.scheduleForActivity(plan, vehicleLabel = vehicle.plate)
                }
            }
        }
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(
            "demo_document_alerts",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DocumentWorker>().build()
        )
        workManager.enqueueUniqueWork(
            "demo_warranty_alerts",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WarrantyWorker>().build()
        )
    }
}
