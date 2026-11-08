package com.micarro.feature.assistant.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.micarro.BuildConfig
import com.micarro.feature.documents.domain.DocumentExtractionParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

data class AssistantTurn(val fromUser: Boolean, val text: String)

@Singleton
class GeminiAssistantClient @Inject constructor() {

    val isConfigured: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotBlank()

    suspend fun reply(history: List<AssistantTurn>, context: String, question: String): String =
        withContext(Dispatchers.IO) {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isBlank()) throw IllegalStateException("Falta la clave de Gemini")

            val contents = JsonArray()
            history.takeLast(8).forEach { turn ->
                contents.add(message(if (turn.fromUser) "user" else "model", turn.text))
            }
            val asked = buildString {
                appendLine("Datos registrados en la app para este vehículo:")
                appendLine(context)
                appendLine()
                append("Pregunta del conductor: ")
                append(question)
            }
            contents.add(message("user", asked))

            val payload = JsonObject().apply {
                add("systemInstruction", JsonObject().apply {
                    add("parts", JsonArray().apply {
                        add(JsonObject().apply { addProperty("text", SYSTEM) })
                    })
                })
                add("contents", contents)
                add("generationConfig", JsonObject().apply {
                    addProperty("temperature", 0.4)
                })
            }.toString()

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
                if (code !in 200..299) throw IOException("No se pudo consultar al asistente")
                DocumentExtractionParser.responseText(body)
                    ?: throw IOException("La respuesta no trae texto")
            } finally {
                connection.disconnect()
            }
        }

    private fun message(role: String, text: String): JsonObject = JsonObject().apply {
        addProperty("role", role)
        add("parts", JsonArray().apply {
            add(JsonObject().apply { addProperty("text", text) })
        })
    }

    companion object {
        private const val ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

        private val SYSTEM = """
            Eres el asistente mecánico de My Car. Respondes en español, claro y breve, sobre el vehículo del conductor.
            Usa el historial de servicios y repuestos que viene en la pregunta. Si un repuesto o una reparación reciente explica el síntoma, dilo y descártalo como causa principal. Si no hay registro, no inventes que se cambió.
            Puedes orientar causas probables, qué revisar primero y si conviene taller.
            Solo da pasos para hacerlo uno mismo cuando sea sencillo y seguro: niveles, presión de llantas, fusibles, luces o una lectura del tablero.
            No des instrucciones para desarmar frenos, dirección, suspensión, airbags, combustible ni el sistema eléctrico.
            Si el síntoma puede ser de frenos, dirección, humo, pérdida de líquido o un olor a quemado, recomienda no circular y llevarlo a un taller.
            Recuerda que es una orientación, no un diagnóstico de taller. No inventes precios, horarios ni teléfonos de talleres.
        """.trimIndent()
    }
}
