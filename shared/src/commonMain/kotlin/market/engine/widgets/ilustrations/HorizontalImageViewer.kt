package market.engine.widgets.ilustrations

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.utils.getImage
import market.engine.core.utils.printLogD

@Composable
fun HorizontalImageViewer(
    images: List<String?>,
    pagerState: PagerState,
    isUpdate: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (isUpdate) images.size
    Column(
        modifier = Modifier
            .background(colors.transparentGrayColor, MaterialTheme.shapes.small)
            .padding(dimens.smallPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalPager(
            pageSize = PageSize.Fill,
            state = pagerState,
            snapPosition = SnapPosition.Center,
            modifier = modifier.weight(1f)
        ) { index ->
            val imageLoadFailed = remember { mutableStateOf(false) }
            val loading = remember { mutableStateOf(true) }

            Box(
                modifier = Modifier.fillMaxSize().padding(dimens.smallPadding),
                contentAlignment = Alignment.Center
            ) {
                if (imageLoadFailed.value) {
                    getImage(images[index])
                } else {
                    if (loading.value) {
                        CircularProgressIndicator(
                            color = colors.inactiveBottomNavIconColor
                        )
                    }

                    AsyncImage(
                        model = images[index],
                        contentDescription = null,
                        modifier = modifier
                            .fillMaxWidth(),
                        imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
                            .crossfade(true)
                            .components {
                                add(SvgDecoder.Factory())
                            }.build(),
                        onSuccess = {
                            loading.value = false
                            printLogD("Coil success", images[index])
                        },
                        onError = {
                            imageLoadFailed.value = true
                            printLogD("Coil Error", it.result.throwable.message)
                        },
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .background(
                    color = colors.white.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            repeat(pagerState.pageCount) { page ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = dimens.extraSmallPadding)
                        .size(if (pagerState.currentPage != page) 6.dp else 8.dp)
                        .background(
                            color = if (pagerState.currentPage != page)
                                colors.textA0AE
                            else
                                colors.actionItemColors,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
