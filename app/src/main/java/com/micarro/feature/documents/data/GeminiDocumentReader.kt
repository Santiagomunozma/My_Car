package com.micarro.feature.documents.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.micarro.BuildConfig
import com.micarro.feature.documents.data.LocalDocumentFileStore.Companion.MAX_BYTES
import com.micarro.feature.documents.domain.DocumentExtraction
import com.micarro.feature.documents.domain.DocumentExtractionParser
import com.micarro.feature.documents.domain.DocumentFileTooLarge
import com.micarro.feature.documents.domain.DocumentReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiDocumentReader @Inject constructor() : DocumentReader {

    override val isConfigured: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotBlank()

    override suspend fun read(path: String, mimeType: String): DocumentExtraction =
        withContext(Dispatchers.IO) {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isBlank()) throw IllegalStateException("Falta la clave de Gemini")
            val file = File(path)
            if (!file.isFile) throw IOException("No se encontró el archivo")
            if (file.length() > MAX_BYTES) throw DocumentFileTooLarge()

            val payload = buildBody(
                mimeType = mimeType,
                data = Base64.getEncoder().encodeToString(file.readBytes())
            )
            val endpoint = URL(
                "$ENDPOINT?key=${URLEncoder.encode(key, Charsets.UTF_8.name())}"
            )
            val connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 20_000
                readTimeout = 60_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            try {
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) throw IOException("No se pudo leer el documento")
                val text = DocumentExtractionParser.responseText(body)
                    ?: throw IOException("La respuesta no trae datos")
                DocumentExtractionParser.parse(text)
            } finally {
                connection.disconnect()
            }
        }

    private fun buildBody(mimeType: String, data: String): String {
        val inline = JsonObject().apply {
            addProperty("mime_type", mimeType)
            addProperty("data", data)
        }
        val parts = JsonArray().apply {
            add(JsonObject().apply { addProperty("text", PROMPT) })
            add(JsonObject().apply { add("inline_data", inline) })
        }
        val contents = JsonArray().apply {
            add(JsonObject().apply { add("parts", parts) })
        }
        val config = JsonObject().apply {
            addProperty("temperature", 0)
            addProperty("responseMimeType", "application/json")
        }
        return JsonObject().apply {
            add("contents", contents)
            add("generationConfig", config)
        }.toString()
    }

    companion object {
        private const val ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

        private val PROMPT = """
            Extrae datos de un documento vehicular de Colombia. Puede ser SOAT, revisión técnico-mecánica, impuesto vehicular, semaforización, seguro u otro.
            Responde solo JSON con estas claves:
            type: SOAT, TECHNICAL_INSPECTION, TAX, TRAFFIC_LIGHT, INSURANCE u OTHER
            expirationDate: fecha de vencimiento o límite de pago en formato YYYY-MM-DD, o null
            issuer: entidad que expide el documento, o null
            referenceNumber: número de póliza, certificado o recibo, o null
            plate: placa del vehículo, o null
            name: nombre corto del documento, o null
            Si un dato no está visible, usa null. No inventes fechas ni placas.
        """.trimIndent()
    }
}
