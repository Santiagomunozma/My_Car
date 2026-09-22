package com.micarro.feature.parts.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.micarro.domain.model.Part
import com.micarro.ui.components.MiCarroCard
import com.micarro.ui.components.MiCarroTextField
import com.micarro.ui.theme.StatusError

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
            style = MaterialTheme.typography.titleMedium
        )

        parts.forEach { part ->
            MiCarroCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(part.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${part.quantity} x $${String.format("%.2f", part.cost)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onRemovePart(part) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StatusError)
                    }
                }
            }
        }

        MiCarroCard {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MiCarroTextField(
                    value = partName,
                    onValueChange = { partName = it },
                    label = "Nombre del repuesto",
                    placeholder = "Filtro de aceite"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiCarroTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = "Cant.",
                        placeholder = "1",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    MiCarroTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = "Precio Unit.",
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(2f)
                    )
                }
                Button(
                    onClick = {
                        if (partName.isNotBlank() && quantity.toIntOrNull() != null && cost.toDoubleOrNull() != null) {
                            onAddPart(
                                Part(
                                    serviceId = "",
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
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir")
                }
            }
        }
    }
}
