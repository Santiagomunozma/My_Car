package com.micarro.app.feature.mileage.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MileageDao {
    @Query("SELECT * FROM mileage_readings WHERE vehicleId = :vehicleId ORDER BY dateEpochMs DESC, id DESC")
    fun observeMileage(vehicleId: Long): Flow<List<MileageEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReading(reading: MileageEntity): Long

    @Query("SELECT * FROM mileage_readings WHERE vehicleId = :vehicleId ORDER BY dateEpochMs DESC, id DESC LIMIT 1")
    suspend fun getLatestReading(vehicleId: Long): MileageEntity?
}
