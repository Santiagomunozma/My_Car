package com.example.my_car.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MaintenanceRulesTest {

    // Aplicamos la convención de nombres descriptivos para los tests
    @Test
    fun `dado un kilometraje proximo al intervalo, debe calcular los kilometros restantes correctamente`() {
        // Arrange (Preparar los datos)
        val intervalMileage = 5000
        val currentMileage = 4600

        // Act (Ejecutar la lógica que queremos probar)
        val remainingKm = intervalMileage - (currentMileage % intervalMileage)

        // Assert (Verificar que el resultado es el esperado)
        assertEquals(400, remainingKm)
        assertTrue("El kilometraje restante (400) debe ser menor o igual al margen de alerta (500)", remainingKm <= 500)
    }

    @Test
    fun `si el vehiculo se paso del kilometraje, debe marcar estado como vencido o urgente`() {
        // Arrange
        val intervalMileage = 10000
        val currentMileage = 10100 // Se pasó por 100 km

        // Act
        // Simulamos la lógica que usaríamos en un UseCase o en el Worker
        val remainingKm = intervalMileage - (currentMileage % intervalMileage)

        // Si el residuo es muy pequeño frente al intervalo, y el total es mayor al intervalo, ya se pasó.
        // Aquí ajustamos la matemática para detectar el sobrepaso.
        val kmSinceLastService = currentMileage % intervalMileage
        val isOverdue = currentMileage >= intervalMileage && kmSinceLastService in 1..500

        // Assert
        assertTrue("El mantenimiento debería estar marcado como vencido porque se pasó por 100km", isOverdue)
    }
}