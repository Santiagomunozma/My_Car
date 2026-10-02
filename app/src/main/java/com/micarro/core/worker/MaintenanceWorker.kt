package com.micarro.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.feature.alerts.domain.AlertScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

/**
 * El aviso real es el trabajo único activity_{id}, con retraso hasta el margen de anticipación.
 * Este periódico solo apaga alertas de vehículos archivados (RN-07). No vuelve a notificar
 * por un módulo fijo de 500 km.
 */
@HiltWorker
class MaintenanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val alertScheduler: AlertScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val vehicles = vehicleRepository.observeAllVehicles().firstOrNull().orEmpty()
        vehicles.filter { it.isArchived }.forEach { vehicle ->
            val plans = maintenanceRepository.observePlans(vehicle.id.toString()).firstOrNull().orEmpty()
            plans.forEach { alertScheduler.cancelForActivity(it.id) }
        }
        return Result.success()
    }
}
