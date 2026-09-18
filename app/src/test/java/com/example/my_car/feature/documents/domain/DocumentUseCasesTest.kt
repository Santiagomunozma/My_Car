package com.example.my_car.feature.documents.domain

import com.example.my_car.domain.model.AlertSettings
import com.example.my_car.domain.model.DocumentStatus
import com.example.my_car.domain.model.DocumentType
import com.example.my_car.domain.model.Vehicle
import com.example.my_car.domain.model.VehicleDocument
import com.example.my_car.domain.model.VehicleType
import com.example.my_car.feature.documents.domain.ObserveDocumentAlertsUseCase.Companion.computeAlerts
import com.example.my_car.fakes.InMemoryDocumentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DocumentStatusRulesTest {

    private val today = LocalDate.of(2025, 6, 15)

    private fun millisOf(date: LocalDate) = DocumentStatusRules.dateToMillis(date)

    @Test
    fun `vencido si la fecha ya paso`() {
        val status = DocumentStatusRules.statusFor(millisOf(today.minusDays(1)), 30, today)
        assertEquals(DocumentStatus.EXPIRED, status)
    }

    @Test
    fun `vence hoy cuenta como proximo`() {
        val status = DocumentStatusRules.statusFor(millisOf(today), 30, today)
        assertEquals(DocumentStatus.UPCOMING, status)
        assertEquals(0, DocumentStatusRules.daysUntil(millisOf(today), today))
    }

    @Test
    fun `limite exacto de anticipacion es proximo`() {
        val status = DocumentStatusRules.statusFor(millisOf(today.plusDays(30)), 30, today)
        assertEquals(DocumentStatus.UPCOMING, status)
    }

    @Test
    fun `un dia despues del limite es vigente`() {
        val status = DocumentStatusRules.statusFor(millisOf(today.plusDays(31)), 30, today)
        assertEquals(DocumentStatus.UP_TO_DATE, status)
    }

    @Test
    fun `el estado cambia al avanzar el dia sin editar datos`() {
        val expiry = millisOf(today.plusDays(31))
        assertEquals(DocumentStatus.UP_TO_DATE, DocumentStatusRules.statusFor(expiry, 30, today))
        assertEquals(
            DocumentStatus.UPCOMING,
            DocumentStatusRules.statusFor(expiry, 30, today.plusDays(1))
        )
    }
}

class DocumentUseCasesTest {

    private val today = LocalDate.of(2025, 6, 15)
    private val settings = AlertSettings(globalAlertsEnabled = true, anticipationDays = 30)

    private fun vehicle(id: String, archived: Boolean = false) = Vehicle(
        id = id, plate = "PLATE-$id", type = VehicleType.CAR, brand = "M",
        line = "L", model = "M", year = 2020, currentMileage = 0, isArchived = archived
    )

    private fun doc(vehicleId: String, daysFromToday: Long, alertsEnabled: Boolean = true) =
        VehicleDocument(
            vehicleId = vehicleId,
            type = DocumentType.SOAT,
            name = "SOAT $vehicleId",
            expirationDate = DocumentStatusRules.dateToMillis(today.plusDays(daysFromToday)),
            alertsEnabled = alertsEnabled
        )

    @Test
    fun `bandeja identifica vehiculo documento y causa`() {
        val alerts = computeAlerts(
            documents = listOf(doc("v1", -5), doc("v1", 10)),
            vehicles = listOf(vehicle("v1")),
            settings = settings,
            today = today
        )
        assertEquals(2, alerts.size)
        val expired = alerts.first()
        assertEquals("v1", expired.vehicleId)
        assertEquals("PLATE-v1", expired.vehiclePlate)
        assertEquals(DocumentStatus.EXPIRED, expired.status)
        assertEquals(-5, expired.daysUntilExpiration)
        assertEquals(DocumentStatus.UPCOMING, alerts[1].status)
        assertEquals(10, alerts[1].daysUntilExpiration)
    }

    @Test
    fun `vencidos van primero en la bandeja`() {
        val alerts = computeAlerts(
            documents = listOf(doc("v1", 3), doc("v1", -2), doc("v1", 0)),
            vehicles = listOf(vehicle("v1")),
            settings = settings,
            today = today
        )
        assertEquals(DocumentStatus.EXPIRED, alerts[0].status)
        assertEquals(0, alerts[1].daysUntilExpiration)
        assertEquals(3, alerts[2].daysUntilExpiration)
    }

    @Test
    fun `incluye documentos de vehiculos archivados`() {
        val alerts = computeAlerts(
            documents = listOf(doc("v1", 5)),
            vehicles = listOf(vehicle("v1", archived = true)),
            settings = settings,
            today = today
        )
        assertEquals(1, alerts.size)
    }

    @Test
    fun `switch global apagado vacia la bandeja`() {
        val alerts = computeAlerts(
            documents = listOf(doc("v1", 5)),
            vehicles = listOf(vehicle("v1")),
            settings = settings.copy(globalAlertsEnabled = false),
            today = today
        )
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `switch por documento apagado lo excluye`() {
        val alerts = computeAlerts(
            documents = listOf(doc("v1", 5, alertsEnabled = false), doc("v1", 10)),
            vehicles = listOf(vehicle("v1")),
            settings = settings,
            today = today
        )
        assertEquals(1, alerts.size)
        assertEquals(10, alerts[0].daysUntilExpiration)
    }

    @Test
    fun `documentos vigentes no generan alerta`() {
        val alerts = computeAlerts(
            documents = listOf(doc("v1", 60)),
            vehicles = listOf(vehicle("v1")),
            settings = settings,
            today = today
        )
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun `guardar documento valida nombre para tipo OTRO y fecha`() = runTest {
        val repo = InMemoryDocumentRepository()
        val save = SaveDocumentUseCase(repo)

        val noName = save(DocumentDraft(vehicleId = "v1", type = DocumentType.OTHER, name = "", expirationDate = 1L))
        assertTrue((noName as SaveDocumentResult.Invalid).errors.containsKey(DocumentField.NAME))

        val noDate = save(DocumentDraft(vehicleId = "v1", type = DocumentType.SOAT, expirationDate = null))
        assertTrue((noDate as SaveDocumentResult.Invalid).errors.containsKey(DocumentField.EXPIRATION_DATE))

        val ok = save(
            DocumentDraft(vehicleId = "v1", type = DocumentType.SOAT, expirationDate = 1L)
        )
        assertTrue(ok is SaveDocumentResult.Success)
        assertEquals(1, repo.observeAllDocuments().first().size)
    }

    @Test
    fun `eliminar y alternar alertas por documento`() = runTest {
        val repo = InMemoryDocumentRepository()
        val document = doc("v1", 10)
        repo.seed(document)

        ToggleDocumentAlertsUseCase(repo)(document.id, false)
        assertTrue(!repo.getDocumentById(document.id)!!.alertsEnabled)

        DeleteDocumentUseCase(repo)(document.id)
        assertTrue(repo.observeAllDocuments().first().isEmpty())
    }
}
