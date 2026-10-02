package com.micarro.domain.usecase

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.micarro.domain.repository.MaintenanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportHistoryToPdfUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) {
    suspend operator fun invoke(
        outputStream: OutputStream,
        vehiclePlate: String? = null,
        currencySymbol: String = "$"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val items = maintenanceRepository.observeHistory(vehiclePlate, com.micarro.domain.model.HistoryFilter())
                .firstOrNull()
                .orEmpty()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val document = PdfDocument()
            val paint = Paint().apply { textSize = 12f }
            val titlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
            var pageNumber = 1
            var page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
            var y = 40f
            fun newPage() {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                y = 40f
            }
            fun line(text: String, bold: Boolean = false) {
                if (y > 800f) newPage()
                page.canvas.drawText(text, 32f, y, if (bold) titlePaint else paint)
                y += if (bold) 24f else 18f
            }
            line("MiCarro - Historial de mantenimiento", bold = true)
            line("Generado: ${dateFormat.format(Date())}")
            line("Registros: ${items.size}")
            line("")
            if (items.isEmpty()) {
                line("No hay registros en el historial.")
            }
            items.forEach { item ->
                line("${item.vehiclePlate}  ${dateFormat.format(Date(item.date))}  ${item.title}", bold = true)
                line("${item.category}  ${item.mileage} km  ${item.workshopName}  $currencySymbol${String.format(Locale.US, "%.2f", item.totalCost)}")
            }
            document.finishPage(page)
            outputStream.use { document.writeTo(it) }
            document.close()
        }
    }
}
