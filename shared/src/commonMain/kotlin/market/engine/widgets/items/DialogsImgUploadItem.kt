package market.engine.widgets.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.items.PhotoTemp
import market.engine.widgets.buttons.SmallIconButton

@Composable
fun DialogsImgUploadItem(
    item : PhotoTemp,
    delete : () -> Unit,
) {
    Card(
        modifier = Modifier.size(125.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.padding(dimens.smallPadding)
                    .align(Alignment.TopEnd)
                    .zIndex(5f)
            ) {
                SmallIconButton(
                    drawables.cancelIcon,
                    color = colors.negativeRed,
                    modifierIconSize = Modifier.size(dimens.smallIconSize),
                    modifier = Modifier.size(dimens.smallIconSize)
                ) {
                    delete()
                }
            }

            AsyncImage(
                model = item.uri ?: item.url ?: "",
                contentDescription = null,
                imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
                    .crossfade(true)
                    .components {
                        add(SvgDecoder.Factory())
                    }.build(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
