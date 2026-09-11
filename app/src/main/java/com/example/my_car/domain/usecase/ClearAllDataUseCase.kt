package com.example.my_car.domain.usecase

import com.example.my_car.core.database.AppDatabase
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