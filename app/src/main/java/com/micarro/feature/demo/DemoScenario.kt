package com.micarro.feature.demo

import com.micarro.domain.model.DocumentType
import com.micarro.domain.model.FuelType
import com.micarro.domain.model.ServiceType
import com.micarro.domain.model.VehicleType
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Historial fijo de la ponencia. Las fechas salen del día en que se carga,
 * así el aceite sigue vencido y la técnico-mecánica sigue próxima el martes.
 */
object DemoScenario {
    const val CAR_ID = 1L
    const val MOTO_ID = 2L
    const val ARCHIVED_ID = 3L

    const val CAR_PLATE = "KHM452"
    const val MOTO_PLATE = "XYZ89F"
    const val ARCHIVED_PLATE = "RTR108"

    const val CAR_KM = 45_200L
    const val MOTO_KM = 12_840L
    const val ARCHIVED_KM = 98_600L

    const val OIL_PLAN = "demo-oil"
    const val BRAKE_PLAN = "demo-brakes"
    const val TIRE_PLAN = "demo-tires"
    const val BATTERY_PLAN = "demo-battery"
    const val LIGHTS_PLAN = "demo-lights"
    const val CHAIN_PLAN = "demo-chain"
    const val ARCHIVED_OIL_PLAN = "demo-archived-oil"

    const val OIL_MARGIN_DAYS = 15
    const val OIL_MARGIN_KM = 500
    const val DOCUMENT_ANTICIPATION_DAYS = 30

    data class Vehicle(
        val id: Long,
        val plate: String,
        val type: VehicleType,
        val brand: String,
        val line: String,
        val model: String,
        val year: Int,
        val currentMileage: Long,
        val color: String?,
        val vin: String?,
        val fuelType: FuelType?,
        val engineCc: Int?,
        val isArchived: Boolean,
        val isPrimary: Boolean
    )

    data class Reading(
        val id: Long,
        val vehicleId: Long,
        val date: LocalDate,
        val mileage: Long,
        val note: String?
    )

    data class Document(
        val id: Long,
        val vehicleId: Long,
        val type: DocumentType,
        val name: String,
        val expirationDate: LocalDate,
        val issuer: String?,
        val alertsEnabled: Boolean,
        val referenceNumber: String?
    )

    data class Plan(
        val id: String,
        val vehicleId: Long,
        val title: String,
        val category: String,
        val intervalMileage: Int,
        val intervalMonths: Int,
        val isActive: Boolean,
        val description: String,
        val deadline: LocalDate?,
        val limitMileage: Int?,
        val marginDays: Int = OIL_MARGIN_DAYS,
        val marginKm: Int = OIL_MARGIN_KM,
        val alertsEnabled: Boolean = true
    )

    data class Service(
        val id: String,
        val vehicleId: Long,
        val planId: String?,
        val title: String,
        val category: String,
        val date: LocalDate,
        val mileage: Int,
        val laborCost: Double,
        val otherCosts: Double,
        val totalCost: Double,
        val workshopName: String,
        val serviceType: ServiceType,
        val description: String
    )

    data class Part(
        val id: String,
        val serviceId: String,
        val name: String,
        val quantity: Int,
        val cost: Double,
        val brand: String?,
        val reference: String?,
        val provider: String?,
        val installationDate: LocalDate?,
        val warranty: String?,
        val notes: String?
    )

    data class Snapshot(
        val today: LocalDate,
        val vehicles: List<Vehicle>,
        val readings: List<Reading>,
        val documents: List<Document>,
        val plans: List<Plan>,
        val services: List<Service>,
        val parts: List<Part>
    )

    fun build(today: LocalDate = LocalDate.now()): Snapshot {
        val car = Vehicle(
            id = CAR_ID,
            plate = CAR_PLATE,
            type = VehicleType.CAR,
            brand = "Chevrolet",
            line = "Onix",
            model = "LTZ",
            year = 2022,
            currentMileage = CAR_KM,
            color = "Gris",
            vin = "9BGKS48A0PG123456",
            fuelType = FuelType.GASOLINE,
            engineCc = 1400,
            isArchived = false,
            isPrimary = true
        )
        val moto = Vehicle(
            id = MOTO_ID,
            plate = MOTO_PLATE,
            type = VehicleType.MOTORCYCLE,
            brand = "Yamaha",
            line = "FZ",
            model = "FZ 2.0",
            year = 2021,
            currentMileage = MOTO_KM,
            color = "Negro",
            vin = null,
            fuelType = FuelType.GASOLINE,
            engineCc = 149,
            isArchived = false,
            isPrimary = false
        )
        val archived = Vehicle(
            id = ARCHIVED_ID,
            plate = ARCHIVED_PLATE,
            type = VehicleType.CAR,
            brand = "Renault",
            line = "Logan",
            model = "Expression",
            year = 2014,
            currentMileage = ARCHIVED_KM,
            color = "Blanco",
            vin = null,
            fuelType = FuelType.GASOLINE,
            engineCc = 1600,
            isArchived = true,
            isPrimary = false
        )

        val readings = listOf(
            Reading(1, CAR_ID, today.minusDays(90), 42_100, "Antes del viaje"),
            Reading(2, CAR_ID, today.minusDays(40), 43_800, null),
            Reading(3, CAR_ID, today.minusDays(12), 44_760, "Después del pinchazo"),
            Reading(4, CAR_ID, today, CAR_KM, "Lectura de la ponencia"),
            Reading(5, MOTO_ID, today.minusDays(50), 12_100, null),
            Reading(6, MOTO_ID, today, MOTO_KM, null),
            Reading(7, ARCHIVED_ID, today.minusDays(400), ARCHIVED_KM, "Última lectura antes de archivarlo")
        )

        val documents = listOf(
            Document(
                id = 1,
                vehicleId = CAR_ID,
                type = DocumentType.SOAT,
                name = "SOAT",
                expirationDate = today.minusDays(12),
                issuer = "Seguros Bolívar",
                alertsEnabled = true,
                referenceNumber = "SOAT-45219"
            ),
            Document(
                id = 2,
                vehicleId = CAR_ID,
                type = DocumentType.TECHNICAL_INSPECTION,
                name = "Técnico-mecánica",
                expirationDate = today.plusDays(8),
                issuer = "CDA Autopista",
                alertsEnabled = true,
                referenceNumber = "TM-88421"
            ),
            Document(
                id = 3,
                vehicleId = CAR_ID,
                type = DocumentType.TAX,
                name = "Impuesto vehicular",
                expirationDate = today.plusDays(20),
                issuer = "Secretaría de Hacienda",
                alertsEnabled = true,
                referenceNumber = null
            ),
            Document(
                id = 4,
                vehicleId = CAR_ID,
                type = DocumentType.INSURANCE,
                name = "Seguro todo riesgo",
                expirationDate = today.plusDays(140),
                issuer = "Sura",
                alertsEnabled = true,
                referenceNumber = "POL-22910"
            ),
            Document(
                id = 5,
                vehicleId = CAR_ID,
                type = DocumentType.TRAFFIC_LIGHT,
                name = "Semáforo",
                expirationDate = today.plusDays(5),
                issuer = "Alcaldía",
                alertsEnabled = false,
                referenceNumber = null
            ),
            Document(
                id = 6,
                vehicleId = MOTO_ID,
                type = DocumentType.SOAT,
                name = "SOAT moto",
                expirationDate = today.plusDays(200),
                issuer = "Axa Colpatria",
                alertsEnabled = true,
                referenceNumber = "SOAT-M-1102"
            )
        )

        val plans = listOf(
            Plan(
                id = OIL_PLAN,
                vehicleId = CAR_ID,
                title = "Cambio de aceite",
                category = "Aceite",
                intervalMileage = 5_000,
                intervalMonths = 6,
                isActive = true,
                description = "Aceite 5W30 y filtro. Ya pasó el kilometraje del intervalo.",
                deadline = today.minusDays(20),
                limitMileage = 44_000
            ),
            Plan(
                id = BRAKE_PLAN,
                vehicleId = CAR_ID,
                title = "Revisión de frenos",
                category = "Frenos",
                intervalMileage = 20_000,
                intervalMonths = 12,
                isActive = true,
                description = "Pastillas y nivel de líquido.",
                deadline = today.plusDays(6),
                limitMileage = 52_000
            ),
            Plan(
                id = TIRE_PLAN,
                vehicleId = CAR_ID,
                title = "Rotación de llantas",
                category = "Llantas",
                intervalMileage = 10_000,
                intervalMonths = 12,
                isActive = true,
                description = "Rotar y revisar presión.",
                deadline = today.plusDays(90),
                limitMileage = 60_000
            ),
            Plan(
                id = BATTERY_PLAN,
                vehicleId = CAR_ID,
                title = "Batería",
                category = "Batería",
                intervalMileage = 0,
                intervalMonths = 24,
                isActive = false,
                description = "Actividad en pausa. Si se reanuda, queda próxima.",
                deadline = today.plusDays(3),
                limitMileage = null
            ),
            Plan(
                id = LIGHTS_PLAN,
                vehicleId = CAR_ID,
                title = "Revisión de luces",
                category = "Otros",
                intervalMileage = 0,
                intervalMonths = 0,
                isActive = true,
                description = "Sin fecha ni kilometraje, para ver el estado sin programación.",
                deadline = null,
                limitMileage = null
            ),
            Plan(
                id = CHAIN_PLAN,
                vehicleId = MOTO_ID,
                title = "Kit de arrastre",
                category = "Transmisión",
                intervalMileage = 3_000,
                intervalMonths = 6,
                isActive = true,
                description = "Cadena, piñón y plato.",
                deadline = today.plusDays(4),
                limitMileage = 15_000
            ),
            Plan(
                id = ARCHIVED_OIL_PLAN,
                vehicleId = ARCHIVED_ID,
                title = "Cambio de aceite",
                category = "Aceite",
                intervalMileage = 5_000,
                intervalMonths = 6,
                isActive = true,
                description = "Quedó pendiente al archivar el Logan.",
                deadline = today.minusDays(40),
                limitMileage = 95_000
            )
        )

        val brakeInstalled = today.minusMonths(6).plusDays(4)
        val services = listOf(
            Service(
                id = "demo-svc-oil",
                vehicleId = CAR_ID,
                planId = OIL_PLAN,
                title = "Cambio de aceite",
                category = "Aceite",
                date = today.minusMonths(8),
                mileage = 40_050,
                laborCost = 45_000.0,
                otherCosts = 0.0,
                totalCost = 168_000.0,
                workshopName = "Taller del Norte",
                serviceType = ServiceType.PREVENTIVE,
                description = "Aceite 5W30 y filtro de aceite."
            ),
            Service(
                id = "demo-svc-brakes",
                vehicleId = CAR_ID,
                planId = BRAKE_PLAN,
                title = "Cambio de pastillas",
                category = "Frenos",
                date = brakeInstalled,
                mileage = 41_800,
                laborCost = 80_000.0,
                otherCosts = 0.0,
                totalCost = 240_000.0,
                workshopName = "Frenos Andinos",
                serviceType = ServiceType.PREVENTIVE,
                description = "Pastillas delanteras. La garantía está por vencer."
            ),
            Service(
                id = "demo-svc-puncture",
                vehicleId = CAR_ID,
                planId = null,
                title = "Pinchazo llanta delantera",
                category = "Llantas",
                date = today.minusDays(18),
                mileage = 44_700,
                laborCost = 20_000.0,
                otherCosts = 0.0,
                totalCost = 35_000.0,
                workshopName = "Montallantas 68",
                serviceType = ServiceType.CORRECTIVE,
                description = "Pinchazo en la llanta delantera derecha. Se reparó con parche y quedó sellada."
            ),
            Service(
                id = "demo-svc-chain",
                vehicleId = MOTO_ID,
                planId = CHAIN_PLAN,
                title = "Ajuste de cadena",
                category = "Transmisión",
                date = today.minusDays(45),
                mileage = 12_200,
                laborCost = 25_000.0,
                otherCosts = 0.0,
                totalCost = 25_000.0,
                workshopName = "Motos del Centro",
                serviceType = ServiceType.PREVENTIVE,
                description = "Tensión y lubricación de la cadena."
            ),
            Service(
                id = "demo-svc-archived",
                vehicleId = ARCHIVED_ID,
                planId = ARCHIVED_OIL_PLAN,
                title = "Cambio de aceite",
                category = "Aceite",
                date = today.minusDays(400),
                mileage = 93_200,
                laborCost = 40_000.0,
                otherCosts = 0.0,
                totalCost = 120_000.0,
                workshopName = "Servicio Rápido",
                serviceType = ServiceType.PREVENTIVE,
                description = "Último aceite del Logan."
            )
        )

        val parts = listOf(
            Part(
                id = "demo-part-oil",
                serviceId = "demo-svc-oil",
                name = "Aceite 5W30",
                quantity = 4,
                cost = 95_000.0,
                brand = "Mobil",
                reference = "5W30-4L",
                provider = "Autopartes del Norte",
                installationDate = today.minusMonths(8),
                warranty = null,
                notes = null
            ),
            Part(
                id = "demo-part-filter",
                serviceId = "demo-svc-oil",
                name = "Filtro de aceite",
                quantity = 1,
                cost = 28_000.0,
                brand = "Mann",
                reference = "W610/6",
                provider = "Autopartes del Norte",
                installationDate = today.minusMonths(8),
                warranty = null,
                notes = null
            ),
            Part(
                id = "demo-part-pads",
                serviceId = "demo-svc-brakes",
                name = "Pastillas delanteras",
                quantity = 1,
                cost = 160_000.0,
                brand = "Bosch",
                reference = "BP-2201",
                provider = "Frenos Andinos",
                installationDate = brakeInstalled,
                warranty = "6 meses",
                notes = "Garantía por vencer en los próximos días."
            ),
            Part(
                id = "demo-part-patch",
                serviceId = "demo-svc-puncture",
                name = "Parche de llanta",
                quantity = 1,
                cost = 15_000.0,
                brand = null,
                reference = null,
                provider = "Montallantas 68",
                installationDate = today.minusDays(18),
                warranty = null,
                notes = "Llanta delantera derecha."
            )
        )

        return Snapshot(
            today = today,
            vehicles = listOf(car, moto, archived),
            readings = readings,
            documents = documents,
            plans = plans,
            services = services,
            parts = parts
        )
    }

    fun utcMillis(date: LocalDate): Long =
        date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
}
