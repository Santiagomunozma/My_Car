package com.micarro.core.di

import com.micarro.feature.alerts.domain.AlertScheduler
import com.micarro.feature.maintenance.domain.MaintenanceUseCases
import com.micarro.feature.parts.domain.PartUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideMaintenanceUseCases(
        observePlans: com.micarro.feature.maintenance.domain.ObserveMaintenancePlansUseCase,
        savePlan: com.micarro.feature.maintenance.domain.SaveMaintenancePlanUseCase,
        updatePlan: com.micarro.feature.maintenance.domain.UpdateMaintenancePlanUseCase,
        updatePlanActiveStatus: com.micarro.feature.maintenance.domain.UpdateMaintenancePlanActiveStatusUseCase,
        deletePlan: com.micarro.feature.maintenance.domain.DeleteMaintenancePlanUseCase,
        registerService: com.micarro.feature.maintenance.domain.RegisterMaintenanceServiceUseCase,
        updateService: com.micarro.feature.maintenance.domain.UpdateMaintenanceServiceUseCase,
        deleteService: com.micarro.feature.maintenance.domain.DeleteMaintenanceServiceUseCase,
        observeServices: com.micarro.feature.maintenance.domain.ObserveMaintenanceServicesUseCase
    ): MaintenanceUseCases {
        return MaintenanceUseCases(
            observePlans = observePlans,
            savePlan = savePlan,
            updatePlan = updatePlan,
            updatePlanActiveStatus = updatePlanActiveStatus,
            deletePlan = deletePlan,
            registerService = registerService,
            updateService = updateService,
            deleteService = deleteService,
            observeServices = observeServices
        )
    }

    @Provides
    @Singleton
    fun providePartUseCases(
        observeInstalledParts: com.micarro.feature.parts.domain.ObserveInstalledPartsUseCase
    ): PartUseCases {
        return PartUseCases(
            observeInstalledParts = observeInstalledParts
        )
    }

    @Provides
    @Singleton
    fun provideAlertScheduler(
        coreAlertScheduler: com.micarro.core.worker.AlertScheduler,
        settingsRepository: com.micarro.domain.repository.AlertSettingsRepository
    ): AlertScheduler {
        return AlertScheduler(coreAlertScheduler, settingsRepository)
    }
}
