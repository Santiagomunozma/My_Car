package com.micarro.feature.backup.data.repository

import android.content.Context
import android.util.Base64
import androidx.room.withTransaction
import com.google.gson.Gson
import com.micarro.core.database.AppDatabase
import com.micarro.domain.model.EvidenceCodec
import com.micarro.feature.backup.domain.model.BackupData
import com.micarro.feature.backup.domain.model.DocumentBackupDto
import com.micarro.feature.backup.domain.model.FileBackupDto
import com.micarro.feature.backup.domain.model.HistoryBackupDto
import com.micarro.feature.backup.domain.model.MileageBackupDto
import com.micarro.feature.backup.domain.model.PartBackupDto
import com.micarro.feature.backup.domain.model.PlanBackupDto
import com.micarro.feature.backup.domain.model.VehicleBackupDto
import com.micarro.feature.backup.domain.repository.BackupRepository
import com.micarro.feature.documents.data.DocumentEntity
import com.micarro.feature.maintenance.data.MaintenancePlanEntity
import com.micarro.feature.maintenance.data.MaintenanceServiceEntity
import com.micarro.feature.mileage.data.MileageEntity
import com.micarro.feature.parts.data.PartEntity
import com.micarro.feature.vehicle.data.VehicleEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) : BackupRepository {

    override suspend fun exportBackup(outputStream: OutputStream): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val vehicles = database.vehicleDao().getAll()
                val plates = vehicles.associate { it.id to it.plate }
                val backup = BackupData(
                    vehicles = vehicles.map { it.toDto() },
                    mileageRecords = database.mileageDao().getAll().map { reading ->
                        MileageBackupDto(
                            id = reading.id,
                            vehicleId = reading.vehicleId,
                            vehiclePlate = plates[reading.vehicleId].orEmpty(),
                            mileage = reading.reading,
                            date = reading.date,
                            note = reading.note
                        )
                    },
                    documents = database.documentDao().getAll().map { doc ->
                        DocumentBackupDto(
                            id = doc.id,
                            vehicleId = doc.vehicleId,
                            type = doc.type,
                            name = doc.name,
                            expirationDate = doc.expirationDate,
                            issuer = doc.issuer,
                            alertsEnabled = doc.alertsEnabled,
                            notes = doc.notes
                        )
                    },
                    plans = database.maintenancePlanDao().getAll().map { plan ->
                        PlanBackupDto(
                            id = plan.id,
                            vehicleId = plan.vehicleId,
                            vehiclePlate = plates[plan.vehicleId.toLongOrNull()] ?: plan.vehicleId,
                            title = plan.title,
                            category = plan.category,
                            description = plan.description,
                            intervalMileage = plan.intervalMileage,
                            intervalMonths = plan.intervalMonths,
                            isActive = plan.isActive,
                            nextDeadlineDate = plan.nextDeadlineDate,
                            nextLimitMileage = plan.nextLimitMileage
                        )
                    },
                    history = database.maintenanceServiceDao().getAll().map { service ->
                        HistoryBackupDto(
                            id = service.id,
                            vehicleId = service.vehicleId,
                            vehiclePlate = plates[service.vehicleId.toLongOrNull()] ?: service.vehicleId,
                            planId = service.planId,
                            title = service.title,
                            category = service.category,
                            serviceType = service.serviceType,
                            mileage = service.mileage,
                            laborCost = service.laborCost,
                            otherCosts = service.otherCosts,
                            totalCost = service.totalCost,
                            workshopName = service.workshopName,
                            date = service.date,
                            description = service.description,
                            evidenceUri = service.evidenceUri
                        )
                    },
                    parts = database.partDao().getAll().map { part ->
                        PartBackupDto(
                            id = part.id,
                            serviceId = part.serviceId,
                            name = part.name,
                            quantity = part.quantity,
                            cost = part.cost,
                            brand = part.brand,
                            reference = part.reference,
                            provider = part.provider,
                            installationDate = part.installationDate,
                            warranty = part.warranty,
                            notes = part.notes
                        )
                    },
                    files = collectFiles(vehicles.mapNotNull { it.photoUri } + database.maintenanceServiceDao().getAll()
                        .flatMap { EvidenceCodec.decode(it.evidenceUri) })
                )
                outputStream.use { it.write(gson.toJson(backup).toByteArray(Charsets.UTF_8)) }
            }
        }

    override suspend fun restoreBackup(inputStream: InputStream): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val json = inputStream.use { it.bufferedReader(Charsets.UTF_8).readText() }
                val backup = gson.fromJson(json, BackupData::class.java)
                    ?: throw IllegalArgumentException("El archivo de respaldo está vacío o corrupto")

                database.withTransaction {
                    val pathMap = restoreFiles(backup.files.orEmpty())
                    val idMap = mutableMapOf<Long, Long>()
                    backup.vehicles.orEmpty().forEach { dto ->
                        if (dto.plate.isBlank()) return@forEach
                        val existing = database.vehicleDao().getByPlate(dto.plate)
                        val targetId = existing?.id ?: dto.id
                        idMap[dto.id] = targetId
                        val entity = dto.toEntity(targetId).let { vehicle ->
                            vehicle.copy(photoUri = rewritePath(vehicle.photoUri, pathMap))
                        }
                        if (existing == null && database.vehicleDao().getById(targetId) == null) {
                            database.vehicleDao().insert(entity)
                        } else {
                            database.vehicleDao().update(entity)
                        }
                    }

                    backup.mileageRecords.orEmpty().forEach { dto ->
                        val vehicleId = idMap[dto.vehicleId] ?: dto.vehicleId
                        database.mileageDao().upsert(
                            MileageEntity(
                                id = dto.id,
                                vehicleId = vehicleId,
                                date = dto.date,
                                reading = dto.mileage,
                                note = dto.note
                            )
                        )
                    }

                    backup.documents.orEmpty().forEach { dto ->
                        database.documentDao().upsert(
                            DocumentEntity(
                                id = dto.id,
                                vehicleId = idMap[dto.vehicleId] ?: dto.vehicleId,
                                type = dto.type,
                                name = dto.name,
                                expirationDate = dto.expirationDate,
                                issuer = dto.issuer,
                                alertsEnabled = dto.alertsEnabled,
                                notes = dto.notes
                            )
                        )
                    }

                    backup.plans.orEmpty().forEach { dto ->
                        val vehicleId = remapVehicle(dto.vehicleId, idMap)
                        database.maintenancePlanDao().insert(
                            MaintenancePlanEntity(
                                id = dto.id,
                                vehicleId = vehicleId,
                                title = dto.title,
                                category = dto.category.ifBlank { "Otros" },
                                intervalMileage = dto.intervalMileage,
                                intervalMonths = dto.intervalMonths,
                                isActive = dto.isActive,
                                description = dto.description,
                                nextDeadlineDate = dto.nextDeadlineDate,
                                nextLimitMileage = dto.nextLimitMileage
                            )
                        )
                    }

                    backup.history.orEmpty().forEach { dto ->
                        database.maintenanceServiceDao().insert(
                            MaintenanceServiceEntity(
                                id = dto.id,
                                vehicleId = remapVehicle(dto.vehicleId.ifBlank { dto.vehiclePlate }, idMap),
                                planId = dto.planId,
                                title = dto.title,
                                category = dto.category,
                                date = dto.date,
                                mileage = dto.mileage,
                                totalCost = dto.totalCost,
                                workshopName = dto.workshopName.orEmpty(),
                                serviceType = dto.serviceType,
                                laborCost = dto.laborCost,
                                otherCosts = dto.otherCosts,
                                description = dto.description,
                                evidenceUri = rewriteEvidence(dto.evidenceUri, pathMap)
                            )
                        )
                    }

                    val parts = backup.parts.orEmpty()
                    if (parts.isNotEmpty()) {
                        database.partDao().insertAll(parts.map { dto ->
                            PartEntity(
                                id = dto.id,
                                serviceId = dto.serviceId,
                                name = dto.name,
                                quantity = dto.quantity,
                                cost = dto.cost,
                                brand = dto.brand,
                                reference = dto.reference,
                                provider = dto.provider,
                                installationDate = dto.installationDate,
                                warranty = dto.warranty,
                                notes = dto.notes
                            )
                        })
                    }
                }
            }
        }

    private fun remapVehicle(rawId: String, idMap: Map<Long, Long>): String {
        val numeric = rawId.toLongOrNull() ?: return rawId
        return (idMap[numeric] ?: numeric).toString()
    }

    private fun collectFiles(paths: List<String>): List<FileBackupDto> =
        paths.distinct().mapNotNull { path ->
            val file = File(path)
            if (!file.isFile) return@mapNotNull null
            FileBackupDto(
                path = path,
                contentBase64 = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
            )
        }

    private fun restoreFiles(files: List<FileBackupDto>): Map<String, String> {
        val rewritten = mutableMapOf<String, String>()
        files.forEach { item ->
            if (item.path.isBlank() || item.contentBase64.isBlank()) return@forEach
            val folder = if (item.path.contains("service_evidence")) "service_evidence" else "vehicle_photos"
            val dir = File(context.filesDir, folder).apply { mkdirs() }
            val dest = File(dir, File(item.path).name)
            dest.writeBytes(Base64.decode(item.contentBase64, Base64.DEFAULT))
            rewritten[item.path] = dest.absolutePath
        }
        return rewritten
    }

    private fun rewritePath(path: String?, map: Map<String, String>): String? =
        path?.let { map[it] ?: it }

    private fun rewriteEvidence(raw: String?, map: Map<String, String>): String? =
        EvidenceCodec.encode(EvidenceCodec.decode(raw).map { map[it] ?: it })
}

private fun VehicleEntity.toDto() = VehicleBackupDto(
    id = id,
    plate = plate,
    type = type,
    brand = brand,
    line = line,
    model = model,
    year = year,
    currentMileage = currentMileage,
    color = color,
    vin = vin,
    fuelType = fuelType,
    engineCc = engineCc,
    photoUri = photoUri,
    isArchived = isArchived,
    isPrimary = isPrimary
)

private fun VehicleBackupDto.toEntity(targetId: Long) = VehicleEntity(
    id = targetId,
    plate = plate,
    type = type,
    brand = brand,
    line = line.ifBlank { model },
    model = model,
    year = year,
    currentMileage = currentMileage,
    color = color,
    vin = vin,
    fuelType = fuelType,
    engineCc = engineCc,
    photoUri = photoUri,
    isArchived = isArchived,
    isPrimary = isPrimary
)
