package market.engine.common

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual fun createSettings(): Settings {
    val delegate = NSUserDefaults(suiteName = "group.application.market.auction-mobile")
    return NSUserDefaultsSettings(delegate)
}
