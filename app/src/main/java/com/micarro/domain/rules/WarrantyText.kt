package com.micarro.domain.rules

/**
 * Lee la garantía guardada como texto ("6 meses / 10000 km") o como un número de meses.
 */
object WarrantyText {
    fun months(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        raw.trim().toLongOrNull()?.let { return it }
        return Regex("(\\d+)\\s*mes", RegexOption.IGNORE_CASE).find(raw)
            ?.groupValues
            ?.getOrNull(1)
            ?.toLongOrNull()
    }

    fun kilometers(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        return Regex("(\\d+)\\s*km", RegexOption.IGNORE_CASE).find(raw)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
    }
}
