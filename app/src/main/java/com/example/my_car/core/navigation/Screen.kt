package com.example.my_car.core.navigation

// Usamos una sealed class para tener seguridad de tipos y evitar errores de tipeo en las rutas
sealed class Screen(val route: String) {

    // Nuestras rutas (Fase 2 y 5)
    object Dashboard : Screen("dashboard_screen")
    object History : Screen("history_screen")
    object Settings : Screen("settings_screen")

    // Rutas del Compañero 1 (Vehículos, Km, Documentos)
    object VehicleList : Screen("vehicle_list_screen")
    object AddVehicle : Screen("add_vehicle_screen")

    // Rutas del Compañero 2 (Mantenimiento, Repuestos, Alertas)
    object MaintenancePlan : Screen("maintenance_plan_screen")
    object AddMaintenance : Screen("add_maintenance_screen")

    // Si necesitamos pasar argumentos (ej: un ID de vehículo), podemos hacer algo así:
    // object VehicleDetail : Screen("vehicle_detail_screen/{vehicleId}") {
    //     fun createRoute(vehicleId: String) = "vehicle_detail_screen/$vehicleId"
    // }
}