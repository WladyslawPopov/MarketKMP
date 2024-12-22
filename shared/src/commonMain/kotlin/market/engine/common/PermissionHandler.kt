package market.engine.common

interface PermissionHandler {
    fun askPermissionNotification()
    fun requestImagePermissions(onPermissionResult: (Boolean) -> Unit)
    fun checkImagePermissions(): Boolean
}
