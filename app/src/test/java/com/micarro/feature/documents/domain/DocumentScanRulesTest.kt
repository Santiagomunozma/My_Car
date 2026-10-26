package com.micarro.feature.documents.domain

import com.micarro.domain.model.DocumentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentExtractionParserTest {

    @Test
    fun `lee el json del modelo`() {
        val raw = """
            {"type":"SOAT","expirationDate":"2026-08-01","issuer":"Seguros Bolivar","referenceNumber":"123","plate":"ABC123","name":"SOAT 2026"}
        """.trimIndent()
        val parsed = DocumentExtractionParser.parse(raw)
        assertEquals(DocumentType.SOAT, parsed.type)
        assertEquals("2026-08-01", parsed.expirationDate.toString())
        assertEquals("Seguros Bolivar", parsed.issuer)
        assertEquals("123", parsed.referenceNumber)
        assertEquals("ABC123", parsed.plate)
        assertEquals("SOAT 2026", parsed.name)
    }

    @Test
    fun `ignora cercas markdown y datos invalidos`() {
        val raw = """
            ```json
            {"type":"DESCONOCIDO","expirationDate":"mañana","issuer":null,"plate":""}
            ```
        """.trimIndent()
        val parsed = DocumentExtractionParser.parse(raw)
        assertNull(parsed.type)
        assertNull(parsed.expirationDate)
        assertNull(parsed.issuer)
        assertNull(parsed.plate)
    }

    @Test
    fun `extrae el texto de la respuesta de gemini`() {
        val body = """
            {"candidates":[{"content":{"parts":[{"text":"{\"type\":\"TAX\"}"}]}}]}
        """.trimIndent()
        val text = DocumentExtractionParser.responseText(body)
        assertEquals(DocumentType.TAX, DocumentExtractionParser.parse(text!!).type)
    }
}

class PlateMatchTest {

    @Test
    fun `ignora espacios y guiones`() {
        assertTrue(PlateMatch.matches("abc-123", "ABC 123"))
    }

    @Test
    fun `placa vacia no bloquea el guardado`() {
        assertTrue(PlateMatch.matches(null, "ABC123"))
        assertTrue(PlateMatch.matches("  ", "ABC123"))
    }

    @Test
    fun `placa distinta no coincide`() {
        assertTrue(!PlateMatch.matches("XYZ999", "ABC123"))
    }
}

class DocumentReminderRulesTest {

    @Test
    fun `a 30 dias avisa el mes`() {
        val decision = DocumentReminderRules.decide(30, false, false, false)
        assertEquals(DocumentReminder.MONTH, decision.reminder)
        assertTrue(decision.remindedMonth)
    }

    @Test
    fun `a 15 dias avisa el mes`() {
        val decision = DocumentReminderRules.decide(15, false, false, false)
        assertEquals(DocumentReminder.MONTH, decision.reminder)
    }

    @Test
    fun `si el mes ya se aviso no repite`() {
        val decision = DocumentReminderRules.decide(20, true, false, false)
        assertNull(decision.reminder)
    }

    @Test
    fun `a 14 dias avisa la quincena y marca el mes`() {
        val decision = DocumentReminderRules.decide(14, false, false, false)
        assertEquals(DocumentReminder.FORTNIGHT, decision.reminder)
        assertTrue(decision.remindedMonth)
        assertTrue(decision.remindedFortnight)
    }

    @Test
    fun `a 1 dia avisa la quincena`() {
        val decision = DocumentReminderRules.decide(1, true, false, false)
        assertEquals(DocumentReminder.FORTNIGHT, decision.reminder)
    }

    @Test
    fun `si la quincena ya se aviso no repite`() {
        val decision = DocumentReminderRules.decide(10, true, true, false)
        assertNull(decision.reminder)
    }

    @Test
    fun `el dia del vencimiento avisa una vez`() {
        val decision = DocumentReminderRules.decide(0, true, true, false)
        assertEquals(DocumentReminder.EXPIRED, decision.reminder)
        assertTrue(decision.remindedExpired)
    }

    @Test
    fun `vencido ya avisado no repite`() {
        val decision = DocumentReminderRules.decide(-4, true, true, true)
        assertNull(decision.reminder)
    }

    @Test
    fun `fuera de la ventana no avisa`() {
        val decision = DocumentReminderRules.decide(31, false, false, false)
        assertNull(decision.reminder)
    }
}
