package market.engine.widgets.grids

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.PhotoTemp
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.ToastType
import market.engine.core.network.operations.uploadFile
import market.engine.fragments.createOffer.CreateOfferViewModel
import market.engine.widgets.items.PhotoCard
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState

@Composable
fun PhotoDraggableGrid(
    photoList: List<PhotoTemp>,
    viewModel: CreateOfferViewModel,
    deletePhoto: (PhotoTemp) -> Unit = {}
) {
    val listState = rememberUpdatedState(photoList)
    val lazyStaggeredGridState = rememberLazyStaggeredGridState()
    val reorderableState = rememberReorderableLazyStaggeredGridState(lazyStaggeredGridState) { from, to ->
        val newList = listState.value.toMutableList()
        newList.add(to.index, newList.removeAt(from.index))
        viewModel.setImages(newList)
    }

    val error = stringResource(strings.failureUploadPhoto)

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
        state = lazyStaggeredGridState,
        contentPadding = PaddingValues(dimens.smallPadding),
        verticalItemSpacing = dimens.smallPadding,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
    ) {
        items(listState.value, key = { it.id ?: it.tempId ?: it.uri ?: it.url ?: "" }) { item ->
            ReorderableItem(reorderableState, key = item.id ?: item.tempId ?: item.uri ?: item.url ?: "") {
                val interactionSource = remember { MutableInteractionSource() }
                PhotoCard(
                    item = item,
                    interactionSource = interactionSource,
                    modifier = Modifier.draggableHandle(
                        onDragStarted = {
//                                    ViewCompat.performHapticFeedback(
//                                        view,
//                                        HapticFeedbackConstantsCompat.GESTURE_START
//                                    )
                        },
                        onDragStopped = {
//                                    ViewCompat.performHapticFeedback(
//                                        view,
//                                        HapticFeedbackConstantsCompat.GESTURE_END
//                                    )
                        },
                        interactionSource = interactionSource,
                    ),
                    updatePhoto = {
                        val tempId = uploadFile(item)
                        if (tempId != null){
                           tempId
                        }else{
                            viewModel.showToast(
                                ToastItem(
                                    type = ToastType.ERROR,
                                    isVisible = true,
                                    message = "$error $tempId"
                                )
                            )
                            tempId
                        }
                    },
                    deletePhoto = deletePhoto
                )
            }
        }
    }
}
