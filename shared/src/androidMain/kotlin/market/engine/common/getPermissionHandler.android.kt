package market.engine.common

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import market.engine.core.repositories.SettingsRepository
import org.koin.mp.KoinPlatform.getKoin

actual fun getPermissionHandler(): PermissionHandler {
    return PermissionHandlerImpl()
}

var requestPermissionLauncher : ActivityResultLauncher<String>? = null
class PermissionHandlerImpl : PermissionHandler {
    private val settingsHelper : SettingsRepository = getKoin().get()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun AskPermissionNotification() {
        if (ContextCompat.checkSelfPermission(
                appContext,
                POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val isPermissionRequested = settingsHelper.getSettingValue("isPermissionRequested", false) ?: false
            if (!isPermissionRequested) {
                requestPermissionLauncher?.launch(POST_NOTIFICATIONS)
            }
        }
    }
}
