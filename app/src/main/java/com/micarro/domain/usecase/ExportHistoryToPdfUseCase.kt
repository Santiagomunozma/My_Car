package com.micarro.domain.usecase

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
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
    private val pageWidth = 595f // A4 width in points
    private val pageHeight = 842f // A4 height in points
    private val margin = 40f
    private val contentWidth = pageWidth - (margin * 2)

    suspend operator fun invoke(
        outputStream: OutputStream,
        vehicleId: String? = null,
        currencySymbol: String = "$"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val items = maintenanceRepository.observeHistory(vehicleId = vehicleId, filter = com.micarro.domain.model.HistoryFilter())
                .firstOrNull()
                .orEmpty()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val document = PdfDocument()
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var y = margin

            // Paints para diferentes estilos
            val titlePaint = Paint().apply {
                color = Color.parseColor("#1a237e") // Azul oscuro
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isFakeBoldText = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#424242")
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                color = Color.WHITE
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isFakeBoldText = true
            }

            val textPaint = Paint().apply {
                color = Color.parseColor("#212121")
                textSize = 10f
            }

            val labelPaint = Paint().apply {
                color = Color.parseColor("#616161")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isFakeBoldText = true
            }

            val valuePaint = Paint().apply {
                color = Color.parseColor("#212121")
                textSize = 10f
            }

            val linePaint = Paint().apply {
                color = Color.parseColor("#e0e0e0")
                strokeWidth = 1f
            }

            val tableHeaderPaint = Paint().apply {
                color = Color.parseColor("#1a237e") // Azul oscuro
                style = Paint.Style.FILL
            }

            val tableAlternatePaint = Paint().apply {
                color = Color.parseColor("#f5f5f5") // Gris claro
                style = Paint.Style.FILL
            }

            fun newPage() {
                document.finishPage(page)
                pageNumber += 1
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = margin
            }

            fun drawLine() {
                canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
                y += 10f
            }

            fun drawHeaderRect(text: String, x: Float, width: Float) {
                val rect = RectF(x, y, x + width, y + 30f)
                canvas.drawRect(rect, tableHeaderPaint)
                canvas.drawText(text, x + 5f, y + 20f, headerPaint)
            }

            fun drawTableRow(index: Int, cells: List<String>, widths: List<Float>) {
                val rowHeight = 30f
                if (y + rowHeight > pageHeight - margin) {
                    newPage()
                }

                // Fondo alternado
                if (index % 2 == 1) {
                    val rect = RectF(margin, y, pageWidth - margin, y + rowHeight)
                    canvas.drawRect(rect, tableAlternatePaint)
                }

                // Dibujar celdas
                var currentX = margin
                cells.forEachIndexed { i, cell ->
                    canvas.drawText(cell, currentX + 5f, y + 20f, textPaint)
                    currentX += widths[i]
                }

                // Borde inferior
                canvas.drawLine(margin, y + rowHeight, pageWidth - margin, y + rowHeight, linePaint)
                y += rowHeight
            }

            // ====== ENCABEZADO DEL DOCUMENTO ======
            
            // Rectángulo de encabezado
            val headerRect = RectF(0f, 0f, pageWidth, 80f)
            val headerBgPaint = Paint().apply {
                color = Color.parseColor("#1a237e")
                style = Paint.Style.FILL
            }
            canvas.drawRect(headerRect, headerBgPaint)

            // Título principal
            canvas.drawText("MiCarro", margin, 50f, titlePaint)
            canvas.drawText("Historial de Mantenimientos", margin, 75f, Paint().apply {
                color = Color.WHITE
                textSize = 16f
            })

            // Fecha en la esquina superior derecha
            val dateText = dateFormat.format(Date())
            canvas.drawText(dateText, pageWidth - margin - 120f, 50f, Paint().apply {
                color = Color.WHITE
                textSize = 10f
            })

            y = 100f

            // ====== BLOQUE DE METADATOS ======
            
            val totalRecords = items.size
            val totalExpenditure = items.sumOf { it.totalCost }
            val formattedTotalExpenditure = currencySymbol + String.format(Locale.US, "%.2f", totalExpenditure)

            val vehicleLabel = if (!vehicleId.isNullOrBlank()) {
                items.firstOrNull()?.vehiclePlate?.uppercase() ?: vehicleId
            } else {
                "TODOS LOS VEHÍCULOS"
            }

            canvas.drawText("Fecha de Generación:", margin, y, labelPaint)
            canvas.drawText(dateText, margin + 150f, y, valuePaint)
            y += 20f

            canvas.drawText("Vehículo:", margin, y, labelPaint)
            canvas.drawText(vehicleLabel, margin + 150f, y, valuePaint)
            y += 20f

            canvas.drawText("Total de Mantenimientos:", margin, y, labelPaint)
            canvas.drawText(totalRecords.toString(), margin + 150f, y, valuePaint)
            y += 20f

            canvas.drawText("Gasto Total Acumulado:", margin, y, labelPaint)
            canvas.drawText(formattedTotalExpenditure, margin + 150f, y, valuePaint)
            y += 30f

            drawLine()

            // ====== TABLA DE DATOS ======
            
            val columnWidths = listOf(80f, 100f, 120f, 80f, 60f, 70f, 85f) // Suma = 595 - 80 = 515
            val headers = listOf("Placa", "Fecha", "Servicio", "Categoría", "Km", "Costo", "Taller")

            // Encabezados de tabla
            var currentX = margin
            headers.forEachIndexed { i, header ->
                drawHeaderRect(header, currentX, columnWidths[i])
                currentX += columnWidths[i]
            }
            y += 30f

            // Filas de datos
            items.forEachIndexed { index, item ->
                val dateStr = dateFormat.format(Date(item.date))
                val costStr = currencySymbol + String.format(Locale.US, "%.2f", item.totalCost)
                val cells = listOf(
                    item.vehiclePlate,
                    dateStr,
                    item.title,
                    item.category,
                    item.mileage.toString(),
                    costStr,
                    item.workshopName
                )
                drawTableRow(index, cells, columnWidths)
            }

            // ====== PIE DE PÁGINA ======
            
            val footerY = pageHeight - 30f
            canvas.drawLine(margin, footerY, pageWidth - margin, footerY, linePaint)
            canvas.drawText("Página $pageNumber", pageWidth / 2 - 20f, footerY + 20f, Paint().apply {
                color = Color.parseColor("#9e9e9e")
                textSize = 9f
            })

            document.finishPage(page)
            outputStream.use { document.writeTo(it) }
            document.close()
        }
    }
}
