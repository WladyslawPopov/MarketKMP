package market.engine.common

import android.Manifest
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import market.engine.core.repositories.SettingsRepository
import org.koin.mp.KoinPlatform.getKoin

actual fun getPermissionHandler(): PermissionHandler {
    return PermissionHandlerImpl()
}
const val PERMISSION_REQUEST_CODE = 101

var requestPermissionLauncher : ActivityResultLauncher<String>? = null

class PermissionHandlerImpl : PermissionHandler {
    private var onPermissionResult: ((Boolean) -> Unit)? = null
    private val settingsHelper : SettingsRepository = getKoin().get()

    private val activity = appContext as Activity

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun askPermissionNotification() {
        if (ContextCompat.checkSelfPermission(
                activity,
                POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val isPermissionRequested = settingsHelper.getSettingValue("isPermissionRequested", false) ?: false
            if (!isPermissionRequested) {
                requestPermissionLauncher?.launch(POST_NOTIFICATIONS)
            }
        }
    }

    override fun requestImagePermissions(onPermissionResult: (Boolean) -> Unit) {
        this.onPermissionResult = onPermissionResult

        val permission = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        // If already granted, callback immediately
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            onPermissionResult(true)
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }

    override fun checkImagePermissions(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }
}
