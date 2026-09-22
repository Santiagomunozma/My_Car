package com.micarro.domain.usecase

import com.micarro.core.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val appDatabase: AppDatabase
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        appDatabase.clearAllTables()
    }
}