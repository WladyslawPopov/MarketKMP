package market.engine.core.utils


import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun getCurrentDate(): String {
    val currentInstant: Instant = Clock.System.now()
    val currentTimeInSeconds = currentInstant.epochSeconds
    return currentTimeInSeconds.toString()
}


fun String.convertDateWithMinutes(): String {
    try {
        // Validate that the input date string is not empty
        if (this.isEmpty()) {
            return ""
        }

        val instant = Instant.fromEpochSeconds(this.toLongOrNull() ?: this.toDouble().toLong())
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val month = localDateTime.monthNumber.toString().padStart(2, '0')
        val year = localDateTime.year
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')

        return "$day.$month.$year $hour:$minute"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

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

fun String.convertDateYear(): String{
    try {
        // Validate that the input date string is not empty
        if (this.isEmpty()) {
            return ""
        }

        val instant = Instant.fromEpochSeconds(this.toLong())
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val month = localDateTime.monthNumber.toString().padStart(2, '0')
        val year = localDateTime.year

        return "$day.$month.$year"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

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



