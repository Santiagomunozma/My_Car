package com.example.my_car.domain.usecase

import com.example.my_car.data.repository.FakeVehicleRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetDashboardSummaryUseCaseTest {

    @Test
    fun `when no vehicles exist summary returns null main vehicle`() = runTest {
        val fakeRepo = FakeVehicleRepositoryImpl()
        val useCase = GetDashboardSummaryUseCase(fakeRepo)

        val result = useCase().first()

        assertNull(result.mainVehicle)
        assertEquals(0, result.activeAlertsCount)
    }
}