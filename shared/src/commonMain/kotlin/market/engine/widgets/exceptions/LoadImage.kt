package market.engine.widgets.exceptions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.util.getImage
import market.engine.core.util.printLogD

@Composable
fun LoadImage(
    url: String,
    isShowLoading: Boolean = true,
    isShowEmpty: Boolean = true,
    size: Dp,
    modifier: Modifier = Modifier
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
            modifier = modifier.size(size),
            imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
                .crossfade(true)
                .components {
                add(SvgDecoder.Factory())
            }.build(),
            onSuccess = {
                isLoading.value = false
                imageLoadFailed.value = false
                printLogD("Coil success", url)
            },
            onError = {
                printLogD("Coil Error", it.result.throwable.message)
                imageLoadFailed.value = true
            },
        )
    }
}
