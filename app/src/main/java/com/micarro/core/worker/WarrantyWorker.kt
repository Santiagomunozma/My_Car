package com.micarro.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.micarro.core.notification.NotificationHelper
import com.micarro.domain.repository.PartRepository
import com.micarro.domain.repository.VehicleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@HiltWorker
class WarrantyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val partRepository: PartRepository,
    private val vehicleRepository: VehicleRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val vehicles = vehicleRepository.observeAllVehicles().firstOrNull() ?: return Result.success()

        val now = Instant.now()

        for (vehicle in vehicles) {
            val parts = partRepository.observeInstalledParts(vehicle.id.toString()).firstOrNull() ?: continue

            for (part in parts) {
                val installationMillis = part.installationDate ?: continue
                val warrantyMonths = part.warranty?.toLongOrNull() ?: continue

                val installationDate = Instant.ofEpochMilli(installationMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                
                val expirationDate = installationDate.plusMonths(warrantyMonths)
                val today = now.atZone(ZoneId.systemDefault()).toLocalDate()

                val daysUntilExpiration = ChronoUnit.DAYS.between(today, expirationDate)

                // Si falta 1 semana o menos (y no ha pasado un margen ridículo de tiempo), notificamos
                if (daysUntilExpiration in 0..7) {
                    val message = if (daysUntilExpiration == 0L) {
                        "La garantía de ${part.name} vence hoy."
                    } else {
                        "La garantía de ${part.name} vence en $daysUntilExpiration días."
                    }

                    NotificationHelper.sendNotification(
                        context = applicationContext,
                        title = "Garantía próxima a vencer (${vehicle.plate})",
                        message = message,
                        notificationId = part.id.hashCode()
                    )
                }
            }
        }

        return Result.success()
    }
}
