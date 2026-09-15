package com.micarro.app.domain.model

enum class VehicleType { CAR, TRUCK, MOTORCYCLE }
enum class FuelType { GASOLINE, DIESEL, ELECTRIC, HYBRID, GAS }
enum class MaintenanceStatus { UP_TO_DATE, UPCOMING, OVERDUE, UNSCHEDULED }
enum class DocumentType { SOAT, TECHNICAL_REVIEW, INSURANCE, OTHER }
enum class DocumentStatus { VALID, EXPIRING_SOON, EXPIRED }

data class Vehicle(
    val id: Long = 0L,
    val plate: String,
    val type: VehicleType,
    val brand: String,
    val line: String,
    val model: String,
    val year: Int,
    val currentMileage: Long,
    val color: String? = null,
    val vin: String? = null,
    val fuelType: FuelType? = null,
    val engineCc: Int? = null,
    val photoUri: String? = null,
    val isPrimary: Boolean = false,
    val isArchived: Boolean = false
)

data class MileageReading(
    val id: Long = 0L,
    val vehicleId: Long,
    val mileage: Long,
    val dateEpochMs: Long
)

data class MaintenancePlan(
    val id: Long = 0L,
    val vehicleId: Long,
    val name: String,
    val category: String,
    val description: String,
    val intervalKm: Long? = null,
    val intervalDays: Int? = null,
    val status: MaintenanceStatus = MaintenanceStatus.UNSCHEDULED
)

data class VehicleDocument(
    val id: Long = 0L,
    val vehicleId: Long,
    val type: DocumentType,
    val name: String? = null,
    val expiryDateEpochMs: Long,
    val notes: String? = null,
    val alertsEnabled: Boolean = true
)
