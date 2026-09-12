package com.example.my_car.domain.usecase

import com.example.my_car.domain.model.MaintenanceHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportHistoryToCsvUseCase @Inject constructor() {
    operator fun invoke(items: List<MaintenanceHistoryItem>): String {
        val csvHeader = "ID,Placa_Vehiculo,Titulo,Categoria,Fecha,Kilometraje,Costo,Taller\n"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val csvBody = items.joinToString("\n") { item ->
            val formattedDate = dateFormat.format(Date(item.date))
            val cleanTitle = item.title.replace(",", " ")
            val cleanWorkshop = item.workshopName.replace(",", " ")

            "${item.id},${item.vehiclePlate},$cleanTitle,${item.category},$formattedDate,${item.mileage},${item.totalCost},$cleanWorkshop"
        }

        return csvHeader + csvBody
    }
}