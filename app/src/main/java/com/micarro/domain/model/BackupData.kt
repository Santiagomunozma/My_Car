package com.micarro.feature.backup.domain.model

import com.google.gson.annotations.SerializedName

data class BackupData(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("vehicles") val vehicles: List<VehicleBackupDto> = emptyList(),
    @SerializedName("mileageRecords") val mileageRecords: List<MileageBackupDto> = emptyList(),
    @SerializedName("plans") val plans: List<PlanBackupDto> = emptyList(),
    @SerializedName("history") val history: List<HistoryBackupDto> = emptyList()
)

data class VehicleBackupDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("plate") val plate: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("year") val year: Int,
    @SerializedName("currentMileage") val currentMileage: Long
)

data class MileageBackupDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("vehiclePlate") val vehiclePlate: String,
    @SerializedName("mileage") val mileage: Long,
    @SerializedName("date") val date: Long
)

data class PlanBackupDto(
    @SerializedName("id") val id: String,
    @SerializedName("vehiclePlate") val vehiclePlate: String,
    @SerializedName("title") val title: String,
    @SerializedName("intervalMileage") val intervalMileage: Int
)

data class HistoryBackupDto(
    @SerializedName("id") val id: String,
    @SerializedName("vehiclePlate") val vehiclePlate: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("mileage") val mileage: Int,
    @SerializedName("totalCost") val totalCost: Double,
    @SerializedName("workshopName") val workshopName: String?,
    @SerializedName("date") val date: Long
)