package market.engine.common

import market.engine.core.network.networkObjects.Conversations

var shortcutDialog : (Conversations) -> Unit = { _->}

actual fun setShortcutForDialog(data: Conversations) {
    shortcutDialog(data)
}
