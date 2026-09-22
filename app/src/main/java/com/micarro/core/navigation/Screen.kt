package com.micarro.core.navigation

sealed class Screen(val route: String) {
    // Rutas del Orquestador (Main)
    object Dashboard : Screen("dashboard_screen")
    object History : Screen("history_screen")
    object Settings : Screen("settings_screen")

    // Rutas del Compañero 1 (Vehículos, Kilometraje y Documentos)
    object VehicleList : Screen("vehicle_list_screen")
    object AddVehicle : Screen("add_vehicle_screen?vehicleId={vehicleId}") {
        fun createRoute(vehicleId: String? = null) =
            if (vehicleId != null) "add_vehicle_screen?vehicleId=$vehicleId"
            else "add_vehicle_screen"
    }

    object Mileage : Screen("mileage_screen/{vehicleId}") {
        fun createRoute(vehicleId: String) = "mileage_screen/$vehicleId"
    }

    object Documents : Screen("documents_screen/{vehicleId}") {
        fun createRoute(vehicleId: String) = "documents_screen/$vehicleId"
    }

    object DocumentsAlerts : Screen("documents/alerts")

    // Rutas del Compañero 2 (Mantenimiento)
    object MaintenancePlan : Screen("maintenance_plan_screen")
    object MaintenanceForm : Screen("maintenance_form_screen/{vehicleId}") {
        fun createRoute(vehicleId: String) = "maintenance_form_screen/$vehicleId"
    }

    object ServiceForm : Screen(
        "service_form_screen/{vehicleId}?planId={planId}&lastMileage={lastMileage}"
    ) {
        fun createRoute(vehicleId: String, planId: String?, lastMileage: Int) =
            "service_form_screen/$vehicleId?planId=${planId ?: ""}&lastMileage=$lastMileage"
    }
}
