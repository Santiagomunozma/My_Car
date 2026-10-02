package com.micarro

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.micarro.domain.model.MaintenanceService
import com.micarro.domain.model.Part
import com.micarro.domain.model.ServiceType
import com.micarro.feature.maintenance.presentation.ServiceFormScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Recorre el alta de un servicio asociado a un vehículo ya elegido.
 * El formulario de vehículo vive en otra pantalla con Hilt; aquí se fija el vehículo
 * y se confirma que el mantenimiento sale con tipo, categoría y costo calculado.
 */
@RunWith(AndroidJUnit4::class)
class RegisterVehicleAndMaintenanceTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun registrarServicioDeUnVehiculoGuardaTipoCategoriaYCosto() {
        var saved: MaintenanceService? = null
        var savedParts: List<Part> = emptyList()

        composeRule.setContent {
            ServiceFormScreen(
                vehicleId = "42",
                planId = "plan-aceite",
                lastMileage = 10000,
                initialCategory = "Aceite",
                onSave = { service, parts, _, _ ->
                    saved = service
                    savedParts = parts
                },
                onBack = {}
            )
        }

        composeRule.onNodeWithText("Descripción del servicio").performTextInput("Cambio de aceite")
        composeRule.onNodeWithText("Confirmar registro").assertIsEnabled()
        composeRule.onNodeWithText("Confirmar registro").performClick()

        composeRule.waitForIdle()
        val service = checkNotNull(saved)
        assertEquals("42", service.vehicleId)
        assertEquals("Aceite", service.category)
        assertEquals(ServiceType.PREVENTIVE, service.serviceType)
        assertEquals("Cambio de aceite", service.title)
        assertTrue(savedParts.isEmpty())
    }
}
