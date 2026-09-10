package com.example.my_car

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MiCarroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Aquí luego podemos inicializar utilidades compartidas si es necesario
    }
}