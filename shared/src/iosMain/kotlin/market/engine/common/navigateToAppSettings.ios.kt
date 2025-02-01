package market.engine.common

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

actual fun navigateToAppSettings() {
    val urlString = UIApplicationOpenSettingsURLString
    val url = NSURL.URLWithString(urlString) ?: return
    if (!UIApplication.sharedApplication.canOpenURL(url)) return
    UIApplication.sharedApplication.openURL(
        url,
        options = emptyMap<Any?, Any>(),
        completionHandler = { success ->
            if (!success) {
                println("Could not open URL: $url")
            }
        }
    )
}
