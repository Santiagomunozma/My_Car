package com.micarro.domain.model

object EvidenceCodec {
    fun decode(raw: String?): List<String> =
        raw?.split('\n')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()

    fun encode(paths: List<String>): String? =
        paths.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString("\n").ifBlank { null }
}
