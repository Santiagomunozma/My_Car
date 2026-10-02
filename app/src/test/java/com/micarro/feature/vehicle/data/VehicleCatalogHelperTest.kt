package com.micarro.feature.vehicle.data

import android.content.ContextWrapper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private object DummyContext : ContextWrapper(null)

private class TestCatalogHelper(private val json: String) : VehicleCatalogHelper(DummyContext) {
    override fun readJsonFromAssets(): String = json
}

class VehicleCatalogHelperTest {

    private val sampleJson = """
        {
          "CARRO": {
            "Renault": ["Logan", "Duster"],
            "Chevrolet": ["Tracker", "Onix"],
            "BYD": ["Dolphin"]
          },
          "MOTO": {
            "Yamaha": ["NMAX 155", "FZ 25"],
            "AKT": ["NKD 125"],
            "Honda": ["XR 150L"]
          }
        }
    """.trimIndent()

    @Test
    fun `getBrands para CARRO retorna marcas del catalogo ordenadas`() = runTest {
        val helper = TestCatalogHelper(sampleJson)
        val brands = helper.getBrands("CARRO")

        assertEquals(listOf("BYD", "Chevrolet", "Renault"), brands)
    }

    @Test
    fun `getBrands para MOTO retorna marcas del catalogo ordenadas`() = runTest {
        val helper = TestCatalogHelper(sampleJson)
        val brands = helper.getBrands("MOTO")

        assertEquals(listOf("AKT", "Honda", "Yamaha"), brands)
    }

    @Test
    fun `getLines retorna lineas asociadas a la marca ordenadas`() = runTest {
        val helper = TestCatalogHelper(sampleJson)
        val lines = helper.getLines("CARRO", "Chevrolet")

        assertEquals(listOf("Onix", "Tracker"), lines)
    }

    @Test
    fun `getLines con marca vacia retorna lista vacia`() = runTest {
        val helper = TestCatalogHelper(sampleJson)
        val lines = helper.getLines("CARRO", "")

        assertTrue(lines.isEmpty())
    }
}
