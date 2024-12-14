package market.engine.widgets.exceptions

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.globalData.ThemeResources.colors

@Composable
fun FullScreenImageViewer(
    pagerFullState: PagerState,
    images: List<String>,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
            .background(colors.grayLayout.copy(alpha = 0.8f))
    ) {
        HorizontalImageViewer(
            images = images,
            pagerState = pagerFullState
        )
    }
}
