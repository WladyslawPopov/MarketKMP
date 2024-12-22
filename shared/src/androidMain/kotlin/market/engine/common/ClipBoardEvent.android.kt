package market.engine.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context


actual fun clipBoardEvent(string: String) {
    if (string.isEmpty()) return
    if (appContext == null) return
    val clipboard = appContext!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("offerId", string)
    clipboard.setPrimaryClip(clip)
}

