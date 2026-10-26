package com.micarro.feature.documents.domain

import java.io.IOException

data class StoredDocumentFile(
    val path: String,
    val mimeType: String
)

class DocumentFileTooLarge : IOException("El archivo supera el tamaño permitido")

interface DocumentFileStore {
    suspend fun importUri(sourceUri: String): StoredDocumentFile
    suspend fun importLocalFile(path: String, mimeType: String): StoredDocumentFile
    fun createCameraFile(): String
    suspend fun delete(path: String?)
    suspend fun deleteAll()
}
