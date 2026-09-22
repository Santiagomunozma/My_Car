package com.micarro.feature.backup.data.repository

import androidx.room.withTransaction
import com.micarro.core.database.AppDatabase
import com.micarro.feature.backup.domain.model.BackupData
import com.micarro.feature.backup.domain.model.HistoryBackupDto
import com.micarro.feature.backup.domain.repository.BackupRepository
import com.micarro.feature.maintenance.data.MaintenanceServiceEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val gson: Gson
) : BackupRepository {

    override suspend fun exportBackup(outputStream: OutputStream): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val serviceEntities = database.maintenanceServiceDao()
                    .observeServices(vehicleId = "")
                    .firstOrNull() ?: emptyList()

                val backupData = BackupData(
                    history = serviceEntities.map { entity ->
                        HistoryBackupDto(
                            id = entity.id,
                            vehiclePlate = entity.vehicleId,
                            title = entity.title,
                            category = entity.category,
                            mileage = entity.mileage,
                            totalCost = entity.totalCost,
                            workshopName = entity.workshopName,
                            date = entity.date
                        )
                    }
                )

                val jsonString = gson.toJson(backupData)
                outputStream.use { stream ->
                    stream.write(jsonString.toByteArray(Charsets.UTF_8))
                }
            }
        }

    override suspend fun restoreBackup(inputStream: InputStream): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val jsonString = inputStream.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }

                val backupData = gson.fromJson(jsonString, BackupData::class.java)
                    ?: throw IllegalArgumentException("El archivo de respaldo está vacío o corrupto")

                database.withTransaction {
                    val serviceEntities = backupData.history.map { dto ->
                        MaintenanceServiceEntity(
                            id = dto.id,
                            vehicleId = dto.vehiclePlate,
                            planId = null,
                            title = dto.title,
                            category = dto.category,
                            mileage = dto.mileage,
                            totalCost = dto.totalCost,
                            workshopName = dto.workshopName ?: "", // FIX 1: Conversión segura de String? a String
                            date = dto.date
                        )
                    }

                    // FIX 2: Invocar la función con la anotación @Insert de tu MaintenanceServiceDao
                    serviceEntities.forEach { service ->
                        database.maintenanceServiceDao().insert(service)

                    }
                }
            }
        }
}