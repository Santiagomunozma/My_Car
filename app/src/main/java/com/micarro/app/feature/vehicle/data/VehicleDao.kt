package com.micarro.app.feature.vehicle.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE isArchived = 0 ORDER BY isPrimary DESC, id DESC")
    fun observeActiveVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles ORDER BY isArchived, isPrimary DESC, id DESC")
    fun observeAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleById(id: Long): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET isArchived = 1 WHERE id = :id")
    suspend fun archiveVehicle(id: Long)

    @Query("UPDATE vehicles SET isArchived = 0 WHERE id = :id")
    suspend fun reactivateVehicle(id: Long)

    @Query("SELECT COUNT(*) FROM vehicles WHERE plate = :plate AND (:excludingId IS NULL OR id != :excludingId)")
    suspend fun countPlates(plate: String, excludingId: Long?): Int

    @Query("UPDATE vehicles SET isPrimary = 0 WHERE id != :primaryId")
    suspend fun clearOtherPrimaryVehicles(primaryId: Long)

    @Query("UPDATE vehicles SET currentMileage = :mileage WHERE id = :vehicleId")
    suspend fun updateMileage(vehicleId: Long, mileage: Long)
}
