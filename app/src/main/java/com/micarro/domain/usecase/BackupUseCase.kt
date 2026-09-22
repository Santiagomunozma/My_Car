package com.micarro.domain.usecase

import com.micarro.feature.backup.domain.repository.BackupRepository
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class BackupUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend fun export(outputStream: OutputStream): Result<Unit> =
        repository.exportBackup(outputStream)

    suspend fun restore(inputStream: InputStream): Result<Unit> =
        repository.restoreBackup(inputStream)
}