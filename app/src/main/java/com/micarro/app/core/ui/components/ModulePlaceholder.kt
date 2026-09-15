package com.micarro.app.core.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.micarro.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulePlaceholder(title: String, icon: ImageVector) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }
    ) { padding ->
        EmptyState(
            icon = icon,
            title = title,
            message = stringResource(R.string.module_in_progress),
            modifier = Modifier.padding(padding)
        )
    }
}
