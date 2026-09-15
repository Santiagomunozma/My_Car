package com.micarro.app.core.di

import android.content.Context
import androidx.room.Room
import com.micarro.app.core.database.AppDatabase
import com.micarro.app.domain.repository.DocumentRepository
import com.micarro.app.domain.repository.MileageRepository
import com.micarro.app.domain.repository.VehicleRepository
import com.micarro.app.feature.documents.data.DocumentDao
import com.micarro.app.feature.documents.data.DocumentRepositoryImpl
import com.micarro.app.feature.mileage.data.MileageDao
import com.micarro.app.feature.mileage.data.MileageRepositoryImpl
import com.micarro.app.feature.vehicle.data.VehicleDao
import com.micarro.app.feature.vehicle.data.VehicleRepositoryImpl
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "micarro_database.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideVehicleDao(database: AppDatabase): VehicleDao = database.vehicleDao()

    @Provides
    fun provideMileageDao(database: AppDatabase): MileageDao = database.mileageDao()

    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao = database.documentDao()

    @Provides
    @Singleton
    fun provideVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository = impl

    @Provides
    @Singleton
    fun provideMileageRepository(impl: MileageRepositoryImpl): MileageRepository = impl

    @Provides
    @Singleton
    fun provideDocumentRepository(impl: DocumentRepositoryImpl): DocumentRepository = impl
}
