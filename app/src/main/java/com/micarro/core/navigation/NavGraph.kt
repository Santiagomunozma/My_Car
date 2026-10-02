package com.micarro.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.micarro.feature.dashboard.presentation.DashboardScreen
import com.micarro.feature.documents.presentation.DocumentScreen
import com.micarro.feature.documents.presentation.DocumentsAlertsScreen
import com.micarro.feature.history.presentation.HistoryScreen
import com.micarro.feature.maintenance.presentation.MaintenanceFormScreen
import com.micarro.feature.maintenance.presentation.MaintenancePlanScreen
import com.micarro.feature.maintenance.presentation.MaintenanceViewModel
import com.micarro.feature.maintenance.presentation.ServiceFormRoute
import com.micarro.feature.mileage.presentation.MileageScreen
import com.micarro.feature.settings.presentation.SettingsScreen
import com.micarro.feature.vehicle.presentation.VehicleFormScreen
import com.micarro.feature.vehicle.presentation.VehicleListScreen

@Composable
fun MiCarroNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onAddVehicle = { navController.navigate(Screen.AddVehicle.createRoute()) },
                onOpenAlerts = { navController.navigate(Screen.DocumentsAlerts.route) }
            )
        }

        // Compañero 1: Vehículos, Kilometraje y Documentos
        composable(route = Screen.VehicleList.route) {
            VehicleListScreen(
                onAddVehicle = { navController.navigate(Screen.AddVehicle.createRoute()) },
                onEditVehicle = { id ->
                    navController.navigate(Screen.AddVehicle.createRoute(id))
                },
                onOpenMileage = { id ->
                    navController.navigate(Screen.Mileage.createRoute(id))
                },
                onOpenDocuments = { id ->
                    navController.navigate(Screen.Documents.createRoute(id))
                },
                onOpenAlerts = { navController.navigate(Screen.DocumentsAlerts.route) }
            )
        }

        composable(
            route = Screen.AddVehicle.route,
            arguments = listOf(
                navArgument("vehicleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            VehicleFormScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Mileage.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) {
            MileageScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Documents.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) {
            DocumentScreen(onBack = { navController.popBackStack() })
        }

        composable(route = Screen.DocumentsAlerts.route) {
            DocumentsAlertsScreen(
                onBack = { navController.popBackStack() },
                onOpenVehicleDocuments = { id ->
                    navController.navigate(Screen.Documents.createRoute(id))
                }
            )
        }

        // Compañero 2: Mantenimiento
        composable(route = Screen.MaintenancePlan.route) {
            val viewModel: MaintenanceViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            MaintenancePlanScreen(
                viewModel = viewModel,
                onAddPlan = {
                    state.selectedVehicle?.id?.let { vehicleId ->
                        navController.navigate(Screen.MaintenanceForm.createRoute(vehicleId.toString()))
                    }
                },
                onRegisterService = { planId ->
                    state.selectedVehicle?.let { vehicle ->
                        navController.navigate(
                            Screen.ServiceForm.createRoute(
                                vehicleId = vehicle.id.toString(),
                                planId = planId,
                                lastMileage = vehicle.currentMileage.toInt()
                            )
                        )
                    }
                },
                onEditPlan = { planId ->
                    state.selectedVehicle?.let { vehicle ->
                        navController.navigate(
                            Screen.MaintenanceForm.createRoute(vehicle.id.toString(), planId)
                        )
                    }
                },
                onOpenAlertSettings = {
                    navController.navigate(Screen.AlertSettings.route)
                }
            )
        }

        composable(route = Screen.AlertSettings.route) {
            val viewModel: MaintenanceViewModel = hiltViewModel()
            com.micarro.feature.alerts.presentation.AlertSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MaintenanceForm.route,
            arguments = listOf(
                navArgument("vehicleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("planId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val viewModel: MaintenanceViewModel = hiltViewModel()
            val vehicleId = backStackEntry.arguments?.getString("vehicleId")
            val planId = backStackEntry.arguments?.getString("planId")?.ifBlank { null }

            MaintenanceFormScreen(
                viewModel = viewModel,
                initialVehicleId = vehicleId,
                planId = planId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ServiceForm.route,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("planId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("lastMileage") {
                    type = NavType.StringType
                    defaultValue = "0"
                },
                navArgument("serviceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val viewModel: MaintenanceViewModel = hiltViewModel()
            ServiceFormRoute(
                vehicleId = checkNotNull(backStackEntry.arguments?.getString("vehicleId")),
                planId = backStackEntry.arguments?.getString("planId")?.ifBlank { null },
                serviceId = backStackEntry.arguments?.getString("serviceId")?.ifBlank { null },
                lastMileage = backStackEntry.arguments?.getString("lastMileage")?.toIntOrNull() ?: 0,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Orquestador
        composable(route = Screen.History.route) {
            HistoryScreen(
                onEditService = { item ->
                    navController.navigate(
                        Screen.ServiceForm.createRoute(
                            vehicleId = item.vehicleId.ifBlank { "0" },
                            planId = null,
                            lastMileage = item.mileage,
                            serviceId = item.id
                        )
                    )
                }
            )
        }

        // RF-40: SettingsScreen con reinicio de backstack tras borrado total de datos
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenBackup = { navController.navigate(Screen.Backup.route) }
            )
        }

        composable(route = Screen.Backup.route) {
            com.micarro.feature.backup.presentation.BackupScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}