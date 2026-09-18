package com.example.my_car.core.navigation

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
import com.example.my_car.feature.dashboard.presentation.DashboardScreen
import com.example.my_car.feature.documents.presentation.DocumentScreen
import com.example.my_car.feature.documents.presentation.DocumentsAlertsScreen
import com.example.my_car.feature.history.presentation.HistoryScreen
import com.example.my_car.feature.maintenance.presentation.MaintenanceFormScreen
import com.example.my_car.feature.maintenance.presentation.MaintenancePlanScreen
import com.example.my_car.feature.maintenance.presentation.MaintenanceViewModel
import com.example.my_car.feature.maintenance.presentation.ServiceFormScreen
import com.example.my_car.feature.mileage.presentation.MileageScreen
import com.example.my_car.feature.settings.presentation.SettingsScreen
import com.example.my_car.feature.vehicle.presentation.VehicleFormScreen
import com.example.my_car.feature.vehicle.presentation.VehicleListScreen

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
                    state.selectedVehicle?.let { vehicle ->
                        navController.navigate(Screen.MaintenanceForm.createRoute(vehicle.id))
                    }
                },
                onRegisterService = { planId ->
                    state.selectedVehicle?.let { vehicle ->
                        navController.navigate(
                            Screen.ServiceForm.createRoute(
                                vehicleId = vehicle.id,
                                planId = planId,
                                lastMileage = vehicle.currentMileage
                            )
                        )
                    }
                }
            )
        }

        composable(
            route = Screen.MaintenanceForm.route,
            arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel: MaintenanceViewModel = hiltViewModel()
            MaintenanceFormScreen(
                vehicleId = checkNotNull(backStackEntry.arguments?.getString("vehicleId")),
                onSave = { plan ->
                    viewModel.savePlan(plan)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
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
                }
            )
        ) { backStackEntry ->
            val viewModel: MaintenanceViewModel = hiltViewModel()
            ServiceFormScreen(
                vehicleId = checkNotNull(backStackEntry.arguments?.getString("vehicleId")),
                planId = backStackEntry.arguments?.getString("planId")?.ifEmpty { null },
                lastMileage = backStackEntry.arguments?.getString("lastMileage")
                    ?.toIntOrNull() ?: 0,
                onSave = { service, parts ->
                    viewModel.registerService(service, parts)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Orquestador
        composable(route = Screen.History.route) { HistoryScreen() }
        composable(route = Screen.Settings.route) { SettingsScreen() }
    }
}
