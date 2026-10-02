package com.micarro.core.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AlertScheduler(
    private val context: Context?,
    @Suppress("UNUSED_PARAMETER") isReal: Boolean
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(context, true)

    constructor() : this(null, false)

    open fun scheduleAlert(
        uniqueWorkName: String,
        delayInMinutes: Long,
        title: String,
        message: String,
        vehicleLabel: String = "",
        activityTitle: String = "",
        cause: String = ""
    ) {
        val ctx = context ?: return
        val inputData = workDataOf(
            MaintenanceAlertWorker.KEY_TITLE to title,
            MaintenanceAlertWorker.KEY_MESSAGE to message,
            MaintenanceAlertWorker.KEY_VEHICLE to vehicleLabel,
            MaintenanceAlertWorker.KEY_ACTIVITY to activityTitle,
            MaintenanceAlertWorker.KEY_CAUSE to cause
        )

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MaintenanceAlertWorker>()
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .setInputData(inputData)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(ctx).enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    open fun cancelAlert(uniqueWorkName: String) {
        val ctx = context ?: return
        WorkManager.getInstance(ctx).cancelUniqueWork(uniqueWorkName)
    }

    companion object {
        fun workName(activityId: String) = "activity_$activityId"
    }
}
