package com.example.chess_clock_app.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_TIME = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")
private val DATE = DateTimeFormatter.ofPattern("d MMM yyyy")

// 1760450700000 -> "14 Oct 2026, 16:05" in the phone's time zone
fun formatDateTime(millis: Long): String =
    DATE_TIME.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

// 2026-10-14 -> "14 Oct 2026"
fun formatDate(date: LocalDate): String = DATE.format(date)
