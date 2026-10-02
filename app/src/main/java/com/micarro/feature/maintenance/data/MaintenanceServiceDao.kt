package com.micarro.feature.maintenance.data

import androidx.room.*
import com.micarro.domain.model.CategoryExpenseDto
import kotlinx.coroutines.flow.Flow


@Dao
interface MaintenanceServiceDao {
    @Query("SELECT * FROM maintenance_services WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun observeServices(vehicleId: String): Flow<List<MaintenanceServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: MaintenanceServiceEntity)

    @Query("SELECT * FROM maintenance_services WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MaintenanceServiceEntity?

    @Query("SELECT * FROM maintenance_services")
    suspend fun getAll(): List<MaintenanceServiceEntity>

    @Query("DELETE FROM maintenance_services WHERE id = :id")
    suspend fun deleteById(id: String)

    // Consulta agregada para el resumen de gastos por categoría (RF-36)
    @Query("""
        SELECT category, SUM(totalCost) AS totalAmount 
        FROM maintenance_services 
        WHERE (:vehicleId = '' OR vehicleId = :vehicleId)
          AND date >= :startDateTimestamp 
        GROUP BY category 
        ORDER BY totalAmount DESC
    """)
    fun observeExpensesByCategory(
        vehicleId: String,
        startDateTimestamp: Long
    ): Flow<List<CategoryExpenseDto>>

    @Query("""
        SELECT s.id AS id,
               s.vehicleId AS vehicleId,
               s.title AS title,
               s.category AS category,
               s.serviceType AS serviceType,
               s.date AS date,
               s.mileage AS mileage,
               s.totalCost AS totalCost,
               s.workshopName AS workshopName,
               COALESCE(v.plate, s.vehicleId) AS vehiclePlate
        FROM maintenance_services s
        LEFT JOIN vehicles v ON v.id = CAST(s.vehicleId AS INTEGER)
        WHERE (:vehicleId IS NULL OR s.vehicleId = :vehicleId)
        AND (s.title LIKE '%' || :query || '%' OR s.category LIKE '%' || :query || '%' OR s.workshopName LIKE '%' || :query || '%')
        AND (:category IS NULL OR s.category = :category)
        AND (:serviceType IS NULL OR s.serviceType = :serviceType)
        AND (:workshop IS NULL OR s.workshopName LIKE '%' || :workshop || '%')
        AND (:minCost IS NULL OR s.totalCost >= :minCost)
        AND (:maxCost IS NULL OR s.totalCost <= :maxCost)
        AND (:startDate IS NULL OR s.date >= :startDate)
        AND (:endDate IS NULL OR s.date <= :endDate)
        ORDER BY s.date DESC
    """)
    fun observeHistory(
        vehicleId: String?,
        query: String,
        category: String?,
        serviceType: String?,
        workshop: String?,
        minCost: Double?,
        maxCost: Double?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<HistoryRow>>
}

data class HistoryRow(
    val id: String,
    val vehicleId: String,
    val title: String,
    val category: String,
    val serviceType: String,
    val date: Long,
    val mileage: Int,
    val totalCost: Double,
    val workshopName: String,
    val vehiclePlate: String
)