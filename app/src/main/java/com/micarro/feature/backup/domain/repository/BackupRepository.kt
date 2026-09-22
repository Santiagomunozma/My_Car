package com.micarro.feature.backup.domain.repository

import java.io.InputStream
import java.io.OutputStream

interface BackupRepository {
    suspend fun exportBackup(outputStream: OutputStream): Result<Unit>
    suspend fun restoreBackup(inputStream: InputStream): Result<Unit>
}