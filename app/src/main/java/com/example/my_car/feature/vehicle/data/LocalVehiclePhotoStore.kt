package com.example.my_car.feature.vehicle.data

import android.content.Context
import android.net.Uri
import com.example.my_car.feature.vehicle.domain.VehiclePhotoStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalVehiclePhotoStore @Inject constructor(
    @ApplicationContext private val context: Context
) : VehiclePhotoStore {

    private val photosDir: File
        get() = File(context.filesDir, PHOTOS_DIR).apply { mkdirs() }

    override suspend fun importPhoto(sourceUri: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(sourceUri)
        context.contentResolver.openInputStream(uri)?.use { input ->
            val file = File(photosDir, "${UUID.randomUUID()}.jpg")
            file.outputStream().use { output -> input.copyTo(output) }
            file.absolutePath
        } ?: throw IOException("No se pudo abrir la imagen seleccionada")
    }

    override suspend fun deletePhoto(path: String) = withContext(Dispatchers.IO) {
        if (path.startsWith(photosDir.absolutePath)) {
            File(path).delete()
        }
    }

    companion object {
        const val PHOTOS_DIR = "vehicle_photos"
    }
}
