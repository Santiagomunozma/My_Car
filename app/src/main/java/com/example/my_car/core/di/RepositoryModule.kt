package com.example.my_car.core.di

import com.example.my_car.data.repository.*
import com.example.my_car.domain.repository.*
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

    @Binds
    @Singleton
    abstract fun bindMileageRepository(
        impl: FakeMileageRepositoryImpl
    ): MileageRepository

    @Binds
    @Singleton
    abstract fun bindPartRepository(
        impl: FakePartRepositoryImpl
    ): PartRepository
}
