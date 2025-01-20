// desktopMain
package market.engine.common

import java.awt.Desktop
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

actual fun openCalendarEvent(
    description: String
) {
    val icsContent = buildIcs(
        description = description,
        startDate = LocalDateTime.now(),
        endDate = LocalDateTime.now().plusHours(1),
    )

    val tempFile = File.createTempFile("event", ".ics")
    tempFile.writeText(icsContent)
    tempFile.deleteOnExit()

    try {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(tempFile)
        } else {
            println("Desktop API not supported")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun buildIcs(
    description: String,
    startDate: LocalDateTime,
    endDate: LocalDateTime,
): String {
    val dtFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")

    val zStart = ZonedDateTime.of(startDate, ZoneId.systemDefault())
    val zEnd = ZonedDateTime.of(endDate, ZoneId.systemDefault())

    val dtStart = dtFormatter.format(zStart)
    val dtEnd = dtFormatter.format(zEnd)
    val safeDescription = description.replace("\n", "\\n")

    return """
        BEGIN:VCALENDAR
        VERSION:2.0
        BEGIN:VEVENT
        UID:${System.currentTimeMillis()}@myapp
        DTSTAMP:${dtFormatter.format(ZonedDateTime.now())}
        DTSTART:$dtStart
        DTEND:$dtEnd
        DESCRIPTION:$safeDescription
        END:VEVENT
        END:VCALENDAR
    """.trimIndent()
}
