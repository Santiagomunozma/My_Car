package com.micarro.feature.vehicle.domain

/**
 * Almacén de fotos de vehículos. La interfaz vive en domain; la
 * implementación [LocalVehiclePhotoStore] en data copia la imagen elegida
 * al almacenamiento interno (vehicle_photos/) y devuelve la ruta local.
 */
interface VehiclePhotoStore {
    /** Importa la imagen seleccionada y devuelve la ruta del archivo gestionado. */
    suspend fun importPhoto(sourceUri: String): String

    /** Borra el archivo gestionado si pertenece al almacén. */
    suspend fun deletePhoto(path: String)
}
