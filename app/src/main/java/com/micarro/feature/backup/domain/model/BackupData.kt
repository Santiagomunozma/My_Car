package com.micarro.feature.backup.domain.model

import com.google.gson.annotations.SerializedName

data class BackupData(
    @SerializedName("version") val version: Int = 2,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("vehicles") val vehicles: List<VehicleBackupDto>? = emptyList(),
    @SerializedName("mileageRecords") val mileageRecords: List<MileageBackupDto>? = emptyList(),
    @SerializedName("documents") val documents: List<DocumentBackupDto>? = emptyList(),
    @SerializedName("plans") val plans: List<PlanBackupDto>? = emptyList(),
    @SerializedName("history") val history: List<HistoryBackupDto>? = emptyList(),
    @SerializedName("parts") val parts: List<PartBackupDto>? = emptyList(),
    @SerializedName("files") val files: List<FileBackupDto>? = emptyList()
)

data class VehicleBackupDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("plate") val plate: String = "",
    @SerializedName("type") val type: String = "CAR",
    @SerializedName("brand") val brand: String = "",
    @SerializedName("line") val line: String = "",
    @SerializedName("model") val model: String = "",
    @SerializedName("year") val year: Int = 0,
    @SerializedName("currentMileage") val currentMileage: Long = 0,
    @SerializedName("color") val color: String? = null,
    @SerializedName("vin") val vin: String? = null,
    @SerializedName("fuelType") val fuelType: String? = null,
    @SerializedName("engineCc") val engineCc: Int? = null,
    @SerializedName("photoUri") val photoUri: String? = null,
    @SerializedName("isArchived") val isArchived: Boolean = false,
    @SerializedName("isPrimary") val isPrimary: Boolean = false
)

data class MileageBackupDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("vehicleId") val vehicleId: Long = 0,
    @SerializedName("vehiclePlate") val vehiclePlate: String = "",
    @SerializedName("mileage") val mileage: Long = 0,
    @SerializedName("date") val date: Long = 0,
    @SerializedName("note") val note: String? = null
)

data class DocumentBackupDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("vehicleId") val vehicleId: Long = 0,
    @SerializedName("type") val type: String = "OTHER",
    @SerializedName("name") val name: String = "",
    @SerializedName("expirationDate") val expirationDate: Long = 0,
    @SerializedName("issuer") val issuer: String? = null,
    @SerializedName("alertsEnabled") val alertsEnabled: Boolean = true,
    @SerializedName("notes") val notes: String? = null
)

data class PlanBackupDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("vehicleId") val vehicleId: String = "",
    @SerializedName("vehiclePlate") val vehiclePlate: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("intervalMileage") val intervalMileage: Int = 0,
    @SerializedName("intervalMonths") val intervalMonths: Int = 0,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("nextDeadlineDate") val nextDeadlineDate: Long? = null,
    @SerializedName("nextLimitMileage") val nextLimitMileage: Int? = null
)

data class HistoryBackupDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("vehicleId") val vehicleId: String = "",
    @SerializedName("vehiclePlate") val vehiclePlate: String = "",
    @SerializedName("planId") val planId: String? = null,
    @SerializedName("title") val title: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("serviceType") val serviceType: String = "CORRECTIVE",
    @SerializedName("mileage") val mileage: Int = 0,
    @SerializedName("laborCost") val laborCost: Double = 0.0,
    @SerializedName("otherCosts") val otherCosts: Double = 0.0,
    @SerializedName("totalCost") val totalCost: Double = 0.0,
    @SerializedName("workshopName") val workshopName: String? = null,
    @SerializedName("date") val date: Long = 0,
    @SerializedName("description") val description: String = "",
    @SerializedName("evidenceUri") val evidenceUri: String? = null
)

data class PartBackupDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("serviceId") val serviceId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("quantity") val quantity: Int = 1,
    @SerializedName("cost") val cost: Double = 0.0,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("installationDate") val installationDate: Long? = null,
    @SerializedName("warranty") val warranty: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class FileBackupDto(
    @SerializedName("path") val path: String = "",
    @SerializedName("contentBase64") val contentBase64: String = ""
)
