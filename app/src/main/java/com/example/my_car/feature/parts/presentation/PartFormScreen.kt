package com.example.my_car.feature.parts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.my_car.domain.model.Part
import com.example.my_car.ui.theme.*

@Composable
fun PartFormScreen(
    parts: List<Part>,
    onAddPart: (Part) -> Unit,
    onRemovePart: (Part) -> Unit
) {
    var partName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Repuestos y Materiales",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )

        // Lista de repuestos agregados
        parts.forEach { part ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BorderGray.copy(alpha = 0.3f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(part.name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${part.quantity} x $${String.format("%.2f", part.cost)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = { onRemovePart(part) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StatusError)
                }
            }
        }

        // Formulario para agregar repuesto
        Card(
            colors = CardDefaults.cardColors(containerColor = BackgroundLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = partName,
                    onValueChange = { partName = it },
                    label = { Text("Nombre del repuesto") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Cant.") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = { Text("Precio Unit.") },
                        modifier = Modifier.weight(2f)
                    )
                }
                Button(
                    onClick = {
                        if (partName.isNotBlank() && quantity.toIntOrNull() != null && cost.toDoubleOrNull() != null) {
                            onAddPart(
                                Part(
                                    serviceId = "", // Se asignará al guardar el servicio
                                    name = partName,
                                    quantity = quantity.toInt(),
                                    cost = cost.toDouble()
                                )
                            )
                            partName = ""
                            quantity = ""
                            cost = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusInfo)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir")
                }
            }
        }
    }
}
