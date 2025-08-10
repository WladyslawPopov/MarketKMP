package market.engine.core.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime

fun nowAsEpochSeconds(): Long {
   return Clock.System.now().epochSeconds
}

fun getMinutesRemainingUntil(endTimestamp: Long): Long? {
    val endInstant = Instant.fromEpochSeconds(endTimestamp)
    val nowInstant = Clock.System.now()

    val duration = endInstant - nowInstant

    if (duration.isNegative()) return null

    val totalMinutes = duration.inWholeMinutes

    return totalMinutes
}

fun Instant.toCurrentSystemLocalDateTime(): LocalDateTime {
    return this.toLocalDateTime(TimeZone.currentSystemDefault())
}

fun Long.convertDateWithMinutes(): String {
    try {
        val instant = Instant.fromEpochSeconds(this)
        val localDateTime = instant.toCurrentSystemLocalDateTime()

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

fun Long.convertHoursAndMinutes(): String {
    try {
        val instant = Instant.fromEpochSeconds(this)
        val localDateTime = instant.toCurrentSystemLocalDateTime()

        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')

        return "$hour:$minute"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

fun Long.convertDateYear(): String{
    try {
        val instant = Instant.fromEpochSeconds(this)
        val localDateTime = instant.toCurrentSystemLocalDateTime()

        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val month = localDateTime.monthNumber.toString().padStart(2, '0')
        val year = localDateTime.year

        return "$day.$month.$year"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

fun Long.convertDateOnlyYear(): String {
    try {
        val instant = Instant.fromEpochSeconds(this)
        val localDateTime = instant.toCurrentSystemLocalDateTime()

        val year = localDateTime.year

        return "$year"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}
