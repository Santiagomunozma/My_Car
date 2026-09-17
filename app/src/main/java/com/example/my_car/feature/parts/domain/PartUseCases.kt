package com.example.my_car.feature.parts.domain

import com.example.my_car.domain.model.Part
import com.example.my_car.domain.repository.PartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveInstalledPartsUseCase @Inject constructor(
    private val repository: PartRepository
) {
    operator fun invoke(vehicleId: String): Flow<List<Part>> =
        repository.observeInstalledParts(vehicleId)
}

class PartUseCases @Inject constructor(
    val observeInstalledParts: ObserveInstalledPartsUseCase
)
