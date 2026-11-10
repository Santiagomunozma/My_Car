package com.micarro.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.micarro.ui.theme.LocalStatusColors

enum class StatusTone {
    Success,
    Warning,
    Error,
    Info,
    Neutral
}

@Composable
fun StatusChip(
    text: String,
    tone: StatusTone,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = LocalStatusColors.current
    val container = when (tone) {
        StatusTone.Success -> colors.successContainer
        StatusTone.Warning -> colors.warningContainer
        StatusTone.Error -> colors.errorContainer
        StatusTone.Info -> colors.infoContainer
        StatusTone.Neutral -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when (tone) {
        StatusTone.Success -> colors.onSuccessContainer
        StatusTone.Warning -> colors.onWarningContainer
        StatusTone.Error -> colors.onErrorContainer
        StatusTone.Info -> colors.onInfoContainer
        StatusTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier.heightIn(min = 28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = content,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun statusAccent(tone: StatusTone): Color {
    val colors = LocalStatusColors.current
    return when (tone) {
        StatusTone.Success -> colors.success
        StatusTone.Warning -> colors.warning
        StatusTone.Error -> colors.error
        StatusTone.Info -> colors.info
        StatusTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun statusContainer(tone: StatusTone): Color {
    val colors = LocalStatusColors.current
    return when (tone) {
        StatusTone.Success -> colors.successContainer
        StatusTone.Warning -> colors.warningContainer
        StatusTone.Error -> colors.errorContainer
        StatusTone.Info -> colors.infoContainer
        StatusTone.Neutral -> MaterialTheme.colorScheme.surfaceVariant
    }
}
