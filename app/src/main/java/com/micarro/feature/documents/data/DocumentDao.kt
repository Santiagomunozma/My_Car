package com.micarro.feature.documents.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM vehicle_documents WHERE vehicle_id = :vehicleId ORDER BY expiration_date ASC")
    fun observeByVehicle(vehicleId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM vehicle_documents ORDER BY expiration_date ASC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM vehicle_documents WHERE id = :id")
    suspend fun getById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(document: DocumentEntity)

    @Query("DELETE FROM vehicle_documents WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE vehicle_documents SET alerts_enabled = :enabled WHERE id = :id")
    suspend fun setAlertsEnabled(id: String, enabled: Boolean)
}
