package com.example.my_car.feature.documents.domain

import com.example.my_car.domain.model.DocumentStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

object DocumentStatusRules {

    fun millisToDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

    fun dateToMillis(date: LocalDate): Long =
        date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    fun statusFor(
        expirationDate: Long,
        anticipationDays: Int,
        today: LocalDate = LocalDate.now()
    ): DocumentStatus {
        val expiry = millisToDate(expirationDate)
        return when {
            expiry.isBefore(today) -> DocumentStatus.EXPIRED
            !expiry.isAfter(today.plusDays(anticipationDays.toLong())) -> DocumentStatus.UPCOMING
            else -> DocumentStatus.UP_TO_DATE
        }
    }

    fun daysUntil(expirationDate: Long, today: LocalDate = LocalDate.now()): Long =
        ChronoUnit.DAYS.between(today, millisToDate(expirationDate))
}
