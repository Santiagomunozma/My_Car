package com.micarro.core.di

import com.micarro.core.settings.SharedPreferencesAlertSettingsRepository
import com.micarro.domain.alerts.MileageAlertNotifier
import com.micarro.domain.repository.AlertSettingsRepository
import com.micarro.domain.repository.DocumentRepository
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.MileageRepository
import com.micarro.domain.repository.PartRepository
import com.micarro.domain.repository.VehicleRepository
import com.micarro.feature.documents.data.DocumentRepositoryImpl
import com.micarro.feature.maintenance.data.MaintenanceRepositoryImpl
import com.micarro.feature.mileage.data.MileageRepositoryImpl
import com.micarro.feature.parts.data.PartRepositoryImpl
import com.micarro.feature.vehicle.data.LocalVehiclePhotoStore
import com.micarro.feature.vehicle.data.VehicleRepositoryImpl
import com.micarro.feature.vehicle.domain.VehiclePhotoStore
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
    abstract fun bindMileageAlertNotifier(
        impl: com.micarro.feature.alerts.domain.MileageAlertScheduler
    ): MileageAlertNotifier

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(impl: MaintenanceRepositoryImpl): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindPartRepository(impl: PartRepositoryImpl): PartRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        impl: com.micarro.feature.backup.data.repository.BackupRepositoryImpl
    ): com.micarro.feature.backup.domain.repository.BackupRepository
}