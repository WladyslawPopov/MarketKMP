package market.engine.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import market.engine.core.data.globalData.AuthManager
import market.engine.core.data.globalData.ThemeResources.colors
import platform.UIKit.UIView

var additionalAuthContent: UIView? = null

@Composable
actual fun additionalAuthorizationContent(onSuccess: (HashMap<String, String>) -> Unit) {
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

    if (additionalAuthContent != null) {
        Box(
            modifier = Modifier.background(colors.transparent).fillMaxSize()
        ) {
            UIKitView(
                factory = { additionalAuthContent!! },
                modifier = Modifier
                    .background(colors.transparent)
                    .height(46.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
            )
        }
    }
}
