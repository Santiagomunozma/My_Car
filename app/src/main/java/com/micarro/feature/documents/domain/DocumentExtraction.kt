package com.micarro.feature.documents.domain

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.micarro.domain.model.DocumentType
import java.time.LocalDate

data class DocumentExtraction(
    val type: DocumentType? = null,
    val expirationDate: LocalDate? = null,
    val issuer: String? = null,
    val referenceNumber: String? = null,
    val plate: String? = null,
    val name: String? = null
)

object DocumentExtractionParser {
    private val gson = Gson()

    fun parse(raw: String): DocumentExtraction {
        val json = extractObject(raw) ?: return DocumentExtraction()
        val obj = runCatching { gson.fromJson(json, JsonObject::class.java) }.getOrNull()
            ?: return DocumentExtraction()
        return DocumentExtraction(
            type = obj.text("type")?.let { token ->
                runCatching { DocumentType.valueOf(token.uppercase()) }.getOrNull()
            },
            expirationDate = obj.text("expirationDate")?.let { value ->
                runCatching { LocalDate.parse(value) }.getOrNull()
            },
            issuer = obj.text("issuer"),
            referenceNumber = obj.text("referenceNumber"),
            plate = obj.text("plate"),
            name = obj.text("name")
        )
    }

    fun responseText(body: String): String? = runCatching {
        val root = JsonParser.parseString(body).asJsonObject
        val parts = root.getAsJsonArray("candidates")
            ?.firstOrNull()
            ?.asJsonObject
            ?.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?: return null
        parts.mapNotNull { element ->
            val text = element.asJsonObject.get("text") ?: return@mapNotNull null
            if (text.isJsonNull) null else text.asString
        }.joinToString("\n").ifBlank { null }
    }.getOrNull()

    private fun JsonObject.text(key: String): String? {
        val element = get(key) ?: return null
        if (!element.isJsonPrimitive || element.isJsonNull) return null
        val value = element.asString.trim()
        if (value.isEmpty() || value.equals("null", ignoreCase = true)) return null
        return value
    }

    private fun extractObject(raw: String): String? {
        val trimmed = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return trimmed.substring(start, end + 1)
    }
}

object PlateMatch {
    fun normalize(plate: String): String = plate.uppercase().filter { it.isLetterOrDigit() }

    fun matches(extracted: String?, vehiclePlate: String): Boolean {
        val left = extracted?.let(::normalize).orEmpty()
        if (left.isEmpty()) return true
        return left == normalize(vehiclePlate)
    }
}
