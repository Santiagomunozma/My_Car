package com.micarro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.micarro.R
import com.micarro.ui.theme.AquaCardShape
import com.micarro.ui.theme.AquaSheetShape
import com.micarro.ui.theme.LocalDarkTheme
import com.micarro.ui.theme.MyCarTheme
import com.micarro.ui.theme.ThemeMode

/** Fondo con lavado Aqua para pantallas principales. */
@Composable
fun AquaBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.background(com.micarro.ui.theme.rememberAquaWash())
    ) { content() }
}

/** Tarjeta Aqua: sin borde en claro, sombra suave, radio 24 dp. */
@Composable
fun AquaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = LocalDarkTheme.current
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = if (dark) 0.dp else 4.dp)
    val border = if (dark) androidx.compose.foundation.BorderStroke(
        1.dp, MaterialTheme.colorScheme.outlineVariant
    ) else null
    if (onClick != null) {
        Card(
            onClick = onClick, modifier = modifier.fillMaxWidth(), shape = AquaCardShape,
            colors = colors, elevation = elevation, border = border, content = content
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(), shape = AquaCardShape,
            colors = colors, elevation = elevation, border = border, content = content
        )
    }
}

/** Botón con imagen/icono grande y etiqueta. Reemplaza filas de botones de texto. */
@Composable
fun ActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2
        )
    }
}

/** Acción de menú overflow u hoja. */
data class MenuAction(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val destructive: Boolean = false,
    val enabled: Boolean = true
)

/** Botón ⋮ con menú desplegable reutilizable. */
@Composable
fun OverflowMenu(
    actions: List<MenuAction>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = contentDescription ?: stringResource(R.string.action_more)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = action.label,
                            color = if (action.destructive) statusAccent(StatusTone.Error)
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { expanded = false; action.onClick() },
                    enabled = action.enabled,
                    leadingIcon = action.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon, contentDescription = null,
                                tint = if (action.destructive) statusAccent(StatusTone.Error)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }
    }
}

/** Hoja inferior Aqua: handle, título opcional y contenido en columna. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiCarroSheet(
    onDismiss: () -> Unit,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = AquaSheetShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            content()
        }
    }
}

/** Fila de lista agrupada tipo Apple: icono, título, valor y chevron opcional. */
@Composable
fun MiCarroListRow(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    value: String? = null,
    destructive: Boolean = false,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val titleColor = if (destructive) statusAccent(StatusTone.Error) else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (destructive) statusAccent(StatusTone.Error).copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, contentDescription = null,
                    tint = if (destructive) statusAccent(StatusTone.Error) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!value.isNullOrBlank()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Contenedor de lista agrupada: una superficie redondeada con separadores finos. */
@Composable
fun GroupedList(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    AquaCard(modifier = modifier) {
        Column { content() }
    }
}

/** Separador fino dentro de una lista agrupada. */
@Composable
fun GroupedDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Preview(name = "Aqua claro", showBackground = true)
@Composable
private fun AquaComponentsPreview() {
    MyCarTheme(themeMode = ThemeMode.LIGHT) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionTile(
                    icon = Icons.Default.MoreVert, label = "Servicio",
                    onClick = {}, modifier = Modifier.weight(1f)
                )
                ActionTile(
                    icon = Icons.Default.MoreVert, label = "Kilometraje",
                    onClick = {}, modifier = Modifier.weight(1f)
                )
            }
            GroupedList {
                MiCarroListRow(title = "Ajustes", showChevron = true, onClick = {})
                GroupedDivider()
                MiCarroListRow(title = "Borrar datos", destructive = true, onClick = {})
            }
        }
    }
}

@Preview(name = "Aqua oscuro", showBackground = true)
@Composable
private fun AquaComponentsDarkPreview() {
    MyCarTheme(themeMode = ThemeMode.DARK) {
        GroupedList(modifier = Modifier.padding(16.dp)) {
            MiCarroListRow(title = "Respaldo", value = "Manual", showChevron = true, onClick = {})
        }
    }
}
