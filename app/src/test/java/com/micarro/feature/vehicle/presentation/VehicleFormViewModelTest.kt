package com.micarro.feature.vehicle.presentation

import androidx.lifecycle.SavedStateHandle
import com.micarro.MainDispatcherRule
import com.micarro.fakes.FakeVehiclePhotoStore
import com.micarro.fakes.InMemoryVehicleRepository
import com.micarro.feature.vehicle.domain.GetVehicleUseCase
import com.micarro.feature.vehicle.domain.SaveVehicleUseCase
import com.micarro.domain.model.VehicleType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = InMemoryVehicleRepository()
    private val photoStore = FakeVehiclePhotoStore()

    private fun viewModel() = VehicleFormViewModel(
        savedStateHandle = SavedStateHandle(),
        saveVehicle = SaveVehicleUseCase(repository, photoStore),
        getVehicle = GetVehicleUseCase(repository),
        photoStore = photoStore
    )

    @Test
    fun `doble guardado solo persiste una vez`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.updateDraft {
            it.copy(
                plate = "abc123", brand = "M", line = "L", model = "M",
                year = "2020", mileage = "100", type = VehicleType.CAR
            )
        }
        vm.onSave()
        vm.onSave() // segundo tap inmediato debe ser ignorado por isSaving
        advanceUntilIdle()

        assertTrue(vm.uiState.value.saved)
        assertEquals(1, repository.observeAllVehicles().first().size)
    }

    @Test
    fun `fallo de importacion de foto se reporta sin romper`() = runTest {
        photoStore.failOnImport = true
        val vm = viewModel()
        advanceUntilIdle()

        vm.onPhotoPicked("content://foto")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.photoImportFailed)
        assertEquals(null, vm.uiState.value.draft.photoUri)
    }

    @Test
    fun `foto importada queda en el draft`() = runTest {
        photoStore.importResult = "/interno/foto.jpg"
        val vm = viewModel()
        advanceUntilIdle()

        vm.onPhotoPicked("content://foto")
        advanceUntilIdle()

        assertEquals("/interno/foto.jpg", vm.uiState.value.draft.photoUri)
        assertTrue(!vm.uiState.value.photoImportFailed)
    }

    @Test
    fun `guardar con datos invalidos muestra errores sin crash`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.updateDraft { it.copy(plate = "", year = "abc") }
        vm.onSave()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errors.isNotEmpty())
        assertTrue(!vm.uiState.value.saved)
    }
}
