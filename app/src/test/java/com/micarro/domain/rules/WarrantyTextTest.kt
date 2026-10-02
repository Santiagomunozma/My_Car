package com.micarro.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WarrantyTextTest {
    @Test
    fun `lee meses y kilometros del texto de garantia`() {
        val raw = "6 meses / 10000 km"
        assertEquals(6L, WarrantyText.months(raw))
        assertEquals(10000, WarrantyText.kilometers(raw))
    }

    @Test
    fun `un numero solo se interpreta como meses`() {
        assertEquals(12L, WarrantyText.months("12"))
        assertNull(WarrantyText.kilometers("12"))
    }
}
