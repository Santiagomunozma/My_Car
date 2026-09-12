package com.example.my_car.core.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object FileExportHelper {
    fun shareCsvFile(context: Context, csvContent: String, fileName: String = "historial_mantenimientos.csv") {
        val file = File(context.cacheDir, fileName)
        file.writeText(csvContent)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Exportación de Historial - MiCarro")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Exportar historial a..."))
    }
}