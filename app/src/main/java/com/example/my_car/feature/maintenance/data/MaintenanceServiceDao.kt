package com.example.my_car.feature.maintenance.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceServiceDao {
    @Query("SELECT * FROM maintenance_services WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun observeServices(vehicleId: String): Flow<List<MaintenanceServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: MaintenanceServiceEntity)

    @Query("""
        SELECT * FROM maintenance_services 
        WHERE (:vehicleId IS NULL OR vehicleId = :vehicleId)
        AND (title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR workshopName LIKE '%' || :query || '%')
        AND (:category IS NULL OR category = :category)
        AND (:minCost IS NULL OR totalCost >= :minCost)
        AND (:maxCost IS NULL OR totalCost <= :maxCost)
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        ORDER BY date DESC
    """)
    fun observeHistory(
        vehicleId: String?,
        query: String,
        category: String?,
        minCost: Double?,
        maxCost: Double?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<MaintenanceServiceEntity>>
}
