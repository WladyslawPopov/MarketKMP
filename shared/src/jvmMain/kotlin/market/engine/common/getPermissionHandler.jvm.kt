package market.engine.common

actual fun getPermissionHandler(): PermissionHandler {
    return DesktopPermissionHandler()
}

class DesktopPermissionHandler : PermissionHandler {
    override fun askPermissionNotification() {
        // No-op for Desktop, as notifications permissions are not applicable.
        println("askPermissionNotification() is not implemented on Desktop.")
    }

    override fun requestImagePermissions(onPermissionResult: (Boolean) -> Unit) {
        onPermissionResult(true)
    }

    override fun checkImagePermissions(): Boolean {
        return true
    }
}
