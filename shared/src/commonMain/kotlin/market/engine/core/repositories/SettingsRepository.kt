package market.engine.core.repositories

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Suppress("UNCHECKED_CAST")
class SettingsRepository(private val settings: Settings) {

    private val _themeMode = MutableStateFlow(
        getSettingValue("theme", "") ?: ""
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun updateThemeMode(newMode: String) {
        setSettingValue("theme", newMode)
        _themeMode.value = newMode
    }

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
