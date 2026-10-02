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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.micarro.R
import com.micarro.domain.model.Part
import com.micarro.ui.components.DateField
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
    var brand by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var installationDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var provider by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var warranty by remember { mutableStateOf("") }
    var warrantyKm by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.parts_title),
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
                        val displayName = if (!part.brand.isNullOrBlank()) "${part.name} (${part.brand})" else part.name
                        Text(displayName, style = MaterialTheme.typography.bodyMedium)
                        val refText = if (!part.reference.isNullOrBlank()) "Ref: ${part.reference} • " else ""
                        Text(
                            "$refText${part.quantity} x $${String.format("%.2f", part.cost)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onRemovePart(part) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.part_remove), tint = StatusError)
                    }
                }
            }
        }

        MiCarroCard {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MiCarroTextField(
                    value = partName,
                    onValueChange = { partName = it },
                    label = stringResource(R.string.part_name),
                    placeholder = stringResource(R.string.part_name_hint)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiCarroTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = stringResource(R.string.part_brand),
                        placeholder = stringResource(R.string.part_brand_hint),
                        modifier = Modifier.weight(1f)
                    )
                    MiCarroTextField(
                        value = reference,
                        onValueChange = { reference = it },
                        label = stringResource(R.string.part_reference),
                        placeholder = stringResource(R.string.part_reference_hint),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiCarroTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = stringResource(R.string.part_qty),
                        placeholder = "1",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    MiCarroTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = stringResource(R.string.part_price),
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(2f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateField(
                        label = stringResource(R.string.part_install_date),
                        selectedDateMillis = installationDate,
                        onDateSelected = { installationDate = it },
                        modifier = Modifier.weight(1f)
                    )
                    MiCarroTextField(
                        value = warranty,
                        onValueChange = { warranty = it.filter { ch -> ch.isDigit() } },
                        label = stringResource(R.string.part_warranty_months),
                        placeholder = stringResource(R.string.part_warranty_hint),
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiCarroTextField(
                        value = provider,
                        onValueChange = { provider = it },
                        label = stringResource(R.string.part_provider),
                        modifier = Modifier.weight(1f)
                    )
                    MiCarroTextField(
                        value = warrantyKm,
                        onValueChange = { warrantyKm = it.filter { ch -> ch.isDigit() } },
                        label = stringResource(R.string.part_warranty_km),
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                MiCarroTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = stringResource(R.string.part_notes)
                )
                Button(
                    onClick = {
                        val unitCost = cost.toDoubleOrNull()
                        val qty = quantity.toIntOrNull()
                        if (partName.isNotBlank() && qty != null && qty >= 0 && unitCost != null && unitCost >= 0) {
                            val warrantyText = listOfNotNull(
                                warranty.trim().takeIf { it.isNotEmpty() }?.let { "$it meses" },
                                warrantyKm.trim().takeIf { it.isNotEmpty() }?.let { "$it km" }
                            ).joinToString(" / ").ifBlank { null }
                            onAddPart(
                                Part(
                                    serviceId = "",
                                    name = partName.trim(),
                                    quantity = qty,
                                    cost = unitCost,
                                    brand = brand.trim().ifEmpty { null },
                                    reference = reference.trim().ifEmpty { null },
                                    provider = provider.trim().ifEmpty { null },
                                    installationDate = installationDate,
                                    warranty = warrantyText,
                                    notes = notes.trim().ifEmpty { null }
                                )
                            )
                            partName = ""
                            brand = ""
                            reference = ""
                            provider = ""
                            notes = ""
                            quantity = ""
                            cost = ""
                            warranty = ""
                            warrantyKm = ""
                            installationDate = System.currentTimeMillis()
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
                    Text(stringResource(R.string.part_add))
                }
            }
        }
    }
}
