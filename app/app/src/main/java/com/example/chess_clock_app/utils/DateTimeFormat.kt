package com.example.chess_clock_app.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_TIME = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")

// 1760450700000 -> "14 Oct 2026, 16:05" in the phone's time zone
fun formatDateTime(millis: Long): String =
    DATE_TIME.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
