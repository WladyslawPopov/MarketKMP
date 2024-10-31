package market.engine.common

actual fun notificationIdentifier(id: Long) {
    identifierMindBox(id)
}

var identifierMindBox : (Long) -> Unit = {}
