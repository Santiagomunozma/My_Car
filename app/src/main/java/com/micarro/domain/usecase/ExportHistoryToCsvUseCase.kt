package com.micarro.domain.usecase

import com.micarro.domain.repository.MaintenanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportHistoryToCsvUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) {
    /**
     * Exporta el historial de mantenimientos con plantilla de informe formal en formato CSV.
     * @param outputStream Stream de salida del archivo seleccionado por SAF
     * @param vehiclePlate Placa específica para filtrar, o null/vacía para exportar todo el historial
     */
    suspend operator fun invoke(
        outputStream: OutputStream,
        vehiclePlate: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Obtener los servicios realizados desde el repositorio
            val services = maintenanceRepository.observeServices(vehiclePlate ?: "")
                .firstOrNull() ?: emptyList()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            // 2. Cargar métricas del reporte
            val totalRecords = services.size
            val totalExpenditure = services.sumOf { it.totalCost }
            val formattedTotalExpenditure = String.format(Locale.US, "$%.2f", totalExpenditure)

            // Kotlin aplica smart-cast automático a String dentro del bloque 'if' al usar isNullOrBlank()
            val vehicleLabel = if (!vehiclePlate.isNullOrBlank()) {
                vehiclePlate.uppercase()
            } else {
                "TODOS LOS VEHÍCULOS"
            }

            // 3. Construir el reporte en formato plantilla + tabla
            val csvContent = buildString {
                // SECCIÓN 1: PLANTILLA / ENCABEZADO FORMAL
                appendLine("==================================================================")
                appendLine("   MICARRO - INFORME OFICIAL DE HISTORIAL DE MANTENIMIENTO")
                appendLine("==================================================================")
                appendLine("Fecha de Generación:, $currentDate")
                appendLine("Vehículo / Filtro:, $vehicleLabel")
                appendLine("Total de Mantenimientos:, $totalRecords")
                appendLine("Gasto Total Acumulado:, $formattedTotalExpenditure")
                appendLine()

                // SECCIÓN 2: TABLA DE DATOS
                appendLine("Placa,Fecha,Servicio,Categoría,Kilometraje (km),Costo Total,Taller / Responsable")

                services.forEach { service ->
                    val serviceDate = dateFormat.format(Date(service.date))
                    val safeTitle = escapeCsv(service.title)
                    val safeCategory = escapeCsv(service.category)
                    val safeWorkshop = escapeCsv(service.workshopName ?: "N/A")
                    val formattedCost = String.format(Locale.US, "%.2f", service.totalCost)

                    appendLine("${service.vehicleId},$serviceDate,$safeTitle,$safeCategory,${service.mileage},$formattedCost,$safeWorkshop")
                }
            }

            // 4. Escribir el buffer con Byte Order Mark (BOM) UTF-8 para Excel
            outputStream.use { stream ->
                stream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                stream.write(csvContent.toByteArray(Charsets.UTF_8))
            }
        }
    }

    /**
     * Escapa caracteres para prevenir ruptura de columnas si hay comas o saltos de línea.
     */
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}