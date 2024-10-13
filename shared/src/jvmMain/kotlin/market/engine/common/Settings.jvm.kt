package market.engine.common

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings

actual fun createSettings(): Settings {
    return PreferencesSettings.Factory().create(name = "settings_market")
}
