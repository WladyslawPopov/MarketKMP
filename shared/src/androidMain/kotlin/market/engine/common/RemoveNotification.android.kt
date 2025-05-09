package market.engine.common

var removeNotificationApp : (id: String) -> Unit = {}
actual fun removeNotification(id: String) {
    removeNotificationApp(id)
}
