package com.micarro.app.feature.documents.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM vehicle_documents WHERE vehicleId = :vehicleId ORDER BY expiryDateEpochMs ASC")
    fun observeDocuments(vehicleId: Long): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM vehicle_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("DELETE FROM vehicle_documents WHERE id = :id")
    suspend fun deleteDocument(id: Long)

    @Query("UPDATE vehicle_documents SET alertsEnabled = :enabled WHERE id = :id")
    suspend fun setAlertsEnabled(id: Long, enabled: Boolean)
}
