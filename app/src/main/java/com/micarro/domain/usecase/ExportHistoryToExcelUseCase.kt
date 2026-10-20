package com.micarro.domain.usecase

import com.micarro.domain.model.HistoryFilter
import com.micarro.domain.model.MaintenanceHistoryItem
import com.micarro.domain.model.Part
import com.micarro.domain.model.Vehicle
import com.micarro.domain.repository.MaintenanceRepository
import com.micarro.domain.repository.PartRepository
import com.micarro.domain.repository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportHistoryToExcelUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository,
    private val partRepository: PartRepository,
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(
        outputStream: OutputStream,
        vehicleId: String? = null,
        currencySymbol: String = "$"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            configurePoiForAndroid()

            val items = maintenanceRepository
                .observeHistory(vehicleId, HistoryFilter())
                .first()
            val vehicles = vehicleRepository.observeAllVehicles().first()
            val vehicleById = vehicles.associateBy { it.id.toString() }

            val details = items.map { item ->
                val service = maintenanceRepository.getServiceById(item.id)
                val parts = partRepository.getPartsForService(item.id)
                HistoryExportRow(
                    item = item,
                    vehicle = vehicleById[item.vehicleId],
                    laborCost = service?.laborCost ?: 0.0,
                    otherCosts = service?.otherCosts ?: 0.0,
                    description = service?.description.orEmpty(),
                    parts = parts,
                    partsCost = parts.sumOf { it.cost * it.quantity }
                )
            }

            XSSFWorkbook().use { workbook ->
                val styles = ExcelStyles(workbook, currencySymbol)
                writeSummarySheet(workbook, styles, details, vehicleId, vehicles)
                writeHistorySheet(workbook, styles, details)
                writePartsSheet(workbook, styles, details)

                outputStream.use { stream ->
                    workbook.write(stream)
                    stream.flush()
                }
            }
        }
    }

    private fun writeSummarySheet(
        workbook: XSSFWorkbook,
        styles: ExcelStyles,
        details: List<HistoryExportRow>,
        vehicleId: String?,
        vehicles: List<Vehicle>
    ) {
        val sheet = workbook.createSheet("Resumen")
        sheet.tabColor = styles.primaryColor
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val selectedVehicle = vehicleId?.toLongOrNull()?.let { id -> vehicles.find { it.id == id } }
        val vehicleLabel = when {
            selectedVehicle != null -> "${selectedVehicle.plate} · ${selectedVehicle.brand} ${selectedVehicle.line} ${selectedVehicle.year}"
            else -> "Todos los vehículos"
        }

        val totalRecords = details.size
        val totalSpend = details.sumOf { it.item.totalCost }
        val averageTicket = if (totalRecords == 0) 0.0 else totalSpend / totalRecords
        val vehicleCount = details.map { it.item.vehicleId }.distinct().size
        val lastServiceDate = details.maxOfOrNull { it.item.date }?.let { dateTimeFormat.format(Date(it)) } ?: "—"

        var rowIndex = 1
        val titleRow = sheet.createRow(rowIndex++)
        titleRow.heightInPoints = 28f
        val titleCell = titleRow.createCell(1)
        titleCell.setCellValue("MiCarro — Informe de historial de mantenimiento")
        titleCell.cellStyle = styles.title
        sheet.addMergedRegion(CellRangeAddress(titleRow.rowNum, titleRow.rowNum, 1, 6))

        val subtitleRow = sheet.createRow(rowIndex++)
        val subtitleCell = subtitleRow.createCell(1)
        subtitleCell.setCellValue("Reporte consolidado de servicios, costos y repuestos")
        subtitleCell.cellStyle = styles.subtitle
        sheet.addMergedRegion(CellRangeAddress(subtitleRow.rowNum, subtitleRow.rowNum, 1, 6))

        rowIndex++
        rowIndex = writeInfoBox(
            sheet = sheet,
            startRow = rowIndex,
            styles = styles,
            rows = listOf(
                "Fecha de generación" to dateTimeFormat.format(Date()),
                "Alcance" to vehicleLabel,
                "Último servicio" to lastServiceDate
            )
        )

        rowIndex++
        val kpiHeader = sheet.createRow(rowIndex++)
        createTextCell(kpiHeader, 1, "Indicadores", styles.sectionHeader)
        sheet.addMergedRegion(CellRangeAddress(kpiHeader.rowNum, kpiHeader.rowNum, 1, 4))

        val kpiLabels = sheet.createRow(rowIndex++)
        listOf("Servicios", "Vehículos", "Gasto total", "Ticket promedio").forEachIndexed { index, label ->
            createTextCell(kpiLabels, index + 1, label, styles.kpiLabel)
        }
        val kpiValues = sheet.createRow(rowIndex++)
        kpiValues.heightInPoints = 22f
        createNumberCell(kpiValues, 1, totalRecords.toDouble(), styles.kpiNumber)
        createNumberCell(kpiValues, 2, vehicleCount.toDouble(), styles.kpiNumber)
        createNumberCell(kpiValues, 3, totalSpend, styles.kpiCurrency)
        createNumberCell(kpiValues, 4, averageTicket, styles.kpiCurrency)

        rowIndex += 2
        val byCategory = details.groupBy { it.item.category.ifBlank { "Sin categoría" } }
            .map { (category, rows) -> category to rows }
            .sortedByDescending { it.second.sumOf { row -> row.item.totalCost } }
        rowIndex = writeGroupedTable(
            sheet = sheet,
            startRow = rowIndex,
            styles = styles,
            title = "Gasto por categoría",
            headers = listOf("Categoría", "Servicios", "Gasto total", "% del total"),
            rows = byCategory.map { (category, rows) ->
                val amount = rows.sumOf { it.item.totalCost }
                listOf(
                    ExportCell.Text(category),
                    ExportCell.Number(rows.size.toDouble()),
                    ExportCell.Money(amount),
                    ExportCell.Percent(if (totalSpend == 0.0) 0.0 else amount / totalSpend)
                )
            }
        )

        rowIndex += 2
        val byVehicle = details.groupBy { it.item.vehicleId }
            .map { (id, rows) ->
                val plate = rows.first().item.vehiclePlate
                val name = rows.first().vehicleLabel()
                Triple("$plate  $name".trim(), rows.size, rows.sumOf { it.item.totalCost })
            }
            .sortedByDescending { it.third }
        writeGroupedTable(
            sheet = sheet,
            startRow = rowIndex,
            styles = styles,
            title = "Gasto por vehículo",
            headers = listOf("Vehículo", "Servicios", "Gasto total", "% del total"),
            rows = byVehicle.map { (label, count, amount) ->
                listOf(
                    ExportCell.Text(label.ifBlank { "Sin placa" }),
                    ExportCell.Number(count.toDouble()),
                    ExportCell.Money(amount),
                    ExportCell.Percent(if (totalSpend == 0.0) 0.0 else amount / totalSpend)
                )
            }
        )

        setColumnWidths(sheet, intArrayOf(4, 28, 16, 18, 16, 18, 22))
        sheet.createFreezePane(0, 3)
        sheet.printSetup.setFitWidth(1.toShort())
        sheet.printSetup.setFitHeight(0.toShort())
        sheet.fitToPage = true
    }

    private fun writeHistorySheet(
        workbook: XSSFWorkbook,
        styles: ExcelStyles,
        details: List<HistoryExportRow>
    ) {
        val sheet = workbook.createSheet("Historial")
        sheet.tabColor = styles.primaryColor
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val headers = listOf(
            "Fecha",
            "Placa",
            "Vehículo",
            "Servicio",
            "Categoría",
            "Tipo",
            "Kilometraje (km)",
            "Mano de obra",
            "Otros costos",
            "Repuestos",
            "Costo total",
            "Taller / responsable",
            "Descripción",
            "Cant. repuestos"
        )

        val titleRow = sheet.createRow(0)
        titleRow.heightInPoints = 24f
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("Detalle de mantenimientos")
        titleCell.cellStyle = styles.title
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, headers.lastIndex))

        val headerRow = sheet.createRow(2)
        headerRow.heightInPoints = 20f
        headers.forEachIndexed { index, header ->
            createTextCell(headerRow, index, header, styles.tableHeader)
        }

        if (details.isEmpty()) {
            val emptyRow = sheet.createRow(3)
            createTextCell(emptyRow, 0, "No hay mantenimientos registrados.", styles.emptyMessage)
            sheet.addMergedRegion(CellRangeAddress(3, 3, 0, headers.lastIndex))
        } else {
            details.forEachIndexed { index, row ->
                val dataRow = sheet.createRow(3 + index)
                val zebra = if (index % 2 == 0) styles.tableEven else styles.tableOdd
                val zebraNumber = if (index % 2 == 0) styles.numberEven else styles.numberOdd
                val zebraMoney = if (index % 2 == 0) styles.currencyEven else styles.currencyOdd
                createTextCell(dataRow, 0, dateFormat.format(Date(row.item.date)), zebra)
                createTextCell(dataRow, 1, row.item.vehiclePlate, zebra)
                createTextCell(dataRow, 2, row.vehicleLabel(), zebra)
                createTextCell(dataRow, 3, row.item.title, zebra)
                createTextCell(dataRow, 4, row.item.category, zebra)
                createTextCell(dataRow, 5, formatServiceType(row.item.serviceType), zebra)
                createNumberCell(dataRow, 6, row.item.mileage.toDouble(), zebraNumber)
                createNumberCell(dataRow, 7, row.laborCost, zebraMoney)
                createNumberCell(dataRow, 8, row.otherCosts, zebraMoney)
                createNumberCell(dataRow, 9, row.partsCost, zebraMoney)
                createNumberCell(dataRow, 10, row.item.totalCost, zebraMoney)
                createTextCell(dataRow, 11, row.item.workshopName, zebra)
                createTextCell(dataRow, 12, row.description, zebra)
                createNumberCell(dataRow, 13, row.parts.size.toDouble(), zebraNumber)
            }

            val totalsRowIndex = 3 + details.size
            val totalsRow = sheet.createRow(totalsRowIndex)
            totalsRow.heightInPoints = 20f
            createTextCell(totalsRow, 0, "TOTALES", styles.totalLabel)
            for (col in 1..5) createTextCell(totalsRow, col, "", styles.totalLabel)
            createNumberCell(totalsRow, 6, details.sumOf { it.item.mileage }.toDouble(), styles.totalNumber)
            createNumberCell(totalsRow, 7, details.sumOf { it.laborCost }, styles.totalCurrency)
            createNumberCell(totalsRow, 8, details.sumOf { it.otherCosts }, styles.totalCurrency)
            createNumberCell(totalsRow, 9, details.sumOf { it.partsCost }, styles.totalCurrency)
            createNumberCell(totalsRow, 10, details.sumOf { it.item.totalCost }, styles.totalCurrency)
            createTextCell(totalsRow, 11, "", styles.totalLabel)
            createTextCell(totalsRow, 12, "", styles.totalLabel)
            createNumberCell(totalsRow, 13, details.sumOf { it.parts.size }.toDouble(), styles.totalNumber)

            val lastDataRow = totalsRowIndex - 1
            sheet.setAutoFilter(CellRangeAddress(2, lastDataRow, 0, headers.lastIndex))
        }

        setColumnWidths(sheet, intArrayOf(14, 12, 28, 28, 16, 14, 18, 16, 16, 16, 16, 24, 36, 16))
        sheet.createFreezePane(0, 3)
    }

    private fun writePartsSheet(
        workbook: XSSFWorkbook,
        styles: ExcelStyles,
        details: List<HistoryExportRow>
    ) {
        val sheet = workbook.createSheet("Repuestos")
        sheet.tabColor = styles.primaryColor
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val headers = listOf(
            "Fecha",
            "Placa",
            "Servicio",
            "Repuesto",
            "Marca",
            "Referencia",
            "Proveedor",
            "Cantidad",
            "Costo unitario",
            "Subtotal",
            "Garantía",
            "Notas"
        )

        val titleRow = sheet.createRow(0)
        titleRow.heightInPoints = 24f
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("Repuestos e insumos utilizados")
        titleCell.cellStyle = styles.title
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, headers.lastIndex))

        val headerRow = sheet.createRow(2)
        headerRow.heightInPoints = 20f
        headers.forEachIndexed { index, header ->
            createTextCell(headerRow, index, header, styles.tableHeader)
        }

        val partsRows = details.flatMap { row ->
            row.parts.map { part -> row to part }
        }

        if (partsRows.isEmpty()) {
            val emptyRow = sheet.createRow(3)
            createTextCell(emptyRow, 0, "No hay repuestos registrados en el historial.", styles.emptyMessage)
            sheet.addMergedRegion(CellRangeAddress(3, 3, 0, headers.lastIndex))
        } else {
            partsRows.forEachIndexed { index, (row, part) ->
                val dataRow = sheet.createRow(3 + index)
                val zebra = if (index % 2 == 0) styles.tableEven else styles.tableOdd
                val zebraNumber = if (index % 2 == 0) styles.numberEven else styles.numberOdd
                val zebraMoney = if (index % 2 == 0) styles.currencyEven else styles.currencyOdd
                createTextCell(dataRow, 0, dateFormat.format(Date(row.item.date)), zebra)
                createTextCell(dataRow, 1, row.item.vehiclePlate, zebra)
                createTextCell(dataRow, 2, row.item.title, zebra)
                createTextCell(dataRow, 3, part.name, zebra)
                createTextCell(dataRow, 4, part.brand.orEmpty(), zebra)
                createTextCell(dataRow, 5, part.reference.orEmpty(), zebra)
                createTextCell(dataRow, 6, part.provider.orEmpty(), zebra)
                createNumberCell(dataRow, 7, part.quantity.toDouble(), zebraNumber)
                createNumberCell(dataRow, 8, part.cost, zebraMoney)
                createNumberCell(dataRow, 9, part.cost * part.quantity, zebraMoney)
                createTextCell(dataRow, 10, part.warranty.orEmpty(), zebra)
                createTextCell(dataRow, 11, part.notes.orEmpty(), zebra)
            }

            val totalsRow = sheet.createRow(3 + partsRows.size)
            createTextCell(totalsRow, 0, "TOTALES", styles.totalLabel)
            for (col in 1..6) createTextCell(totalsRow, col, "", styles.totalLabel)
            createNumberCell(totalsRow, 7, partsRows.sumOf { it.second.quantity }.toDouble(), styles.totalNumber)
            createTextCell(totalsRow, 8, "", styles.totalLabel)
            createNumberCell(totalsRow, 9, partsRows.sumOf { it.second.cost * it.second.quantity }, styles.totalCurrency)
            createTextCell(totalsRow, 10, "", styles.totalLabel)
            createTextCell(totalsRow, 11, "", styles.totalLabel)

            sheet.setAutoFilter(CellRangeAddress(2, 2 + partsRows.size, 0, headers.lastIndex))
        }

        setColumnWidths(sheet, intArrayOf(14, 12, 28, 24, 16, 16, 18, 12, 16, 16, 16, 28))
        sheet.createFreezePane(0, 3)
    }

    private fun writeInfoBox(
        sheet: XSSFSheet,
        startRow: Int,
        styles: ExcelStyles,
        rows: List<Pair<String, String>>
    ): Int {
        var rowIndex = startRow
        rows.forEachIndexed { index, (label, value) ->
            val row = sheet.createRow(rowIndex++)
            val labelStyle = if (index == 0) styles.infoLabelFirst else styles.infoLabel
            val valueStyle = if (index == 0) styles.infoValueFirst else styles.infoValue
            createTextCell(row, 1, label, labelStyle)
            createTextCell(row, 2, value, valueStyle)
            sheet.addMergedRegion(CellRangeAddress(row.rowNum, row.rowNum, 2, 4))
        }
        val region = CellRangeAddress(startRow, startRow + rows.lastIndex, 1, 4)
        RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet)
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet)
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet)
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet)
        return rowIndex
    }

    private fun writeGroupedTable(
        sheet: XSSFSheet,
        startRow: Int,
        styles: ExcelStyles,
        title: String,
        headers: List<String>,
        rows: List<List<ExportCell>>
    ): Int {
        var rowIndex = startRow
        val titleRow = sheet.createRow(rowIndex++)
        createTextCell(titleRow, 1, title, styles.sectionHeader)
        sheet.addMergedRegion(CellRangeAddress(titleRow.rowNum, titleRow.rowNum, 1, headers.size))

        val headerRow = sheet.createRow(rowIndex++)
        headers.forEachIndexed { index, header ->
            createTextCell(headerRow, index + 1, header, styles.tableHeader)
        }

        if (rows.isEmpty()) {
            val emptyRow = sheet.createRow(rowIndex++)
            createTextCell(emptyRow, 1, "Sin datos para mostrar.", styles.emptyMessage)
            sheet.addMergedRegion(CellRangeAddress(emptyRow.rowNum, emptyRow.rowNum, 1, headers.size))
            return rowIndex
        }

        rows.forEachIndexed { index, cells ->
            val dataRow = sheet.createRow(rowIndex++)
            val textStyle = if (index % 2 == 0) styles.tableEven else styles.tableOdd
            val numberStyle = if (index % 2 == 0) styles.numberEven else styles.numberOdd
            val moneyStyle = if (index % 2 == 0) styles.currencyEven else styles.currencyOdd
            val percentStyle = if (index % 2 == 0) styles.percentEven else styles.percentOdd
            cells.forEachIndexed { col, cell ->
                when (cell) {
                    is ExportCell.Text -> createTextCell(dataRow, col + 1, cell.value, textStyle)
                    is ExportCell.Number -> createNumberCell(dataRow, col + 1, cell.value, numberStyle)
                    is ExportCell.Money -> createNumberCell(dataRow, col + 1, cell.value, moneyStyle)
                    is ExportCell.Percent -> createNumberCell(dataRow, col + 1, cell.value, percentStyle)
                }
            }
        }
        return rowIndex
    }

    private fun createTextCell(row: Row, column: Int, value: String, style: XSSFCellStyle) {
        val cell = row.createCell(column)
        cell.setCellValue(value)
        cell.cellStyle = style
    }

    private fun createNumberCell(row: Row, column: Int, value: Double, style: XSSFCellStyle) {
        val cell = row.createCell(column)
        cell.setCellValue(value)
        cell.cellStyle = style
    }

    private fun setColumnWidths(sheet: Sheet, widths: IntArray) {
        widths.forEachIndexed { index, chars ->
            sheet.setColumnWidth(index, (chars * 256).coerceAtMost(255 * 256))
        }
    }

    private fun formatServiceType(type: String): String = when (type.uppercase(Locale.US)) {
        "PREVENTIVE" -> "Preventivo"
        "CORRECTIVE" -> "Correctivo"
        else -> type.ifBlank { "—" }
    }

    private fun configurePoiForAndroid() {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }

    private data class HistoryExportRow(
        val item: MaintenanceHistoryItem,
        val vehicle: Vehicle?,
        val laborCost: Double,
        val otherCosts: Double,
        val description: String,
        val parts: List<Part>,
        val partsCost: Double
    ) {
        fun vehicleLabel(): String {
            val current = vehicle ?: return ""
            return listOf(current.brand, current.line, current.year.toString())
                .filter { it.isNotBlank() }
                .joinToString(" ")
        }
    }

    private sealed class ExportCell {
        data class Text(val value: String) : ExportCell()
        data class Number(val value: Double) : ExportCell()
        data class Money(val value: Double) : ExportCell()
        data class Percent(val value: Double) : ExportCell()
    }

    private class ExcelStyles(workbook: XSSFWorkbook, currencySymbol: String) {
        private val colorMap = DefaultIndexedColorMap()
        val primaryColor = color(0xC4, 0x1E, 0x3A)
        private val white = color(0xFF, 0xFF, 0xFF)
        private val darkText = color(0x1A, 0x1D, 0x23)
        private val mutedText = color(0x6B, 0x72, 0x80)
        private val surface = color(0xF3, 0xF4, 0xF6)
        private val evenFill = color(0xFF, 0xFF, 0xFF)
        private val oddFill = color(0xFD, 0xE8, 0xEC)
        private val totalFill = color(0x1B, 0x22, 0x30)
        private val borderColor = color(0xD1, 0xD5, 0xDB)

        private val titleFont = font(workbook, bold = true, size = 16, color = darkText)
        private val subtitleFont = font(workbook, bold = false, size = 11, color = mutedText)
        private val headerFont = font(workbook, bold = true, size = 10, color = white)
        private val sectionFont = font(workbook, bold = true, size = 12, color = white)
        private val bodyFont = font(workbook, bold = false, size = 10, color = darkText)
        private val labelFont = font(workbook, bold = true, size = 10, color = darkText)
        private val kpiFont = font(workbook, bold = true, size = 14, color = primaryColor)
        private val totalFont = font(workbook, bold = true, size = 10, color = white)

        private val currencyFormat = workbook.createDataFormat().getFormat("\"$currencySymbol\"#,##0.00")
        private val numberFormat = workbook.createDataFormat().getFormat("#,##0")
        private val percentFormat = workbook.createDataFormat().getFormat("0.0%")

        val title = baseStyle(workbook, titleFont, align = HorizontalAlignment.LEFT)
        val subtitle = baseStyle(workbook, subtitleFont, align = HorizontalAlignment.LEFT)
        val sectionHeader = baseStyle(
            workbook,
            sectionFont,
            align = HorizontalAlignment.LEFT,
            fill = primaryColor,
            bordered = true
        )
        val tableHeader = baseStyle(
            workbook,
            headerFont,
            align = HorizontalAlignment.CENTER,
            fill = primaryColor,
            bordered = true
        )
        val kpiLabel = baseStyle(
            workbook,
            headerFont,
            align = HorizontalAlignment.CENTER,
            fill = primaryColor,
            bordered = true
        )
        val kpiNumber = numericStyle(workbook, kpiFont, numberFormat, HorizontalAlignment.CENTER, surface, true)
        val kpiCurrency = numericStyle(workbook, kpiFont, currencyFormat, HorizontalAlignment.CENTER, surface, true)
        val infoLabel = baseStyle(workbook, labelFont, fill = surface, bordered = true)
        val infoValue = baseStyle(workbook, bodyFont, fill = evenFill, bordered = true)
        val infoLabelFirst = infoLabel
        val infoValueFirst = infoValue
        val tableEven = baseStyle(workbook, bodyFont, fill = evenFill, bordered = true)
        val tableOdd = baseStyle(workbook, bodyFont, fill = oddFill, bordered = true)
        val numberEven = numericStyle(workbook, bodyFont, numberFormat, HorizontalAlignment.RIGHT, evenFill, true)
        val numberOdd = numericStyle(workbook, bodyFont, numberFormat, HorizontalAlignment.RIGHT, oddFill, true)
        val currencyEven = numericStyle(workbook, bodyFont, currencyFormat, HorizontalAlignment.RIGHT, evenFill, true)
        val currencyOdd = numericStyle(workbook, bodyFont, currencyFormat, HorizontalAlignment.RIGHT, oddFill, true)
        val percentEven = numericStyle(workbook, bodyFont, percentFormat, HorizontalAlignment.RIGHT, evenFill, true)
        val percentOdd = numericStyle(workbook, bodyFont, percentFormat, HorizontalAlignment.RIGHT, oddFill, true)
        val totalLabel = baseStyle(workbook, totalFont, fill = totalFill, bordered = true)
        val totalNumber = numericStyle(workbook, totalFont, numberFormat, HorizontalAlignment.RIGHT, totalFill, true)
        val totalCurrency = numericStyle(workbook, totalFont, currencyFormat, HorizontalAlignment.RIGHT, totalFill, true)
        val emptyMessage = baseStyle(workbook, subtitleFont, fill = surface, bordered = true)

        private fun color(r: Int, g: Int, b: Int) = XSSFColor(
            byteArrayOf(r.toByte(), g.toByte(), b.toByte()),
            colorMap
        )

        private fun font(
            workbook: XSSFWorkbook,
            bold: Boolean,
            size: Short,
            color: XSSFColor
        ): XSSFFont {
            val font = workbook.createFont()
            font.fontName = "Calibri"
            font.bold = bold
            font.fontHeightInPoints = size
            font.setColor(color)
            return font
        }

        private fun baseStyle(
            workbook: XSSFWorkbook,
            font: XSSFFont,
            align: HorizontalAlignment = HorizontalAlignment.LEFT,
            fill: XSSFColor? = null,
            bordered: Boolean = false
        ): XSSFCellStyle {
            val style = workbook.createCellStyle()
            style.setFont(font)
            style.alignment = align
            style.verticalAlignment = VerticalAlignment.CENTER
            style.wrapText = true
            if (fill != null) {
                style.setFillForegroundColor(fill)
                style.fillPattern = FillPatternType.SOLID_FOREGROUND
            }
            if (bordered) applyBorders(style)
            return style
        }

        private fun numericStyle(
            workbook: XSSFWorkbook,
            font: XSSFFont,
            format: Short,
            align: HorizontalAlignment,
            fill: XSSFColor,
            bordered: Boolean
        ): XSSFCellStyle {
            val style = baseStyle(workbook, font, align, fill, bordered)
            style.dataFormat = format
            return style
        }

        private fun applyBorders(style: XSSFCellStyle) {
            style.borderTop = BorderStyle.THIN
            style.borderBottom = BorderStyle.THIN
            style.borderLeft = BorderStyle.THIN
            style.borderRight = BorderStyle.THIN
            style.setTopBorderColor(borderColor)
            style.setBottomBorderColor(borderColor)
            style.setLeftBorderColor(borderColor)
            style.setRightBorderColor(borderColor)
        }
    }
}
