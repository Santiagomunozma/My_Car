package com.micarro.core.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.micarro.MainActivity // Asegúrate de que esta ruta sea la correcta hacia tu MainActivity

class MaintenanceAlertWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Alerta de Mantenimiento"
        val message = inputData.getString(KEY_MESSAGE) ?: "Tienes una revisión pendiente para tu vehículo."
        val vehicle = inputData.getString(KEY_VEHICLE).orEmpty()
        val activity = inputData.getString(KEY_ACTIVITY).orEmpty()
        val cause = inputData.getString(KEY_CAUSE).orEmpty()

        showNotification(title, message, vehicle, activity, cause)
        return Result.success()
    }

    private fun showNotification(
        title: String,
        message: String,
        vehicle: String,
        activity: String,
        cause: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "maintenance_alerts_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Mantenimiento",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 1. Creamos el Intent para abrir el MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(KEY_VEHICLE, vehicle)
            putExtra(KEY_ACTIVITY, activity)
            putExtra(KEY_CAUSE, cause)
        }

        // 2. Envolvemos el Intent en un PendingIntent inmutable (por seguridad y compatibilidad)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Le agregamos el setContentIntent a la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val KEY_TITLE = "key_title"
        const val KEY_MESSAGE = "key_message"
        const val KEY_VEHICLE = "alert_vehicle"
        const val KEY_ACTIVITY = "alert_activity"
        const val KEY_CAUSE = "alert_cause"
    }
}