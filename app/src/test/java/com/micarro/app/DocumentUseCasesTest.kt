package com.micarro.app

import com.micarro.app.domain.model.DocumentStatus
import com.micarro.app.domain.model.DocumentType
import com.micarro.app.domain.model.VehicleDocument
import com.micarro.app.feature.documents.domain.DocumentError
import com.micarro.app.feature.documents.domain.DocumentField
import com.micarro.app.feature.documents.domain.DocumentStatusUseCase
import com.micarro.app.feature.documents.domain.SaveDocumentResult
import com.micarro.app.feature.documents.domain.SaveDocumentUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class DocumentUseCasesTest {

    private val status = DocumentStatusUseCase()
    private val today = LocalDate.of(2026, 9, 15)

    private fun epochOf(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test
    fun `expired document reports EXPIRED`() {
        val result = status(epochOf(today.minusDays(1)), today = today)
        assertEquals(DocumentStatus.EXPIRED, result)
    }

    @Test
    fun `document expiring within anticipation window reports EXPIRING_SOON`() {
        assertEquals(
            DocumentStatus.EXPIRING_SOON,
            status(epochOf(today), today = today)
        )
        assertEquals(
            DocumentStatus.EXPIRING_SOON,
            status(epochOf(today.plusDays(30)), today = today)
        )
    }

    @Test
    fun `document beyond anticipation window reports VALID`() {
        assertEquals(
            DocumentStatus.VALID,
            status(epochOf(today.plusDays(31)), today = today)
        )
    }

    @Test
    fun `custom anticipation window is respected`() {
        assertEquals(
            DocumentStatus.EXPIRING_SOON,
            status(epochOf(today.plusDays(10)), anticipationDays = 15, today = today)
        )
        assertEquals(
            DocumentStatus.VALID,
            status(epochOf(today.plusDays(10)), anticipationDays = 5, today = today)
        )
    }

    @Test
    fun `OTHER type requires a name`() = runBlocking {
        val repo = FakeDocumentRepository()
        val save = SaveDocumentUseCase(repo)
        val doc = VehicleDocument(
            vehicleId = 1L,
            type = DocumentType.OTHER,
            name = " ",
            expiryDateEpochMs = epochOf(today.plusDays(90))
        )
        val result = save(doc)
        assertTrue(result is SaveDocumentResult.Invalid)
        assertTrue(
            (result as SaveDocumentResult.Invalid).errors
                .any { it.field == DocumentField.NAME && it.error == DocumentError.REQUIRED }
        )
    }

    @Test
    fun `invalid expiry date is rejected`() = runBlocking {
        val repo = FakeDocumentRepository()
        val save = SaveDocumentUseCase(repo)
        val doc = VehicleDocument(
            vehicleId = 1L,
            type = DocumentType.SOAT,
            expiryDateEpochMs = 0L
        )
        val result = save(doc)
        assertTrue(result is SaveDocumentResult.Invalid)
        assertTrue(
            (result as SaveDocumentResult.Invalid).errors
                .any { it.field == DocumentField.EXPIRY_DATE }
        )
    }

    @Test
    fun `valid document is saved`() = runBlocking {
        val repo = FakeDocumentRepository()
        val save = SaveDocumentUseCase(repo)
        val doc = VehicleDocument(
            vehicleId = 1L,
            type = DocumentType.SOAT,
            expiryDateEpochMs = epochOf(today.plusDays(90))
        )
        val result = save(doc)
        assertTrue(result is SaveDocumentResult.Success)
        assertEquals(1, repo.documents.value.size)
    }
}
