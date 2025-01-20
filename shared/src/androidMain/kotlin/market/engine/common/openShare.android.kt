package market.engine.common

import android.content.Intent

actual fun openShare(text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val chooser = Intent.createChooser(sendIntent, null)
    appContext?.startActivity(chooser)
}
