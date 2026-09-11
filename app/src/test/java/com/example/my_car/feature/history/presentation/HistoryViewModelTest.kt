package com.example.my_car.feature.history.presentation

import com.example.my_car.data.repository.FakeMaintenanceRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when query changes filter state reflects new value`() = runTest {
        val fakeRepo = FakeMaintenanceRepositoryImpl()
        val viewModel = HistoryViewModel(fakeRepo)

        viewModel.onQueryChanged("Frenos")

        assertEquals("Frenos", viewModel.filterState.value.query)
    }
}