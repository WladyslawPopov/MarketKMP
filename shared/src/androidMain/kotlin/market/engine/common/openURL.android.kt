package market.engine.common

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Patterns
import androidx.core.net.toUri

actual fun openUrl(url: String) {
    if (url.isNotBlank() && Patterns.WEB_URL.matcher(url).matches()) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            appContext?.startActivity(intent)
        } catch (_: ActivityNotFoundException) {}
    }
}
