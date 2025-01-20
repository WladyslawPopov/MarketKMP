package market.engine.common

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openEmail(email: String) {
    val url = NSURL.URLWithString("mailto:$email")
    if (url != null) {
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
}
