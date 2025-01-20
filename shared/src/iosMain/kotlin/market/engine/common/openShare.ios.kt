package market.engine.common

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual fun openShare(text: String) {
    val items = mutableListOf<Any>()
    items.add(text)

    val controller = UIActivityViewController(
        activityItems = items.toList(),
        applicationActivities = null
    )

    val window = UIApplication.sharedApplication.keyWindow
    val rootVC = window?.rootViewController

    rootVC?.presentViewController(controller, animated = true, completion = null)
}
