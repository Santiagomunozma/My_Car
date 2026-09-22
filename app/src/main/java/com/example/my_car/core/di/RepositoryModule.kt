package com.example.my_car.core.di

import com.example.my_car.core.settings.SharedPreferencesAlertSettingsRepository
import com.example.my_car.domain.alerts.MileageAlertNotifier
import com.example.my_car.domain.alerts.NoOpMileageAlertNotifier
import com.example.my_car.domain.repository.AlertSettingsRepository
import com.example.my_car.domain.repository.DocumentRepository
import com.example.my_car.domain.repository.MaintenanceRepository
import com.example.my_car.domain.repository.MileageRepository
import com.example.my_car.domain.repository.PartRepository
import com.example.my_car.domain.repository.VehicleRepository
import com.example.my_car.feature.documents.data.DocumentRepositoryImpl
import com.example.my_car.feature.maintenance.data.MaintenanceRepositoryImpl
import com.example.my_car.feature.mileage.data.MileageRepositoryImpl
import com.example.my_car.feature.parts.data.PartRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindMileageAlertNotifier(impl: NoOpMileageAlertNotifier): MileageAlertNotifier

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(impl: MaintenanceRepositoryImpl): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindPartRepository(impl: PartRepositoryImpl): PartRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        impl: com.example.my_car.feature.backup.data.repository.BackupRepositoryImpl
    ): com.example.my_car.feature.backup.domain.repository.BackupRepository
}