package market.engine.widgets.exceptions

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
    images: List<String>,
    pagerState: PagerState,
) {
    Box(
        modifier = Modifier.background(colors.transparentGrayColor, MaterialTheme.shapes.small)
            .padding(dimens.smallPadding),
        contentAlignment = Alignment.Center
    ){
        HorizontalPager(
            pageSize = PageSize.Fill,
            state = pagerState,
            snapPosition = SnapPosition.Center,
        ) { index ->
            val imageLoadFailed = remember { mutableStateOf(false) }
            val loading = remember { mutableStateOf(true) }
            val imageUrl = images[index]

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (imageLoadFailed.value || imageUrl.isEmpty()){
                    getImage(imageUrl)
                }else {
                    if (loading.value) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator(
                                color = colors.inactiveBottomNavIconColor
                            )
                        }
                    }

                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                        imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
                            .crossfade(true)
                            .components {
                                add(SvgDecoder.Factory())
                            }.build(),
                        onSuccess = {
                            loading.value = false
                            printLogD("Coil success", imageUrl)
                        },
                        onError = {
                            imageLoadFailed.value = true
                            printLogD("Coil Error", it.result.throwable.message)
                        },
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = dimens.smallPadding)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        color = colors.white.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .align(Alignment.Center)
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
                                    colors.inactiveBottomNavIconColor,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}
