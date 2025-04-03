package market.engine.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import market.engine.core.data.globalData.isBigScreen
import platform.UIKit.UIView

var additionalAuthContent: List<UIView?> = emptyList()

@OptIn(ExperimentalLayoutApi::class)
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
        modifier = Modifier.background(colors.transparent).fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(if(isBigScreen.value) 0.4f else 1f),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            maxItemsInEachRow = 2
        ) {
            additionalAuthContent.forEach { content ->
                if (content != null) {
                    UIKitView(
                        factory = { content },
                        modifier = Modifier
                            .background(colors.transparent)
                            .height(44.dp)
                            .weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }
        }
    }
}
