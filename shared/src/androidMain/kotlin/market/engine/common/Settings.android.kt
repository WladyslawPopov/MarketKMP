package market.engine.common

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createSettings(): Settings {
    return SharedPreferencesSettings.Factory(appContext!!).create(name = "settings_market")
}
