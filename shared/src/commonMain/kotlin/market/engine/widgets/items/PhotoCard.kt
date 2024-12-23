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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.items.PhotoTemp
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.exceptions.LoadImage

@Composable
fun PhotoCard(
    item: PhotoTemp,
    interactionSource: MutableInteractionSource,
    modifier: Modifier,
    updatePhoto: suspend (PhotoTemp) -> String?,
    deletePhoto: (PhotoTemp) -> Unit = {},
    openPhoto: () -> Unit = {}
) {
    val rotate = remember { mutableStateOf(item.rotate) }

    val isLoading = remember{ mutableStateOf(item.tempId == null) }

    LaunchedEffect(item.tempId){
        if (item.tempId == null){
            item.tempId = updatePhoto(item)
            isLoading.value = false
        }
    }

    Card(
        onClick = openPhoto,
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ){
            Row(
                modifier = Modifier.padding(dimens.smallPadding)
                    .align(Alignment.TopStart)
                    .zIndex(5f)
            ) {
                SmallIconButton(
                    drawables.recycleIcon,
                    color = colors.titleTextColor,
                    modifierIconSize = Modifier.size(dimens.smallIconSize),
                    modifier = Modifier
                        .size(dimens.smallIconSize)
                ) {
                    item.rotate += 90
                    item.rotate %= 360
                    rotate.value = item.rotate
                }
            }

            Row(
                modifier = Modifier.padding(dimens.smallPadding)
                    .align(Alignment.TopEnd)
                    .zIndex(5f)
            ) {
                SmallIconButton(
                    drawables.cancelIcon,
                    color = colors.titleTextColor,
                    modifierIconSize = Modifier.size(dimens.smallIconSize),
                    modifier = Modifier.size(dimens.smallIconSize)
                ) {
                    deletePhoto(item)
                }
            }


            if (isLoading.value) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(150.dp)
                ){
                    CircularProgressIndicator(
                        color = colors.inactiveBottomNavIconColor
                    )
                }
            } else {
                LoadImage(
                    item.uri ?: item.url ?: "",
                    size = 150.dp,
                    rotation = rotate.value.toFloat(),
                    isShowLoading = false,
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}
