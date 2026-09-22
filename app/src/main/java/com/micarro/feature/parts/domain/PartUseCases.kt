package com.micarro.feature.parts.domain

import com.micarro.domain.model.Part
import com.micarro.domain.repository.PartRepository
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
