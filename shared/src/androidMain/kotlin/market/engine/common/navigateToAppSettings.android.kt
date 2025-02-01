package market.engine.common

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

actual fun navigateToAppSettings() {
    val intent = Intent().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, appContext?.packageName)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", appContext?.packageName)
                putExtra("app_uid", appContext?.applicationInfo?.uid)
            }
            else -> {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("package:${appContext?.packageName}")
            }
        }
    }
    appContext?.startActivity(intent)
}
