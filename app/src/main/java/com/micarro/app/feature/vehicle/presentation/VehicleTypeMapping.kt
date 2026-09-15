package com.micarro.app.feature.vehicle.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.micarro.app.R
import com.micarro.app.domain.model.FuelType
import com.micarro.app.domain.model.VehicleType

fun VehicleType.icon(): ImageVector = when (this) {
    VehicleType.CAR -> Icons.Filled.DirectionsCar
    VehicleType.TRUCK -> Icons.Filled.LocalShipping
    VehicleType.MOTORCYCLE -> Icons.Filled.TwoWheeler
}

@Composable
fun VehicleType.label(): String = when (this) {
    VehicleType.CAR -> stringResource(R.string.type_car)
    VehicleType.TRUCK -> stringResource(R.string.type_truck)
    VehicleType.MOTORCYCLE -> stringResource(R.string.type_motorcycle)
}

@Composable
fun FuelType.label(): String = when (this) {
    FuelType.GASOLINE -> stringResource(R.string.fuel_gasoline)
    FuelType.DIESEL -> stringResource(R.string.fuel_diesel)
    FuelType.ELECTRIC -> stringResource(R.string.fuel_electric)
    FuelType.HYBRID -> stringResource(R.string.fuel_hybrid)
    FuelType.GAS -> stringResource(R.string.fuel_gas)
}
