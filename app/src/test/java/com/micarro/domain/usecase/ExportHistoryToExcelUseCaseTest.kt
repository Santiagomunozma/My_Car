package com.micarro.domain.usecase

import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import com.micarro.domain.model.ServiceType
import com.micarro.domain.model.Vehicle
import com.micarro.domain.model.VehicleType
import com.micarro.fakes.InMemoryMaintenanceRepository
import com.micarro.fakes.InMemoryPartRepository
import com.micarro.fakes.InMemoryVehicleRepository
import kotlinx.coroutines.test.runTest
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ExportHistoryToExcelUseCaseTest {

    @Test
    fun `excel export includes history parts and is not empty`() = runTest {
        val maintenanceRepository = InMemoryMaintenanceRepository()
        val partRepository = InMemoryPartRepository()
        val vehicleRepository = InMemoryVehicleRepository()

        vehicleRepository.createVehicle(
            Vehicle(
                id = 1L,
                plate = "ABC123",
                type = VehicleType.CAR,
                brand = "Toyota",
                line = "Corolla",
                year = 2020,
                currentMileage = 45000
            )
        )
        maintenanceRepository.registerService(
            MaintenanceService(
                id = "svc-1",
                vehicleId = "1",
                title = "Cambio de aceite",
                category = "Motor",
                date = 1_700_000_000_000L,
                mileage = 45000,
                totalCost = 180_000.0,
                workshopName = "Taller Central",
                serviceType = ServiceType.PREVENTIVE,
                laborCost = 80_000.0,
                otherCosts = 20_000.0,
                description = "Servicio 10.000 km"
            ),
            listOf(
                Part(
                    id = "part-1",
                    serviceId = "svc-1",
                    name = "Aceite 5W30",
                    quantity = 4,
                    cost = 20_000.0,
                    brand = "Mobil"
                )
            )
        )
        partRepository.saveParts(
            listOf(
                Part(
                    id = "part-1",
                    serviceId = "svc-1",
                    name = "Aceite 5W30",
                    quantity = 4,
                    cost = 20_000.0,
                    brand = "Mobil"
                )
            )
        )

        val useCase = ExportHistoryToExcelUseCase(
            maintenanceRepository,
            partRepository,
            vehicleRepository
        )
        val output = ByteArrayOutputStream()

        val result = useCase(output, vehicleId = "1", currencySymbol = "$")

        assertTrue(result.isSuccess)
        assertTrue(output.size() > 0)

        XSSFWorkbook(ByteArrayInputStream(output.toByteArray())).use { workbook ->
            assertEquals(3, workbook.numberOfSheets)
            assertEquals("Resumen", workbook.getSheetAt(0).sheetName)
            assertEquals("Historial", workbook.getSheetAt(1).sheetName)
            assertEquals("Repuestos", workbook.getSheetAt(2).sheetName)

            val history = workbook.getSheet("Historial")
            assertEquals("Cambio de aceite", history.getRow(3).getCell(3).stringCellValue)
            assertEquals("Toyota Corolla 2020", history.getRow(3).getCell(2).stringCellValue)
            assertEquals(180_000.0, history.getRow(3).getCell(10).numericCellValue, 0.01)

            val parts = workbook.getSheet("Repuestos")
            assertEquals("Aceite 5W30", parts.getRow(3).getCell(3).stringCellValue)
            assertEquals(80_000.0, parts.getRow(3).getCell(9).numericCellValue, 0.01)
        }
    }
}
