package market.engine.common

import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.repositories.SettingsRepository
import org.koin.mp.KoinPlatform.getKoin
import platform.UserNotifications.UNUserNotificationCenter

actual fun getPermissionHandler(): PermissionHandler {
    return IosPermissionHandler()
}

class IosPermissionHandler : PermissionHandler {
    private val settingsHelper : SettingsRepository = getKoin().get()
    private val analyticsHelper : AnalyticsHelper = getKoin().get()

    private fun requestNotificationPermission(callback: (Boolean) -> Unit) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val isPermissionRequested = settingsHelper.getSettingValue("isPermissionRequested", false) ?: false
        if (!isPermissionRequested) {
            center.requestAuthorizationWithOptions(
                options = 1UL or 2UL or 4UL
            ) { granted, error ->
                if (error != null) {
                    println("Error requesting notification permissions: $error")
                    callback(false)
                } else {
                    callback(granted)
                }
            }
        }
    }

    override fun AskPermissionNotification() {
        requestNotificationPermission { granted ->
            settingsHelper.setSettingValue("isPermissionRequested", true)
            val userProfileAttributes = mapOf(
                "notificationsEnabled" to granted
            )
            analyticsHelper.updateUserProfile(userProfileAttributes)
        }
    }
}
