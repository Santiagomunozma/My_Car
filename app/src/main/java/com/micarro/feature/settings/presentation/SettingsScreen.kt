package com.micarro.feature.settings.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.micarro.R
import com.micarro.ui.components.AquaBackground
import com.micarro.ui.components.GroupedDivider
import com.micarro.ui.components.GroupedList
import com.micarro.ui.components.MiCarroListRow
import com.micarro.ui.theme.LocalThemeController
import com.micarro.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToWelcome: () -> Unit,
    onOpenBackup: () -> Unit = {},
    onOpenAlerts: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val theme = LocalThemeController.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val deletedToast = stringResource(R.string.settings_deleted_toast)

    LaunchedEffect(uiState.isDataCleared) {
        if (uiState.isDataCleared) {
            Toast.makeText(context, deletedToast, Toast.LENGTH_LONG).show()
            viewModel.onNavigationHandled()
            onNavigateToWelcome()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { paddingValues ->
        AquaBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_appearance),
                    style = MaterialTheme.typography.titleMedium
                )
                GroupedList {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_theme_help),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val themeOptions = listOf(
                            ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
                            ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
                            ThemeMode.DARK to stringResource(R.string.settings_theme_dark)
                        )
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            themeOptions.forEachIndexed { index, (mode, label) ->
                                SegmentedButton(
                                    selected = theme.mode == mode,
                                    onClick = { theme.onChange(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = themeOptions.size
                                    )
                                ) { Text(label) }
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.settings_data),
                    style = MaterialTheme.typography.titleMedium
                )
                GroupedList {
                    MiCarroListRow(
                        title = stringResource(R.string.settings_backup),
                        icon = Icons.Default.Backup,
                        showChevron = true,
                        onClick = onOpenBackup
                    )
                    GroupedDivider()
                    MiCarroListRow(
                        title = stringResource(R.string.maintenance_alerts_title),
                        icon = Icons.Default.Notifications,
                        showChevron = true,
                        onClick = onOpenAlerts
                    )
                }

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                GroupedList {
                    MiCarroListRow(
                        title = stringResource(R.string.settings_delete),
                        subtitle = stringResource(R.string.settings_delete_help),
                        icon = Icons.Default.Delete,
                        destructive = true,
                        onClick = { if (!uiState.isDeleting) showDeleteDialog = true }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.settings_delete_title)) },
            text = { Text(stringResource(R.string.settings_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.clearAllData()
                    }
                ) {
                    Text(
                        stringResource(R.string.settings_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
