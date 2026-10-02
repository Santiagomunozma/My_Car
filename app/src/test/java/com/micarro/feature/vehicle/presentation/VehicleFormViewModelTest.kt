package com.micarro.feature.vehicle.presentation

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.SavedStateHandle
import com.micarro.MainDispatcherRule
import com.micarro.fakes.FakeVehiclePhotoStore
import com.micarro.fakes.InMemoryVehicleRepository
import com.micarro.feature.vehicle.data.VehicleCatalogHelper
import com.micarro.feature.vehicle.domain.GetVehicleUseCase
import com.micarro.feature.vehicle.domain.SaveVehicleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private object DummyContext : ContextWrapper(null)

class FakeVehicleCatalogHelper : VehicleCatalogHelper(DummyContext) {
    override suspend fun getBrands(typeKey: String): List<String> = listOf("BYD", "Chevrolet", "Renault")
    override suspend fun getLines(typeKey: String, brand: String): List<String> = listOf("Onix", "Tracker")
}

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = InMemoryVehicleRepository()
    private val photoStore = FakeVehiclePhotoStore()
    private val catalogHelper = FakeVehicleCatalogHelper()

    private fun viewModel() = VehicleFormViewModel(
        savedStateHandle = SavedStateHandle(),
        saveVehicle = SaveVehicleUseCase(repository, photoStore),
        getVehicle = GetVehicleUseCase(repository),
        photoStore = photoStore,
        catalogHelper = catalogHelper
    )

    @Test
    fun `doble guardado solo persiste una vez`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        vm.onPlateChanged("AAA123")
        vm.onBrandSelected("Chevrolet")
        vm.onLineSelected("Onix")
        vm.onYearSelected("2020")
        vm.onMileageChanged("100")
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

        vm.onPlateChanged("")
        vm.onYearSelected("abc")
        vm.onSave()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.errors.isNotEmpty())
        assertTrue(!vm.uiState.value.saved)
    }
}
