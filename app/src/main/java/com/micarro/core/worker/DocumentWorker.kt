package com.micarro.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.micarro.R
import com.micarro.core.notification.NotificationHelper
import com.micarro.domain.model.DocumentType
import com.micarro.domain.model.VehicleDocument
import com.micarro.domain.repository.AlertSettingsRepository
import com.micarro.domain.repository.DocumentRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.feature.documents.domain.DocumentReminder
import com.micarro.feature.documents.domain.DocumentReminderRules
import com.micarro.feature.documents.domain.DocumentStatusRules
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
        if (!settings.globalAlertsEnabled) return Result.success()

        val vehicles = vehicleRepository.observeAllVehicles().firstOrNull() ?: return Result.success()
        val documents = documentRepository.observeAllDocuments().firstOrNull() ?: return Result.success()
        val vehiclesById = vehicles.associateBy { it.id }
        val today = LocalDate.now()

        for (document in documents) {
            if (!document.alertsEnabled) continue
            val vehicle = vehiclesById[document.vehicleId] ?: continue
            if (vehicle.isArchived) continue

            val days = DocumentStatusRules.daysUntil(document.expirationDate, today)
            val decision = DocumentReminderRules.decide(
                daysUntil = days,
                remindedMonth = document.remindedMonth,
                remindedFortnight = document.remindedFortnight,
                remindedExpired = document.remindedExpired
            )
            val reminder = decision.reminder
            if (reminder != null) {
                notify(document, vehicle.plate, days, reminder)
            }
            val flagsChanged = decision.remindedMonth != document.remindedMonth ||
                decision.remindedFortnight != document.remindedFortnight ||
                decision.remindedExpired != document.remindedExpired
            if (flagsChanged) {
                documentRepository.markReminders(
                    id = document.id,
                    month = decision.remindedMonth,
                    fortnight = decision.remindedFortnight,
                    expired = decision.remindedExpired
                )
            }
        }

        return Result.success()
    }

    private fun notify(
        document: VehicleDocument,
        plate: String,
        daysUntil: Long,
        reminder: DocumentReminder
    ) {
        val documentName = documentDisplayName(document.type, document.name, applicationContext)
        val cause = when {
            daysUntil == 0L -> applicationContext.getString(R.string.alert_expires_today)
            daysUntil < 0 -> applicationContext.resources.getQuantityString(
                R.plurals.alert_days_expired,
                (-daysUntil).toInt(),
                (-daysUntil).toInt()
            )
            else -> applicationContext.resources.getQuantityString(
                R.plurals.alert_days_remaining,
                daysUntil.toInt(),
                daysUntil.toInt()
            )
        }
        val title = if (daysUntil < 0) {
            "Documento vencido: $plate"
        } else {
            "Vencimiento próximo: $plate"
        }
        NotificationHelper.sendNotification(
            context = applicationContext,
            title = title,
            message = "$documentName - $cause",
            notificationId = notificationId(document.id, reminder)
        )
    }

    private fun notificationId(documentId: Long, reminder: DocumentReminder): Int {
        val slot = when (reminder) {
            DocumentReminder.MONTH -> 1
            DocumentReminder.FORTNIGHT -> 2
            DocumentReminder.EXPIRED -> 3
        }
        return ((documentId % 200_000_000L) * 10 + slot).toInt()
    }

    private fun documentDisplayName(type: DocumentType, customName: String, context: Context): String = when (type) {
        DocumentType.SOAT -> context.getString(R.string.document_type_soat)
        DocumentType.TECHNICAL_INSPECTION -> context.getString(R.string.document_type_technical)
        DocumentType.INSURANCE -> context.getString(R.string.document_type_insurance)
        DocumentType.TAX -> context.getString(R.string.document_type_tax)
        DocumentType.TRAFFIC_LIGHT -> context.getString(R.string.document_type_traffic_light)
        DocumentType.OTHER -> customName.ifBlank { context.getString(R.string.document_type_other) }
    }
}
