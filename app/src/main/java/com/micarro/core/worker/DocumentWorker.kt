package com.micarro.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.micarro.R
import com.micarro.core.notification.NotificationHelper
import com.micarro.domain.model.DocumentStatus
import com.micarro.domain.model.DocumentType
import com.micarro.domain.repository.AlertSettingsRepository
import com.micarro.domain.repository.DocumentRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.feature.documents.domain.ObserveDocumentAlertsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

@HiltWorker
class DocumentWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val vehicleRepository: VehicleRepository,
    private val documentRepository: DocumentRepository,
    private val alertSettingsRepository: AlertSettingsRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val settings = alertSettingsRepository.observeSettings().firstOrNull() ?: return Result.success()
        
        if (!settings.globalAlertsEnabled) {
            return Result.success()
        }

        val vehicles = vehicleRepository.observeAllVehicles().firstOrNull() ?: return Result.success()
        val documents = documentRepository.observeAllDocuments().firstOrNull() ?: return Result.success()

        val alerts = ObserveDocumentAlertsUseCase.computeAlerts(
            documents = documents,
            vehicles = vehicles,
            settings = settings,
            today = LocalDate.now()
        )

        for (alert in alerts) {
            val isExpired = alert.status == DocumentStatus.EXPIRED
            val documentName = documentDisplayName(alert.documentType, alert.documentName, applicationContext)
            
            val cause = when {
                alert.daysUntilExpiration == 0L -> applicationContext.getString(R.string.alert_expires_today)
                alert.daysUntilExpiration < 0 -> applicationContext.resources.getQuantityString(
                    R.plurals.alert_days_expired,
                    (-alert.daysUntilExpiration).toInt(),
                    (-alert.daysUntilExpiration).toInt()
                )
                else -> applicationContext.resources.getQuantityString(
                    R.plurals.alert_days_remaining,
                    alert.daysUntilExpiration.toInt(),
                    alert.daysUntilExpiration.toInt()
                )
            }

            val title = if (isExpired) {
                "Documento Vencido: ${alert.vehiclePlate}"
            } else {
                "Vencimiento Próximo: ${alert.vehiclePlate}"
            }

            val message = "$documentName - $cause"

            NotificationHelper.sendNotification(
                context = applicationContext,
                title = title,
                message = message,
                notificationId = alert.documentId.toInt()
            )
        }

        return Result.success()
    }
    
    private fun documentDisplayName(type: DocumentType, customName: String, context: Context): String = when (type) {
        DocumentType.SOAT -> context.getString(R.string.document_type_soat)
        DocumentType.TECHNICAL_INSPECTION -> context.getString(R.string.document_type_technical)
        DocumentType.INSURANCE -> context.getString(R.string.document_type_insurance)
        DocumentType.OTHER -> customName.ifBlank { context.getString(R.string.document_type_other) }
    }
}
