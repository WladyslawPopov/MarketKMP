package market.engine.widgets.ilustrations

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun FullScreenImageViewer(
    pagerFullState: PagerState,
    images: List<String>,
    isUpdate: Boolean = false,
) {
    val zoomState = rememberZoomState()

    Box(
        modifier = Modifier
            .animateContentSize()
            .background(colors.grayLayout.copy(alpha = 0.8f))
    ) {
        HorizontalImageViewer(
            images = images,
            pagerState = pagerFullState,
            isUpdate = isUpdate,
            modifier = Modifier.fillMaxSize().zoomable(
                zoomState,
                onDoubleTap = { position ->
                    val targetScale = when {
                        zoomState.scale < 2f -> 2f
                        zoomState.scale < 4f -> 4f
                        else -> 1f
                    }
                    zoomState.changeScale(targetScale, position)
                }
            )
        )
    }
}


