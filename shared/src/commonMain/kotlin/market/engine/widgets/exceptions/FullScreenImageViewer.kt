package market.engine.widgets.exceptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.widgets.buttons.SmallIconButton

@Composable
fun FullScreenImageViewer(
    images: List<String>,
    initialIndex: Int = 0,
    onClose: (Int) -> Unit
) {
    val pagerFullState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { images.size },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.grayLayout.copy(alpha = 0.8f))
    ) {
        HorizontalImageViewer(
            images = images,
            pagerState = pagerFullState
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(colors.white, shape = CircleShape)
                .padding(dimens.smallPadding),
            contentAlignment = Alignment.Center
        ) {
            SmallIconButton(
                drawables.closeBtn,
                color = colors.black
            ){
                onClose(pagerFullState.currentPage)
            }
        }
    }
}
