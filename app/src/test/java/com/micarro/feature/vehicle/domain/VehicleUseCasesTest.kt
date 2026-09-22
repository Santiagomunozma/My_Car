package com.micarro.feature.vehicle.domain

import com.micarro.domain.model.Vehicle
import com.micarro.domain.model.VehicleType
import com.micarro.fakes.FakeVehiclePhotoStore
import com.micarro.fakes.InMemoryVehicleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VehicleUseCasesTest {

    private lateinit var repository: InMemoryVehicleRepository
    private lateinit var photoStore: FakeVehiclePhotoStore
    private lateinit var saveVehicle: SaveVehicleUseCase

    private fun validDraft() = VehicleDraft(
        plate = "abc 123",
        type = VehicleType.CAR,
        brand = "Mazda",
        line = "3",
        model = "Touring",
        year = "2020",
        mileage = "45000"
    )

    @Before
    fun setUp() {
        repository = InMemoryVehicleRepository()
        photoStore = FakeVehiclePhotoStore()
        saveVehicle = SaveVehicleUseCase(repository, photoStore)
    }

    @Test
    fun `registro valido guarda con placa normalizada`() = runTest {
        val result = saveVehicle(validDraft())
        assertTrue(result is SaveVehicleResult.Success)
        val saved = repository.observeAllVehicles().first().single()
        assertEquals("ABC123", saved.plate)
        assertEquals(45000, saved.currentMileage)
        assertEquals("Mazda", saved.brand)
    }

    @Test
    fun `placa duplicada es rechazada`() = runTest {
        saveVehicle(validDraft())
        val result = saveVehicle(validDraft().copy(plate = " abc123 "))
        assertTrue(result is SaveVehicleResult.Invalid)
        val errors = (result as SaveVehicleResult.Invalid).errors
        assertEquals(VehicleError.DUPLICATE_PLATE, errors[VehicleField.PLATE])
        assertEquals(1, repository.observeAllVehicles().first().size)
    }

    @Test
    fun `placa duplicada se permite al editar el mismo vehiculo`() = runTest {
        saveVehicle(validDraft())
        val id = repository.observeAllVehicles().first().single().id
        val result = saveVehicle(validDraft().copy(id = id, plate = "ABC123"))
        assertTrue(result is SaveVehicleResult.Success)
    }

    @Test
    fun `anio invalido es rechazado`() = runTest {
        val result = saveVehicle(validDraft().copy(year = "1800"))
        val errors = (result as SaveVehicleResult.Invalid).errors
        assertEquals(VehicleError.INVALID_VALUE, errors[VehicleField.YEAR])
    }

    @Test
    fun `anio no numerico es rechazado`() = runTest {
        val result = saveVehicle(validDraft().copy(year = "abc"))
        val errors = (result as SaveVehicleResult.Invalid).errors
        assertEquals(VehicleError.INVALID_FORMAT, errors[VehicleField.YEAR])
    }

    @Test
    fun `kilometraje negativo o no numerico es rechazado`() = runTest {
        val negative = saveVehicle(validDraft().copy(mileage = "-50"))
        assertEquals(
            VehicleError.INVALID_VALUE,
            (negative as SaveVehicleResult.Invalid).errors[VehicleField.MILEAGE]
        )
        val notNumber = saveVehicle(validDraft().copy(mileage = "12.5"))
        assertEquals(
            VehicleError.INVALID_FORMAT,
            (notNumber as SaveVehicleResult.Invalid).errors[VehicleField.MILEAGE]
        )
        assertTrue(repository.observeAllVehicles().first().isEmpty())
    }

    @Test
    fun `vin invalido es rechazado`() = runTest {
        val result = saveVehicle(validDraft().copy(vin = "CORTO"))
        val errors = (result as SaveVehicleResult.Invalid).errors
        assertEquals(VehicleError.INVALID_FORMAT, errors[VehicleField.VIN])
    }

    @Test
    fun `cilindraje invalido es rechazado`() = runTest {
        val result = saveVehicle(validDraft().copy(engineCc = "0"))
        val errors = (result as SaveVehicleResult.Invalid).errors
        assertEquals(VehicleError.INVALID_VALUE, errors[VehicleField.ENGINE_CC])
        val notNumber = saveVehicle(validDraft().copy(engineCc = "abc"))
        assertEquals(
            VehicleError.INVALID_FORMAT,
            (notNumber as SaveVehicleResult.Invalid).errors[VehicleField.ENGINE_CC]
        )
    }

    @Test
    fun `campos obligatorios vacios son rechazados`() = runTest {
        val result = saveVehicle(validDraft().copy(brand = "", plate = ""))
        val errors = (result as SaveVehicleResult.Invalid).errors
        assertEquals(VehicleError.REQUIRED, errors[VehicleField.BRAND])
        assertEquals(VehicleError.REQUIRED, errors[VehicleField.PLATE])
    }

    @Test
    fun `edicion conserva kilometraje archivado y principal`() = runTest {
        val vehicle = Vehicle(
            plate = "ABC123", type = VehicleType.CAR, brand = "Mazda", line = "3",
            model = "Touring", year = 2020, currentMileage = 80000,
            isArchived = true, isMainVehicle = true
        )
        repository.seed(vehicle)
        val result = saveVehicle(
            validDraft().copy(id = vehicle.id, mileage = "999999", brand = "Mazda Editada")
        )
        assertTrue(result is SaveVehicleResult.Success)
        val updated = repository.getVehicleById(vehicle.id)!!
        assertEquals("Mazda Editada", updated.brand)
        assertEquals(80000, updated.currentMileage)
        assertTrue(updated.isArchived)
        assertTrue(updated.isMainVehicle)
    }

    @Test
    fun `edicion de vehiculo desaparecido falla`() = runTest {
        val result = saveVehicle(validDraft().copy(id = "inexistente"))
        assertTrue(result is SaveVehicleResult.Failure)
    }

    @Test
    fun `archivar y reactivar conservan el vehiculo`() = runTest {
        val vehicle = Vehicle(
            plate = "ABC123", type = VehicleType.CAR, brand = "M", line = "L",
            model = "M", year = 2020, currentMileage = 10
        )
        repository.seed(vehicle)
        val archive = ArchiveVehicleUseCase(repository)
        val reactivate = ReactivateVehicleUseCase(repository)

        archive(vehicle.id)
        assertTrue(repository.getVehicleById(vehicle.id)!!.isArchived)
        assertTrue(repository.observeVehicles().first().isEmpty())

        reactivate(vehicle.id)
        assertTrue(!repository.getVehicleById(vehicle.id)!!.isArchived)
        assertEquals(1, repository.observeVehicles().first().size)
    }

    @Test
    fun `solo un vehiculo principal`() = runTest {
        val a = Vehicle(plate = "A1", type = VehicleType.CAR, brand = "a", line = "a", model = "a", year = 2020, currentMileage = 0)
        val b = a.copy(id = "otro", plate = "B2")
        repository.seed(a, b)
        val setMain = SetMainVehicleUseCase(repository)

        setMain(a.id)
        assertEquals(a.id, repository.observeAllVehicles().first().single { it.isMainVehicle }.id)

        setMain(b.id)
        val all = repository.observeAllVehicles().first()
        assertEquals(b.id, all.single { it.isMainVehicle }.id)
        assertEquals(1, all.count { it.isMainVehicle })
    }

    @Test
    fun `reemplazar foto borra la anterior gestionada`() = runTest {
        val vehicle = Vehicle(
            plate = "ABC123", type = VehicleType.CAR, brand = "M", line = "L",
            model = "M", year = 2020, currentMileage = 0, photoUri = "/photos/vieja.jpg"
        )
        repository.seed(vehicle)
        saveVehicle(validDraft().copy(id = vehicle.id, photoUri = "/photos/nueva.jpg"))
        assertEquals(listOf("/photos/vieja.jpg"), photoStore.deleted)
    }

    @Test
    fun `quitar foto borra el archivo gestionado`() = runTest {
        val vehicle = Vehicle(
            plate = "ABC123", type = VehicleType.CAR, brand = "M", line = "L",
            model = "M", year = 2020, currentMileage = 0, photoUri = "/photos/vieja.jpg"
        )
        repository.seed(vehicle)
        saveVehicle(validDraft().copy(id = vehicle.id, photoUri = null))
        assertEquals(listOf("/photos/vieja.jpg"), photoStore.deleted)
        assertNull(repository.getVehicleById(vehicle.id)!!.photoUri)
    }

    @Test
    fun `mantener foto no borra el archivo`() = runTest {
        val vehicle = Vehicle(
            plate = "ABC123", type = VehicleType.CAR, brand = "M", line = "L",
            model = "M", year = 2020, currentMileage = 0, photoUri = "/photos/vieja.jpg"
        )
        repository.seed(vehicle)
        saveVehicle(validDraft().copy(id = vehicle.id, photoUri = "/photos/vieja.jpg"))
        assertTrue(photoStore.deleted.isEmpty())
        assertEquals("/photos/vieja.jpg", repository.getVehicleById(vehicle.id)!!.photoUri)
    }

    @Test
    fun `opcionales vacios se guardan como null`() = runTest {
        saveVehicle(validDraft().copy(vin = "", engineCc = "", color = "  "))
        val saved = repository.observeAllVehicles().first().single()
        assertNull(saved.vin)
        assertNull(saved.engineCc)
        assertNull(saved.color)
        assertNotNull(saved.id)
    }
}
