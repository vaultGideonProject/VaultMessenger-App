package com.vaultmessenger.modules

import android.os.Build
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.ZoneId

fun formatTimestamp(isoString: String): String {
    // Parse the ISO 8601 timestamp
    val timestamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ZonedDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME)
    } else {
        return isoString
    }

    // Get the current time in UTC or your desired timezone
    val now = ZonedDateTime.now(ZoneId.of("UTC"))

    // Calculate the difference in days between the current time and the timestamp
    val daysDifference = ChronoUnit.DAYS.between(timestamp.toLocalDate(), now.toLocalDate())

    return when (daysDifference) {
        0L -> {
            // Show as time in 24-hour format if it's today
            timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        1L -> {
            // Show "Yesterday" if it's the previous day
            "Yesterday"
        }
        in 2L..6L -> {
            // Show as day of the week for messages within the last week
            timestamp.format(DateTimeFormatter.ofPattern("EEEE"))
        }
        else -> {
            // Show as day and month for older messages
            timestamp.format(DateTimeFormatter.ofPattern("dd MMM"))
        }
    }
}

