package market.engine.widgets.grids

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.MesImage

@Composable
fun ImagesPreviewGrid(
    images: List<MesImage>,
    openImage: (Int) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(if(images.size < 3) images.size else 3),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .heightIn(max = 1200.dp)
            .widthIn(max = 400.dp),
        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
        verticalItemSpacing = dimens.extraSmallPadding
    ) {
        items(images.size) { index ->
            val image = images[index]
            AsyncImage(
                model = image.thumbUrl ?: "",
                contentDescription = null,
                imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
                    .crossfade(true)
                    .components {
                        add(SvgDecoder.Factory())
                    }.build(),
                Modifier.clickable {
                    openImage(index)
                },
                contentScale = ContentScale.Crop
            )
        }
    }
}
