package market.engine.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import market.engine.core.data.globalData.AuthManager
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import platform.UIKit.UIView

var additionalAuthContent: List<UIView?> = emptyList()

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

    Box(
        modifier = Modifier.background(colors.transparent).fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            additionalAuthContent.forEach { content ->
                if (content != null) {
                    UIKitView(
                        factory = { content },
                        modifier = Modifier
                            .background(colors.transparent)
                            .height(44.dp)
                            .fillMaxWidth(0.7f)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }
        }
    }
}
