package market.engine.widgets.ilustrations

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun FullScreenImageViewer(
    pagerFullState: PagerState,
    images: List<String>,
    isUpdate: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
            .background(colors.grayLayout.copy(alpha = 0.8f))
    ) {
        HorizontalImageViewer(
            images = images,
            pagerState = pagerFullState,
            isUpdate
        )
    }
}
