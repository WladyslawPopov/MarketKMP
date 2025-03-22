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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.PhotoTemp
import market.engine.core.utils.printLogD
import market.engine.fragments.root.main.createOffer.CreateOfferViewModel
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun PhotoCard(
    item: PhotoTemp,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    viewModel: CreateOfferViewModel,
    deletePhoto: (PhotoTemp) -> Unit = {},
    openPhoto: () -> Unit = {}
) {
    val rotate = remember { mutableStateOf(item.rotate) }

    val isLoading = remember{ mutableStateOf(item.tempId == null) }

    val errTost = stringResource(strings.operationFailed)

    LaunchedEffect(item.tempId){
        if (item.tempId == null){
            viewModel.uploadPhotoTemp(item){
                if (it.tempId != null) {
                    item.uri = it.uri
                    item.tempId = it.tempId
                    isLoading.value = false
                }else{
                    viewModel.showToast(
                        errorToastItem.copy(
                            message = errTost
                        )
                    )
                    deletePhoto(item)
                }
            }
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
                    color = colors.negativeRed,
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
                    color = colors.negativeRed,
                    modifierIconSize = Modifier.size(dimens.smallIconSize),
                    modifier = Modifier.size(dimens.smallIconSize)
                ) {
                    deletePhoto(item)
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
                AsyncImage(
                    model = item.uri ?: item.url ?: "",
                    contentDescription = null,
                    modifier = Modifier.rotate(rotate.value.toFloat()).fillMaxSize(),
                    imageLoader = ImageLoader.Builder(LocalPlatformContext.current)
                        .crossfade(true)
                        .components {
                            add(SvgDecoder.Factory())
                        }.build(),
                    contentScale = ContentScale.Crop,
                    onSuccess = {
                        isLoading.value = false
                    },
                    onError = {
                        printLogD("Coil Error", it.result.throwable.message)
                    },
                )
            }
        }
    }
}
