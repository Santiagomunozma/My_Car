package com.micarro.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Forma Aqua: radios amplios y suaves.
 * Controles 16 dp, tarjetas 20-24 dp, hojas y diálogos 28 dp.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Radio de tarjeta principal Aqua. */
val AquaCardShape = RoundedCornerShape(24.dp)

/** Radio de hoja inferior Aqua (solo esquinas superiores). */
val AquaSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
