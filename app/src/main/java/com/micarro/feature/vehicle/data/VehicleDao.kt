package com.micarro.feature.vehicle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE is_archived = 0 ORDER BY is_main_vehicle DESC, brand ASC")
    fun observeActive(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles ORDER BY is_main_vehicle DESC, brand ASC")
    fun observeAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getById(id: String): VehicleEntity?

    @Insert
    suspend fun insert(vehicle: VehicleEntity)

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET is_archived = 1 WHERE id = :id")
    suspend fun archive(id: String)

    @Query("UPDATE vehicles SET is_archived = 0 WHERE id = :id")
    suspend fun reactivate(id: String)

    @Query("SELECT COUNT(*) FROM vehicles WHERE plate = :plate AND id != :excludingId")
    suspend fun countPlate(plate: String, excludingId: String): Int

    @Query("UPDATE vehicles SET is_main_vehicle = 0 WHERE is_main_vehicle = 1")
    suspend fun clearMainVehicle()

    @Query("UPDATE vehicles SET is_main_vehicle = 1 WHERE id = :id")
    suspend fun setMain(id: String)

    @Transaction
    suspend fun setMainVehicle(id: String) {
        clearMainVehicle()
        setMain(id)
    }

    @Query("UPDATE vehicles SET current_mileage = :mileage WHERE id = :id")
    suspend fun updateCurrentMileage(id: String, mileage: Int)
}
