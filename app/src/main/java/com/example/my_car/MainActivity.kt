package com.example.my_car

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.my_car.core.worker.AlertScheduler
import com.example.my_car.ui.theme.MyCarTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var alertScheduler: AlertScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestDevPanel(
                        onTestAlert = {
                            alertScheduler.scheduleAlert(
                                delayInMinutes = 1,
                                title = "My Car - Mantenimiento",
                                message = "¡WorkManager está funcionando al pelo!"
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TestDevPanel(onTestAlert: () -> Unit) {
    val context = LocalContext.current
    var alertScheduled by remember { mutableStateOf(false) }

    // Launcher para pedir el permiso de notificaciones en Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onTestAlert()
            alertScheduled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "My Car - Core Dev Panel",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Infraestructura base lista: Room, WorkManager, Theme y Tests.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Si es Android 13 o superior, verificamos permisos
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        onTestAlert()
                        alertScheduled = true
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    // Si es Android 12 o menor, no se necesita pedir permiso en tiempo de ejecución
                    onTestAlert()
                    alertScheduled = true
                }
            }
        ) {
            Text("Probar Alerta WorkManager (1 min)")
        }

        if (alertScheduled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¡Alerta programada! Bloquea tu pantalla o minimiza la app y espera.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nota: WorkManager gestiona la batería, puede tardar entre 1 y 2 minutos reales.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}