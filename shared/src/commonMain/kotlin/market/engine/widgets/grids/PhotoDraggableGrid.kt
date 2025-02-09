package market.engine.widgets.grids

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.PhotoTemp
import market.engine.fragments.root.main.createOffer.CreateOfferViewModel
import market.engine.widgets.items.PhotoCard
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

@Composable
fun PhotoDraggableGrid(
    photoList: List<PhotoTemp>,
    viewModel: CreateOfferViewModel,
    deletePhoto: (PhotoTemp) -> Unit = {}
) {
    val listState = rememberUpdatedState(photoList)
    val lazyStaggeredGridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(lazyStaggeredGridState) { from, to ->
        val newList = listState.value.toMutableList()
        newList.add(to.index, newList.removeAt(from.index))
        viewModel.setImages(newList)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier.fillMaxWidth().heightIn(max = (200 * photoList.size).dp),
        state = lazyStaggeredGridState,
        contentPadding = PaddingValues(dimens.mediumPadding),
        horizontalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
    ) {
        items(listState.value, key = { it.id ?: it.tempId ?: it.uri ?: it.url ?: "" }) { item ->
            ReorderableItem(reorderableState, key = item.id ?: item.tempId ?: item.uri ?: item.url ?: "") {
                val interactionSource = remember { MutableInteractionSource() }
                PhotoCard(
                    item = item,
                    interactionSource = interactionSource,
                    viewModel = viewModel,
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
                    ).sizeIn(maxWidth = 600.dp, maxHeight = 150.dp),
                    deletePhoto = deletePhoto
                )
            }
        }
    }
}
