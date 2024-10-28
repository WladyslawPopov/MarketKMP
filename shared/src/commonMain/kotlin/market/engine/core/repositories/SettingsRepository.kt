package market.engine.core.repositories

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

@Suppress("UNCHECKED_CAST")
class SettingsRepository(private val settings: Settings ) {
    fun <T>setSettingValue(key: String, value: T) {
        when (value) {
            is String -> settings[key] = value
            is Int -> settings[key] = value
            is Long -> settings[key] = value
            is Float -> settings[key] = value
            is Boolean -> settings[key] = value
            is Double -> settings[key] = value
        }
    }
    fun <T>getSettingValue(key: String, defaultValue: T): T? {
        return when(defaultValue){
            is String -> settings[key, defaultValue] as? T
            is Int -> settings[key, defaultValue] as? T
            is Long -> settings[key, defaultValue] as? T
            is Float -> settings[key, defaultValue] as? T
            is Boolean -> settings[key, defaultValue] as? T
            is Double -> settings[key, defaultValue] as? T
            else -> null
        }
    }
}
