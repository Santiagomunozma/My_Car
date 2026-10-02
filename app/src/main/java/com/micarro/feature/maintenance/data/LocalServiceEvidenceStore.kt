package com.micarro.feature.maintenance.data

import android.content.Context
import android.net.Uri
import com.micarro.feature.maintenance.domain.ServiceEvidenceStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalServiceEvidenceStore @Inject constructor(
    @ApplicationContext private val context: Context
) : ServiceEvidenceStore {

    private val dir: File
        get() = File(context.filesDir, DIR).apply { mkdirs() }

    override suspend fun import(sourceUri: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(sourceUri)
        context.contentResolver.openInputStream(uri)?.use { input ->
            val extension = when {
                sourceUri.contains("pdf", ignoreCase = true) -> "pdf"
                else -> "jpg"
            }
            val file = File(dir, "${UUID.randomUUID()}.$extension")
            file.outputStream().use { output -> input.copyTo(output) }
            file.absolutePath
        } ?: throw IOException("No se pudo abrir el soporte seleccionado")
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dir.listFiles()?.forEach { it.delete() }
        }
    }

    companion object {
        const val DIR = "service_evidence"
    }
}
