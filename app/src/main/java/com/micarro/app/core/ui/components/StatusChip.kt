package com.micarro.app.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.micarro.app.R
import com.micarro.app.core.ui.theme.StateError
import com.micarro.app.core.ui.theme.StateSuccess
import com.micarro.app.core.ui.theme.StateWarning
import com.micarro.app.core.ui.theme.TextSecondary
import com.micarro.app.domain.model.DocumentStatus
import com.micarro.app.domain.model.MaintenanceStatus

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MaintenanceStatusChip(status: MaintenanceStatus, modifier: Modifier = Modifier) {
    val (text, color) = when (status) {
        MaintenanceStatus.UP_TO_DATE -> stringResource(R.string.status_up_to_date) to StateSuccess
        MaintenanceStatus.UPCOMING -> stringResource(R.string.status_upcoming) to StateWarning
        MaintenanceStatus.OVERDUE -> stringResource(R.string.status_overdue) to StateError
        MaintenanceStatus.UNSCHEDULED -> stringResource(R.string.status_unscheduled) to TextSecondary
    }
    StatusChip(text = text, color = color, modifier = modifier)
}

@Composable
fun DocumentStatusChip(status: DocumentStatus, modifier: Modifier = Modifier) {
    val (text, color) = when (status) {
        DocumentStatus.VALID -> stringResource(R.string.doc_status_valid) to StateSuccess
        DocumentStatus.EXPIRING_SOON -> stringResource(R.string.doc_status_expiring) to StateWarning
        DocumentStatus.EXPIRED -> stringResource(R.string.doc_status_expired) to StateError
    }
    StatusChip(text = text, color = color, modifier = modifier)
}
