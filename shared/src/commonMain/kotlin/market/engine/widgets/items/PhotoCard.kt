package market.engine.widgets.items

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.items.PhotoSave
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.ilustrations.LoadImage

@Composable
fun PhotoCard(
    item: PhotoSave,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    openPhoto: (PhotoSave) -> Unit,
    rotatePhoto: (PhotoSave) -> Unit,
    setDeleteImages: (PhotoSave) -> Unit,
) {
    val rotate = remember { mutableStateOf(item.rotate) }

    val isLoading = remember(item.tempId) { mutableStateOf(item.tempId == null) }

    Card(
        onClick = { openPhoto(item) },
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ){
            if(item.tempId?.isNotBlank() == true) {
                Row(
                    modifier = Modifier.padding(dimens.smallPadding)
                        .align(Alignment.TopStart)
                        .zIndex(5f)
                ) {
                    SmallIconButton(
                        drawables.recycleIcon,
                        color = colors.negativeRed,
                        modifierIconSize = Modifier.size(dimens.smallIconSize),
                        modifier = Modifier
                            .size(dimens.smallIconSize)
                    ) {
                        item.rotate += 90
                        item.rotate %= 360
                        rotate.value = item.rotate
                        rotatePhoto(item)
                    }
                }
            }

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
                    setDeleteImages(item)
                }
            }


            if (isLoading.value) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ){
                    CircularProgressIndicator(
                        color = colors.inactiveBottomNavIconColor
                    )
                }
            } else {
                LoadImage(
                    url = item.url ?: item.uri ?: item.tempId ?: "",
                    modifier = Modifier.rotate(rotate.value.toFloat()).fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}
