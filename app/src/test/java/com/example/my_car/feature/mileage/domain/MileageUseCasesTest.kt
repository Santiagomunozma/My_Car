package com.example.my_car.feature.mileage.domain

import com.example.my_car.domain.model.MileageRecord
import com.example.my_car.domain.model.Vehicle
import com.example.my_car.domain.model.VehicleType
import com.example.my_car.fakes.InMemoryMileageRepository
import com.example.my_car.fakes.InMemoryVehicleRepository
import com.example.my_car.fakes.RecordingMileageAlertNotifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class MileageUseCasesTest {

    private lateinit var mileageRepo: InMemoryMileageRepository
    private lateinit var vehicleRepo: InMemoryVehicleRepository
    private lateinit var notifier: RecordingMileageAlertNotifier
    private lateinit var addReading: AddMileageReadingUseCase

    private val vehicle = Vehicle(
        id = "v1", plate = "ABC123", type = VehicleType.CAR, brand = "M",
        line = "L", model = "M", year = 2020, currentMileage = 50000
    )

    private val todayMillis = MileageRules.dateToMillis(LocalDate.now())

    @Before
    fun setUp() {
        mileageRepo = InMemoryMileageRepository()
        vehicleRepo = InMemoryVehicleRepository()
        notifier = RecordingMileageAlertNotifier()
        addReading = AddMileageReadingUseCase(mileageRepo, vehicleRepo, notifier)
        vehicleRepo.seed(vehicle)
    }

    @Test
    fun `lectura mayor se guarda y actualiza el kilometraje`() = runTest {
        val result = addReading("v1", "51000", todayMillis, null, false)
        assertTrue(result is AddReadingResult.Success)
        assertEquals(51000, vehicleRepo.getVehicleById("v1")!!.currentMileage)
        assertEquals(1, mileageRepo.observeMileage("v1").first().size)
        assertEquals(listOf("v1" to 51000), notifier.calls)
    }

    @Test
    fun `lectura menor requiere confirmacion y no se guarda`() = runTest {
        val result = addReading("v1", "49000", todayMillis, null, false)
        assertTrue(result is AddReadingResult.RequiresConfirmation)
        assertEquals(50000, (result as AddReadingResult.RequiresConfirmation).previousReading)
        assertTrue(mileageRepo.observeMileage("v1").first().isEmpty())
        assertEquals(50000, vehicleRepo.getVehicleById("v1")!!.currentMileage)
        assertTrue(notifier.calls.isEmpty())
    }

    @Test
    fun `lectura menor confirmada se guarda`() = runTest {
        val result = addReading("v1", "49000", todayMillis, "corrección", true)
        assertTrue(result is AddReadingResult.Success)
        assertEquals(49000, mileageRepo.getLatestMileage("v1")!!.reading)
    }

    @Test
    fun `fecha futura es rechazada`() = runTest {
        val future = MileageRules.dateToMillis(LocalDate.now().plusDays(1))
        val result = addReading("v1", "51000", future, null, false)
        assertTrue((result as AddReadingResult.Invalid).errors.contains(MileageError.FUTURE_DATE))
    }

    @Test
    fun `odometro negativo vacio o decimal es rechazado`() = runTest {
        listOf("-5" to MileageError.NEGATIVE, "" to MileageError.EMPTY, "12.5" to MileageError.NOT_A_NUMBER, "abc" to MileageError.NOT_A_NUMBER)
            .forEach { (input, error) ->
                val result = addReading("v1", input, todayMillis, null, false)
                assertTrue(
                    "input=$input",
                    (result as AddReadingResult.Invalid).errors.contains(error)
                )
            }
        assertTrue(mileageRepo.observeMileage("v1").first().isEmpty())
    }

    @Test
    fun `vehiculo inexistente produce fallo`() = runTest {
        val result = addReading("fantasma", "1000", todayMillis, null, false)
        assertTrue(result is AddReadingResult.Failure)
    }

    @Test
    fun `lectura con fecha antigua no pisa el kilometraje actual`() = runTest {
        mileageRepo.addMileage(MileageRecord(vehicleId = "v1", date = todayMillis, reading = 60000))
        vehicleRepo.updateCurrentMileage("v1", 60000)
        val oldDate = MileageRules.dateToMillis(LocalDate.now().minusDays(10))
        val result = addReading("v1", "61000", oldDate, null, false)
        // 61000 > referencia 60000 → éxito, pero la última cronológica sigue siendo 60000
        assertTrue(result is AddReadingResult.Success)
        assertEquals(60000, vehicleRepo.getVehicleById("v1")!!.currentMileage)
    }

    @Test
    fun `notifier recibe la ultima lectura cronologica`() = runTest {
        mileageRepo.addMileage(MileageRecord(vehicleId = "v1", date = todayMillis, reading = 60000))
        vehicleRepo.updateCurrentMileage("v1", 60000)
        val oldDate = MileageRules.dateToMillis(LocalDate.now().minusDays(10))
        addReading("v1", "61000", oldDate, null, false)
        assertEquals(listOf("v1" to 60000), notifier.calls)
    }
}
