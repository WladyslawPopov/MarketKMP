package market.engine.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import market.engine.core.data.globalData.AuthManager

lateinit var viewAdditionalAuthorizationContent : @Composable () -> Unit

@Composable
actual fun additionalAuthorizationContent(onSuccess: (HashMap<String, String>) -> Unit) {
    if (::viewAdditionalAuthorizationContent.isInitialized) {
        DisposableEffect(Unit) {
            AuthManager.onAuthSuccess = { map ->
                val hmap = HashMap<String, String>()
                hmap.putAll(map)
                onSuccess(hmap)
            }
            onDispose {
                AuthManager.onAuthSuccess = null
            }
        }

        viewAdditionalAuthorizationContent()
    }
}
