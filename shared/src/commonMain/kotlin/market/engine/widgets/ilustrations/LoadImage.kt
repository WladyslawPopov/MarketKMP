package market.engine.widgets.ilustrations

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoadImage(
    url: String,
    modifier: Modifier,
    isShowLoading: Boolean = true,
    isShowEmpty: Boolean = true,
    rotation: Float = 0f,
    contentScale: ContentScale = ContentScale.Fit,
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(url)
            .crossfade(300)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = null,
        modifier = modifier.rotate(rotation),
        contentScale = contentScale,
        loading = {
            if (isShowLoading){
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = modifier
                ){
                    CircularProgressIndicator(
                        color = colors.inactiveBottomNavIconColor
                    )
                }
            }
        },
        error = {
            if (isShowEmpty) {
                Image(
                    painter = painterResource(drawables.noImageOffer),
                    contentDescription = null,
                    modifier = modifier,
                    contentScale = contentScale
                )
            }
        }
    )
}
