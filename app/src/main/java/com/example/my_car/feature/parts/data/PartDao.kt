package com.example.my_car.feature.parts.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PartDao {
    @Query("SELECT * FROM parts WHERE serviceId = :serviceId")
    suspend fun getPartsForService(serviceId: String): List<PartEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parts: List<PartEntity>)

    @Query("""
        SELECT p.* FROM parts p 
        INNER JOIN maintenance_services s ON p.serviceId = s.id 
        WHERE s.vehicleId = :vehicleId
    """)
    fun observeInstalledParts(vehicleId: String): kotlinx.coroutines.flow.Flow<List<PartEntity>>
}
