package com.micarro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.micarro.app.core.navigation.Screen
import com.micarro.app.core.ui.components.ModulePlaceholder
import com.micarro.app.core.ui.theme.MiCarroTheme
import com.micarro.app.feature.dashboard.presentation.DashboardScreen
import com.micarro.app.feature.documents.presentation.DocumentScreen
import com.micarro.app.feature.mileage.presentation.MileageScreen
import com.micarro.app.feature.vehicle.presentation.VehicleDetailScreen
import com.micarro.app.feature.vehicle.presentation.VehicleFormScreen
import com.micarro.app.feature.vehicle.presentation.VehicleListScreen
import dagger.hilt.android.AndroidEntryPoint

private data class TopLevelDestination(
    val screen: Screen,
    val labelRes: Int,
    val icon: ImageVector
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val topLevelDestinations = listOf(
        TopLevelDestination(Screen.Dashboard, R.string.nav_dashboard, Icons.Filled.Home),
        TopLevelDestination(Screen.Vehicles, R.string.nav_vehicles, Icons.Filled.DirectionsCar),
        TopLevelDestination(Screen.Maintenance, R.string.nav_maintenance, Icons.Filled.Build),
        TopLevelDestination(Screen.Alerts, R.string.nav_alerts, Icons.Filled.Notifications)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiCarroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MiCarroNavHost()
                }
            }
        }
    }

    @Composable
    private fun MiCarroNavHost() {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val topLevelRoutes = topLevelDestinations.map { it.screen.route }.toSet()

        Scaffold(
            bottomBar = {
                if (currentRoute in topLevelRoutes) {
                    NavigationBar {
                        topLevelDestinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.screen.route,
                                onClick = { navigateToTopLevel(navController, destination.screen) },
                                icon = {
                                    Icon(
                                        destination.icon,
                                        contentDescription = stringResource(destination.labelRes)
                                    )
                                },
                                label = { Text(stringResource(destination.labelRes)) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onVehicleClick = { id ->
                            navController.navigate(Screen.VehicleDetail.createRoute(id))
                        },
                        onAddVehicle = {
                            navController.navigate(Screen.VehicleForm.createRoute())
                        }
                    )
                }
                composable(Screen.Vehicles.route) {
                    VehicleListScreen(
                        onVehicleClick = { id ->
                            navController.navigate(Screen.VehicleDetail.createRoute(id))
                        },
                        onAddVehicle = {
                            navController.navigate(Screen.VehicleForm.createRoute())
                        },
                        onEditVehicle = { id ->
                            navController.navigate(Screen.VehicleForm.createRoute(id))
                        }
                    )
                }
                composable(Screen.Maintenance.route) {
                    ModulePlaceholder(
                        title = stringResource(R.string.nav_maintenance),
                        icon = Icons.Filled.Build
                    )
                }
                composable(Screen.Alerts.route) {
                    ModulePlaceholder(
                        title = stringResource(R.string.nav_alerts),
                        icon = Icons.Filled.Notifications
                    )
                }
                composable(
                    route = Screen.VehicleDetail.route,
                    arguments = listOf(
                        navArgument("vehicleId") { type = NavType.LongType }
                    )
                ) {
                    VehicleDetailScreen(
                        onBack = { navController.popBackStack() },
                        onEdit = { id ->
                            navController.navigate(Screen.VehicleForm.createRoute(id))
                        },
                        onMileage = { id ->
                            navController.navigate(Screen.Mileage.createRoute(id))
                        },
                        onDocuments = { id ->
                            navController.navigate(Screen.Documents.createRoute(id))
                        }
                    )
                }
                composable(
                    route = Screen.VehicleForm.route,
                    arguments = listOf(
                        navArgument("vehicleId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) {
                    VehicleFormScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route = Screen.Mileage.route,
                    arguments = listOf(
                        navArgument("vehicleId") { type = NavType.LongType }
                    )
                ) {
                    MileageScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route = Screen.Documents.route,
                    arguments = listOf(
                        navArgument("vehicleId") { type = NavType.LongType }
                    )
                ) {
                    DocumentScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }

    private fun navigateToTopLevel(navController: NavHostController, screen: Screen) {
        navController.navigate(screen.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
