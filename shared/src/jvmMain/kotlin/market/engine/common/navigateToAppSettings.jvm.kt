package market.engine.common

actual fun navigateToAppSettings() {
    try {
        //mac os
        Runtime.getRuntime().exec("open x-apple.systempreferences:com.apple.preference.notifications")
        //linux
        Runtime.getRuntime().exec("gnome-control-center notifications")
        //windows
        Runtime.getRuntime().exec("cmd /c start ms-settings:notifications")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
