package market.engine.widgets.grids

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.items.PhotoTemp
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.items.PhotoCard
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState

@Composable
fun PhotoDraggableGrid(
    photoList: List<PhotoTemp>,
    viewModel: BaseViewModel
) {
    val listState = remember { mutableStateOf(photoList) }

    val lazyStaggeredGridState = rememberLazyStaggeredGridState()
    val reorderableState = rememberReorderableLazyStaggeredGridState(lazyStaggeredGridState) { from, to ->
        val newList = listState.value.toMutableList()
        newList.add(to.index, newList.removeAt(from.index))
        listState.value = newList
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 96.dp),
        modifier = Modifier.fillMaxWidth().height(500.dp),
        state = lazyStaggeredGridState,
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(listState.value, key = { it.uri ?: "" }) { item ->
            ReorderableItem(reorderableState, key = item.uri ?: "") {
                val interactionSource = remember { MutableInteractionSource() }

                PhotoCard(
                    item = item,
                    viewModel = viewModel,
                    onItemUploaded = { newTempId ->
                        val oldList = listState.value.toMutableList()
                        val idx = oldList.indexOf(item)
                        if (idx != -1) {
                            oldList[idx] = oldList[idx].copy(tempId = newTempId)
                            listState.value = oldList
                        }
                    },
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
                    )
                )
            }
        }
    }
}
