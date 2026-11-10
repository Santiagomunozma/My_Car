package com.micarro.feature.documents.data

import android.content.Context
import android.net.Uri
import com.micarro.feature.documents.domain.DocumentFileStore
import com.micarro.feature.documents.domain.DocumentFileTooLarge
import com.micarro.feature.documents.domain.StoredDocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDocumentFileStore @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentFileStore {

    private val dir: File
        get() = File(context.filesDir, DIR).apply { mkdirs() }

    override suspend fun importUri(sourceUri: String): StoredDocumentFile = withContext(Dispatchers.IO) {
        val uri = Uri.parse(sourceUri)
        val mime = context.contentResolver.getType(uri)?.substringBefore(';')
            ?: mimeFromName(uri.lastPathSegment)
        context.contentResolver.openInputStream(uri)?.use { input ->
            copy(input, normalizeMime(mime))
        } ?: throw IOException("No se pudo abrir el archivo seleccionado")
    }

    override suspend fun importLocalFile(path: String, mimeType: String): StoredDocumentFile =
        withContext(Dispatchers.IO) {
            val source = File(path)
            val stored = source.inputStream().use { copy(it, normalizeMime(mimeType)) }
            if (source.absolutePath != stored.path) source.delete()
            stored
        }

    override fun createCameraFile(): String {
        val cameraDir = File(context.cacheDir, CAMERA_DIR).apply { mkdirs() }
        return File(cameraDir, "${UUID.randomUUID()}.jpg").absolutePath
    }

    override suspend fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (file.parentFile?.absolutePath == dir.absolutePath) file.delete()
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            dir.listFiles()?.forEach { it.delete() }
        }
    }

    private fun copy(input: InputStream, mimeType: String): StoredDocumentFile {
        val file = File(dir, "${UUID.randomUUID()}.${extensionFor(mimeType)}")
        file.outputStream().use { output -> input.copyTo(output) }
        if (file.length() > MAX_BYTES) {
            file.delete()
            throw DocumentFileTooLarge()
        }
        return StoredDocumentFile(path = file.absolutePath, mimeType = mimeType)
    }

    private fun mimeFromName(name: String?): String = when {
        name?.endsWith(".pdf", ignoreCase = true) == true -> "application/pdf"
        name?.endsWith(".png", ignoreCase = true) == true -> "image/png"
        name?.endsWith(".webp", ignoreCase = true) == true -> "image/webp"
        else -> "image/jpeg"
    }

    private fun normalizeMime(mime: String): String = when (mime.lowercase()) {
        "image/jpg" -> "image/jpeg"
        else -> mime.lowercase()
    }

    private fun extensionFor(mime: String): String = when (mime) {
        "application/pdf" -> "pdf"
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "jpg"
    }

    companion object {
        const val DIR = "document_files"
        private const val CAMERA_DIR = "document_camera"
        const val MAX_BYTES = 8L * 1024L * 1024L
    }
}
