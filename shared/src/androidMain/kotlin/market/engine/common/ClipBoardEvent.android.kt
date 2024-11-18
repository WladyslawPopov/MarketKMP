package market.engine.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context


actual fun clipBoardEvent(string: String) {
    val context = appContext
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("offerId", string)
        clipboard.setPrimaryClip(clip)
}

