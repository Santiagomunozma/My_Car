package com.example.my_car.feature.mileage.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MileageDao {
    @Query("SELECT * FROM mileage_readings WHERE vehicle_id = :vehicleId ORDER BY date DESC, id DESC")
    fun observeByVehicle(vehicleId: String): Flow<List<MileageEntity>>

    @Insert
    suspend fun insert(reading: MileageEntity)

    @Query("SELECT * FROM mileage_readings WHERE vehicle_id = :vehicleId ORDER BY date DESC, id DESC LIMIT 1")
    suspend fun getLatest(vehicleId: String): MileageEntity?
}
