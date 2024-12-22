package market.engine.common

import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.repositories.SettingsRepository
import org.koin.mp.KoinPlatform.getKoin
import platform.Photos.PHAccessLevelReadWrite
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.UserNotifications.UNUserNotificationCenter

actual fun getPermissionHandler(): PermissionHandler {
    return IosPermissionHandler()
}

class IosPermissionHandler : PermissionHandler {
    private val settingsHelper : SettingsRepository = getKoin().get()
    private val analyticsHelper : AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()

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

    override fun askPermissionNotification() {
        requestNotificationPermission { granted ->
            settingsHelper.setSettingValue("isPermissionRequested", true)
            val userProfileAttributes = mapOf(
                "notificationsEnabled" to granted
            )
            analyticsHelper.updateUserProfile(userProfileAttributes)
        }
    }

    override fun requestImagePermissions(onPermissionResult: (Boolean) -> Unit) {
        PHPhotoLibrary.requestAuthorization { status ->
            when (status) {
                PHAuthorizationStatusAuthorized,
                PHAuthorizationStatusLimited -> onPermissionResult(true)
                else -> onPermissionResult(false)
            }
        }
    }

    override fun checkImagePermissions(): Boolean {
        val status = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelReadWrite)
        return (status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited)
    }
}
