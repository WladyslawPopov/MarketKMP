package market.engine.common

import platform.Foundation.NSURL
import platform.UIKit.UIApplication


actual fun openUrl(url: String) {
    val nsUrl = NSURL(string = url)
    if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
        UIApplication.sharedApplication.openURL(
            nsUrl,
            options = emptyMap<Any?, Any>(),
            completionHandler = { success ->
                if (!success) {
                    println("Could not open URL: $url")
                }
            }
        )
    } else {
        println("URL not supported: $url")
    }
}
