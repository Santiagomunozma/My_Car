package com.example.my_car.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.my_car.feature.history.presentation.HistoryScreen

@Composable
fun MiCarroNavGraph(navController: NavHostController) {
    // Definimos el Dashboard como la pantalla inicial (startDestination)
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {

        // --- SECCIÓN NUESTRA (ORQUESTADOR) ---
        composable(route = Screen.Dashboard.route) {
            // Placeholder: Aquí irá nuestro DashboardScreen real
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Dashboard (En construcción)")
            }
        }

        composable(route = Screen.History.route) {
            HistoryScreen()
        }

        // --- SECCIÓN COMPAÑERO 1 ---
        composable(route = Screen.VehicleList.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Lista de Vehículos")
            }
        }

        // --- SECCIÓN COMPAÑERO 2 ---
        composable(route = Screen.MaintenancePlan.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Plan de Mantenimiento")
            }
        }
    }
}