package com.micarro.app.core.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Vehicles : Screen("vehicles")
    data object Maintenance : Screen("maintenance")
    data object Alerts : Screen("alerts")

    data object VehicleDetail : Screen("vehicle_detail/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "vehicle_detail/$vehicleId"
    }

    data object VehicleForm : Screen("vehicle_form?vehicleId={vehicleId}") {
        fun createRoute(vehicleId: Long? = null) =
            if (vehicleId != null) "vehicle_form?vehicleId=$vehicleId" else "vehicle_form"
    }

    data object Mileage : Screen("mileage/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "mileage/$vehicleId"
    }

    data object Documents : Screen("documents/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "documents/$vehicleId"
    }
}
