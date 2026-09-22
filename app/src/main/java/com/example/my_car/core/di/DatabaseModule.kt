package com.example.my_car.core.di

import android.content.Context
import androidx.room.Room
import com.example.my_car.core.database.AppDatabase
import com.example.my_car.feature.documents.data.DocumentDao
import com.example.my_car.feature.maintenance.data.MaintenancePlanDao
import com.example.my_car.feature.maintenance.data.MaintenanceServiceDao
import com.example.my_car.feature.mileage.data.MileageDao
import com.example.my_car.feature.parts.data.PartDao
import com.example.my_car.feature.vehicle.data.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my_car_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideVehicleDao(database: AppDatabase): VehicleDao = database.vehicleDao()

    @Provides
    @Singleton
    fun provideMileageDao(database: AppDatabase): MileageDao = database.mileageDao()

    @Provides
    @Singleton
    fun provideDocumentDao(database: AppDatabase): DocumentDao = database.documentDao()

    @Provides
    @Singleton
    fun provideMaintenancePlanDao(database: AppDatabase): MaintenancePlanDao = database.maintenancePlanDao()

    @Provides
    @Singleton
    fun provideMaintenanceServiceDao(database: AppDatabase): MaintenanceServiceDao = database.maintenanceServiceDao()

    @Provides
    @Singleton
    fun providePartDao(database: AppDatabase): PartDao = database.partDao()

    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson = com.google.gson.Gson()
}
