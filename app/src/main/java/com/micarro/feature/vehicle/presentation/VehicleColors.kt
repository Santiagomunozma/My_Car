package com.micarro.feature.vehicle.presentation

import androidx.compose.ui.graphics.Color

data class VehicleColorOption(
    val name: String,
    val color: Color
)

object VehicleColors {
    val options = listOf(
        VehicleColorOption("Blanco", Color(0xFFFFFFFF)),
        VehicleColorOption("Negro", Color(0xFF1C1C1C)),
        VehicleColorOption("Gris / Plata", Color(0xFFA0A0A0)),
        VehicleColorOption("Rojo", Color(0xFFE53935)),
        VehicleColorOption("Azul", Color(0xFF1E88E5)),
        VehicleColorOption("Verde", Color(0xFF4CAF50)),
        VehicleColorOption("Amarillo", Color(0xFFFDD835))
    )

    fun findByName(name: String): VehicleColorOption? =
        options.firstOrNull { it.name.equals(name, ignoreCase = true) }
}
