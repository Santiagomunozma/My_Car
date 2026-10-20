package com.micarro.domain.usecase

import com.micarro.domain.repository.MaintenanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportHistoryToExcelUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) {
    /**
     * Exporta el historial de mantenimientos a un archivo Excel .xlsx bien formateado.
     * @param outputStream Stream de salida del archivo seleccionado por SAF
     * @param vehiclePlate Placa específica para filtrar, o null/vacía para exportar todo el historial
     * @param currencySymbol Símbolo de moneda a usar (ej. "$")
     */
    suspend operator fun invoke(
        outputStream: OutputStream,
        vehiclePlate: String? = null,
        currencySymbol: String = "$"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Obtener los servicios realizados desde el repositorio
            val items = maintenanceRepository.observeHistory(vehiclePlate, com.micarro.domain.model.HistoryFilter())
                .firstOrNull()
                .orEmpty()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            // 2. Cargar métricas del reporte
            val totalRecords = items.size
            val totalExpenditure = items.sumOf { it.totalCost }
            val formattedTotalExpenditure = currencySymbol + String.format(Locale.US, "%.2f", totalExpenditure)

            val vehicleLabel = if (!vehiclePlate.isNullOrBlank()) {
                vehiclePlate.uppercase()
            } else {
                "TODOS LOS VEHÍCULOS"
            }

            // 3. Crear workbook y sheet
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Historial de Mantenimientos")

            // 4. Estilos de celda
            val titleStyle = createTitleStyle(workbook)
            val headerStyle = createHeaderStyle(workbook)
            val labelStyle = createLabelStyle(workbook)
            val currencyStyle = createCurrencyStyle(workbook, currencySymbol)
            val numberStyle = createNumberStyle(workbook)
            val textStyle = createTextStyle(workbook)

            var rowIndex = 0

            // 5. BLOQUE DE ENCABEZADO DEL DOCUMENTO
            // Título principal (merge cells A1 hasta G1)
            val titleRow = sheet.createRow(rowIndex++)
            val titleCell = titleRow.createCell(0)
            titleCell.setCellValue("MICARRO - INFORME OFICIAL DE HISTORIAL DE MANTENIMIENTO")
            titleCell.cellStyle = titleStyle
            sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 6))

            // Línea en blanco
            sheet.createRow(rowIndex++)

            // Metadatos organizados
            createMetadataRow(sheet, rowIndex++, "Fecha de Generación:", currentDate, labelStyle, textStyle)
            createMetadataRow(sheet, rowIndex++, "Vehículo / Filtro:", vehicleLabel, labelStyle, textStyle)
            createMetadataRow(sheet, rowIndex++, "Total de Mantenimientos:", totalRecords.toString(), labelStyle, textStyle)
            createMetadataRow(sheet, rowIndex++, "Gasto Total Acumulado:", formattedTotalExpenditure, labelStyle, textStyle)

            // Línea separadora
            sheet.createRow(rowIndex++)

            // 6. TABLA DE DATOS PRINCIPALES
            // Encabezados de columna
            val headerRow = sheet.createRow(rowIndex++)
            val headers = arrayOf("Placa", "Fecha", "Servicio", "Categoría", "Kilometraje (km)", "Costo Total", "Taller / Responsable")
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            // Filas de datos
            items.forEach { service ->
                val dataRow = sheet.createRow(rowIndex++)
                val serviceDate = dateFormat.format(Date(service.date))

                // Placa
                createCell(dataRow, 0, service.vehiclePlate, textStyle)
                // Fecha
                createCell(dataRow, 1, serviceDate, textStyle)
                // Servicio
                createCell(dataRow, 2, service.title, textStyle)
                // Categoría
                createCell(dataRow, 3, service.category, textStyle)
                // Kilometraje (formato numérico)
                val mileageCell = dataRow.createCell(4)
                mileageCell.setCellValue(service.mileage.toDouble())
                mileageCell.cellStyle = numberStyle
                // Costo Total (formato moneda)
                val costCell = dataRow.createCell(5)
                costCell.setCellValue(service.totalCost)
                costCell.cellStyle = currencyStyle
                // Taller
                createCell(dataRow, 6, service.workshopName, textStyle)
            }

            // 7. AUTO-ADJUST DE COLUMNAS
            for (i in 0..6) {
                sheet.autoSizeColumn(i)
                // Añadir un pequeño margen extra para evitar que los números aparezcan como ###
                val currentWidth = sheet.getColumnWidth(i)
                sheet.setColumnWidth(i, (currentWidth * 1.2).toInt())
            }

            // 8. Escribir el workbook al output stream
            outputStream.use { stream ->
                workbook.write(stream)
            }
            workbook.close()
        }
    }

    private fun createTitleStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 14
        style.setFont(font)
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        return style
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.color = IndexedColors.WHITE.index
        style.setFont(font)
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.index)
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        style.setBorderTop(BorderStyle.THIN)
        style.setBorderBottom(BorderStyle.THIN)
        style.setBorderLeft(BorderStyle.THIN)
        style.setBorderRight(BorderStyle.THIN)
        return style
    }

    private fun createLabelStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        style.setFont(font)
        return style
    }

    private fun createCurrencyStyle(workbook: XSSFWorkbook, symbol: String): XSSFCellStyle {
        val style = workbook.createCellStyle()
        val format = workbook.createDataFormat()
        style.dataFormat = format.getFormat("$symbol#,##0.00")
        style.alignment = HorizontalAlignment.RIGHT
        style.setBorderTop(BorderStyle.THIN)
        style.setBorderBottom(BorderStyle.THIN)
        style.setBorderLeft(BorderStyle.THIN)
        style.setBorderRight(BorderStyle.THIN)
        return style
    }

    private fun createNumberStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        val format = workbook.createDataFormat()
        style.dataFormat = format.getFormat("#,##0")
        style.alignment = HorizontalAlignment.RIGHT
        style.setBorderTop(BorderStyle.THIN)
        style.setBorderBottom(BorderStyle.THIN)
        style.setBorderLeft(BorderStyle.THIN)
        style.setBorderRight(BorderStyle.THIN)
        return style
    }

    private fun createTextStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val style = workbook.createCellStyle()
        style.alignment = HorizontalAlignment.LEFT
        style.setBorderTop(BorderStyle.THIN)
        style.setBorderBottom(BorderStyle.THIN)
        style.setBorderLeft(BorderStyle.THIN)
        style.setBorderRight(BorderStyle.THIN)
        return style
    }

    private fun createMetadataRow(
        sheet: Sheet,
        rowIndex: Int,
        label: String,
        value: String,
        labelStyle: XSSFCellStyle,
        textStyle: XSSFCellStyle
    ) {
        val row = sheet.createRow(rowIndex)
        val labelCell = row.createCell(0)
        labelCell.setCellValue(label)
        labelCell.cellStyle = labelStyle

        val valueCell = row.createCell(1)
        valueCell.setCellValue(value)
        valueCell.cellStyle = textStyle
    }

    private fun createCell(row: Row, columnIndex: Int, value: String, style: XSSFCellStyle) {
        val cell = row.createCell(columnIndex)
        cell.setCellValue(value)
        cell.cellStyle = style
    }
}
