package com.example.my_car.core.di

import com.example.my_car.data.repository.FakeMaintenanceRepositoryImpl
import com.example.my_car.data.repository.FakeVehicleRepositoryImpl
import com.example.my_car.domain.repository.MaintenanceRepository
import com.example.my_car.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(
        impl: FakeMaintenanceRepositoryImpl
    ): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        impl: FakeVehicleRepositoryImpl
    ): VehicleRepository
}
