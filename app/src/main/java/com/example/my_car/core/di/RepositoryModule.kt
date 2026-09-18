package com.example.my_car.core.di

import com.example.my_car.core.settings.SharedPreferencesAlertSettingsRepository
import com.example.my_car.data.repository.FakeMaintenanceRepositoryImpl
import com.example.my_car.data.repository.FakePartRepositoryImpl
import com.example.my_car.domain.alerts.MileageAlertNotifier
import com.example.my_car.domain.alerts.NoOpMileageAlertNotifier
import com.example.my_car.domain.repository.AlertSettingsRepository
import com.example.my_car.domain.repository.DocumentRepository
import com.example.my_car.domain.repository.MaintenanceRepository
import com.example.my_car.domain.repository.MileageRepository
import com.example.my_car.domain.repository.PartRepository
import com.example.my_car.domain.repository.VehicleRepository
import com.example.my_car.feature.documents.data.DocumentRepositoryImpl
import com.example.my_car.feature.mileage.data.MileageRepositoryImpl
import com.example.my_car.feature.vehicle.data.LocalVehiclePhotoStore
import com.example.my_car.feature.vehicle.data.VehicleRepositoryImpl
import com.example.my_car.feature.vehicle.domain.VehiclePhotoStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Compañero 1: implementaciones reales sobre Room
    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindMileageRepository(impl: MileageRepositoryImpl): MileageRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindVehiclePhotoStore(impl: LocalVehiclePhotoStore): VehiclePhotoStore

    @Binds
    @Singleton
    abstract fun bindAlertSettingsRepository(
        impl: SharedPreferencesAlertSettingsRepository
    ): AlertSettingsRepository

    // Contrato para el módulo de alertas del Compañero 2; NoOp hasta que llegue la real
    @Binds
    @Singleton
    abstract fun bindMileageAlertNotifier(impl: NoOpMileageAlertNotifier): MileageAlertNotifier

    // Compañero 2/3: fakes provisionales del repo (sus implementaciones Room no están registradas aún)
    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(impl: FakeMaintenanceRepositoryImpl): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindPartRepository(impl: FakePartRepositoryImpl): PartRepository

}
