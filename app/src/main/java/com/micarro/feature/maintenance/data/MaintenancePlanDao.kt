package com.micarro.feature.maintenance.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenancePlanDao {
    @Query("SELECT * FROM maintenance_plans WHERE vehicleId = :vehicleId")
    fun observePlans(vehicleId: String): Flow<List<MaintenancePlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: MaintenancePlanEntity)

    @Update
    suspend fun update(plan: MaintenancePlanEntity)

    @Query("DELETE FROM maintenance_plans WHERE id = :planId")
    suspend fun delete(planId: String)

    @Query("SELECT COUNT(*) FROM maintenance_services WHERE planId = :planId")
    suspend fun getServiceCountForPlan(planId: String): Int
}
