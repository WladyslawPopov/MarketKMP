package market.engine.core.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun getCurrentDate(): String {
    val currentInstant: Instant = Clock.System.now()
    val currentTimeInSeconds = currentInstant.epochSeconds
    return currentTimeInSeconds.toString()
}

@OptIn(ExperimentalTime::class)
fun getRemainingMinutesTime(endTimestamp: Long): Long {
    val endInstant = Instant.fromEpochSeconds(endTimestamp)
    val nowInstant = Clock.System.now()

    val duration = endInstant - nowInstant

    if (duration.isNegative()) return 1

    val totalMinutes = duration.inWholeMinutes

    return totalMinutes
}


@OptIn(ExperimentalTime::class)
fun String.convertDateWithMinutes(): String {
    try {
        // Validate that the input date string is not empty
        if (this.isEmpty()) {
            return ""
        }

        val instant = Instant.fromEpochSeconds(this.toLongOrNull() ?: this.toDouble().toLong())
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val day = localDateTime.day.toString().padStart(2, '0')
        val month = localDateTime.month.number.toString().padStart(2, '0')
        val year = localDateTime.year
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')

        return "$day.$month.$year $hour:$minute"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

@OptIn(ExperimentalTime::class)
fun String.convertHoursAndMinutes(): String {
    try {
        // Validate that the input date string is not empty
        if (this.isEmpty()) {
            return ""
        }

        val instant = Instant.fromEpochSeconds(this.toLongOrNull() ?: this.toDouble().toLong())
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')

        return "$hour:$minute"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

@OptIn(ExperimentalTime::class)
fun String.convertDateYear(): String{
    try {
        // Validate that the input date string is not empty
        if (this.isEmpty() || this == "null") {
            return ""
        }

        val instant = Instant.fromEpochSeconds(this.toLong())
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val day = localDateTime.day.toString().padStart(2, '0')
        val month = localDateTime.month.number.toString().padStart(2, '0')
        val year = localDateTime.year

        return "$day.$month.$year"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

@OptIn(ExperimentalTime::class)
fun String.convertDateOnlyYear(): String {
    try {
        // Validate that the input date string is not empty
        if (this.isEmpty()) {
            return ""
        }

        val instant = Instant.fromEpochSeconds(this.toLong())
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val year = localDateTime.year

        return "$year"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}



