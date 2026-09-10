package com.example.my_car.core.di

import android.content.Context
import androidx.room.Room
import com.example.my_car.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Proveemos la instancia única (Singleton) de nuestra base de datos
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "micarro_db"
        )
            .fallbackToDestructiveMigration() // Útil mientras estamos en desarrollo iterando la BD
            .build()
    }

    // Nota para el equipo: A medida que los compañeros 1 y 2 creen sus DAOs,
    // nosotros crearemos aquí las funciones @Provides para esos DAOs y sus Repositorios.
    // Ejemplo futuro:
    // @Provides
    // fun provideVehicleDao(database: AppDatabase): VehicleDao = database.vehicleDao()
}