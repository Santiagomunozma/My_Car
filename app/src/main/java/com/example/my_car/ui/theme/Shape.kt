package com.example.my_car.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),  // Campos de texto y botones[cite: 3]
    medium = RoundedCornerShape(16.dp), // Cards de vehículos y servicios[cite: 3]
    large = RoundedCornerShape(20.dp)   // Chips de estado y diálogos[cite: 3]
)