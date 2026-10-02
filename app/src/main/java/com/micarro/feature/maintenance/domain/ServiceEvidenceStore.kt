package com.micarro.feature.maintenance.domain

/**
 * Copia fotos o documentos de soporte de un servicio al almacenamiento interno.
 */
interface ServiceEvidenceStore {
    suspend fun import(sourceUri: String): String
    suspend fun deleteAll()
}
