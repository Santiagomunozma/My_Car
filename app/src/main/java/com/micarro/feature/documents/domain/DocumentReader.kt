package com.micarro.feature.documents.domain

interface DocumentReader {
    val isConfigured: Boolean
    suspend fun read(path: String, mimeType: String): DocumentExtraction
}
