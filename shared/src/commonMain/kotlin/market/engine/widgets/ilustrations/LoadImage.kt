package market.engine.widgets.ilustrations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.utils.getImage
import market.engine.core.utils.printLogD

@Composable
fun LoadImage(
    url: String,
    isShowLoading: Boolean = true,
    isShowEmpty: Boolean = true,
    size: Dp,
    rotation: Float = 0f,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val imageLoadFailed = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(true) }

    if (imageLoadFailed.value){
        getImage(url, size, isShowEmpty)
    }else {
        if (isLoading.value && isShowLoading){
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(size)
            ){
                CircularProgressIndicator(
                    color = colors.inactiveBottomNavIconColor
                )
            }
        }
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier.size(size).rotate(rotation),
            imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
                .crossfade(true)
                .components {
                add(SvgDecoder.Factory())
            }.build(),
            contentScale = contentScale,
            onSuccess = {
                isLoading.value = false
                imageLoadFailed.value = false
            },
            onError = {
                printLogD("Coil Error", it.result.throwable.message)
                imageLoadFailed.value = true
            },
        )
    }
}
