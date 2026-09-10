package com.example.my_car

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.my_car.core.navigation.MiCarroNavGraph
import com.example.my_car.ui.theme.My_CarTheme
import dagger.hilt.android.AndroidEntryPoint

// ¡Súper importante la anotación de Hilt aquí para que pueda inyectar dependencias en Compose!
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            My_CarTheme {
                // Un contenedor de superficie usando el color de fondo del tema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Inicializamos el controlador de navegación
                    val navController = rememberNavController()
                    // Llamamos a nuestro Grafo Central
                    MiCarroNavGraph(navController = navController)
                }
            }
        }
    }
}