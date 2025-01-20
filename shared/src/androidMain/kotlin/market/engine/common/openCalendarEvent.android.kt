package market.engine.common

import android.content.Intent
import android.provider.CalendarContract

actual fun openCalendarEvent(description: String) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.DESCRIPTION, description)
    }
    appContext?.startActivity(intent)
}
