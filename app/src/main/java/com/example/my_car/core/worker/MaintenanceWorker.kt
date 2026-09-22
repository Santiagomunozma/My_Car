package com.example.my_car.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.my_car.core.notification.NotificationHelper
import com.example.my_car.domain.repository.MaintenanceRepository
import com.example.my_car.domain.repository.VehicleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class MaintenanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val vehicles = vehicleRepository.observeVehicles().firstOrNull() ?: return Result.success()

        for (vehicle in vehicles) {
            val plans = maintenanceRepository.observePlans(vehicle.plate).firstOrNull() ?: continue

            for (plan in plans) {
                if (plan.intervalMileage <= 0) continue

                // Cálculo del kilometraje restante para el próximo ciclo
                val remainingKm = plan.intervalMileage - (vehicle.currentMileage % plan.intervalMileage)

                // Si faltan 500 km o menos (y aún no se ha pasado)
                if (remainingKm in 1..500) {
                    NotificationHelper.sendNotification(
                        context = applicationContext,
                        title = "Próximo Mantenimiento (${vehicle.plate})",
                        message = "Faltan $remainingKm km para: ${plan.title}"
                    )
                    break // Evitamos saturar con múltiples notificaciones en una sola ejecución
                }
            }
        }

        return Result.success()
    }
}