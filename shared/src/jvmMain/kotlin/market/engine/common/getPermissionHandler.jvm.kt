package market.engine.common

actual fun getPermissionHandler(): PermissionHandler {
    return DesktopPermissionHandler()
}

class DesktopPermissionHandler : PermissionHandler {
    override fun AskPermissionNotification() {
        // No-op for Desktop, as notifications permissions are not applicable.
        println("askPermissionNotification() is not implemented on Desktop.")
    }
}
