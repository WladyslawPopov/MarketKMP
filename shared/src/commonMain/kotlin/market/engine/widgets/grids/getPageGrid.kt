package market.engine.widgets.grids

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.globalData.LD
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.widgets.bars.PagingCounterBar
import market.engine.widgets.buttons.getFloatAnyButton

@Composable
fun <T : Any> PagingGrid(
    data: LazyPagingItems<T>,
    state: LazyGridState = rememberLazyGridState(),
    listingData: State<LD>,
    content: @Composable (T) -> Unit
) {
    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big
    val itemsPerPage = listingData.value.pageCountItems
    val totalPages = listingData.value.totalPages

    var previousIndex by remember { mutableStateOf(0) }
    var showUpButton by remember { mutableStateOf(false) }
    var showDownButton by remember { mutableStateOf(true) }

    val currentPage by remember {
        derivedStateOf {
            (state.firstVisibleItemIndex / itemsPerPage) + 1
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleItemIndex }
            .collect { currentIndex ->
                if (currentIndex < previousIndex) {
                    showUpButton = true
                    showDownButton = false
                } else if (currentIndex > previousIndex) {
                    showUpButton = false
                    showDownButton = true
                }

                if (currentIndex == 0) {
                    showUpButton = false
                }

                if (currentPage == totalPages) {
                    showDownButton = false
                    showUpButton = false
                }

                previousIndex = currentIndex
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = state,
            columns = GridCells.Fixed(if (listingData.value.listingType != 0) if (showNavigationRail) 4 else 2 else 1),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.fillMaxSize().animateContentSize()
        ) {
            items(data.itemCount) { index ->
                val item = data[index]
                item?.let { content(it) }
            }
        }

        PagingCounterBar(
            currentPage = currentPage,
            totalPages = listingData.value.totalPages,
            modifier = Modifier.align(Alignment.BottomStart)
        )

        if (showUpButton) {
            getFloatAnyButton(
                drawable = drawables.iconArrowUp,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                CoroutineScope(Dispatchers.Main).launch {
                    scrollToPreviousPage(state, itemsPerPage)
                }
            }
        }

        if (showDownButton) {
            getFloatAnyButton(
                drawable = drawables.iconArrowDown,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                CoroutineScope(Dispatchers.Main).launch {
                    scrollToNextPage(state, itemsPerPage)
                }
            }
        }
    }
}

private suspend fun scrollToPreviousPage(state: LazyGridState, itemsPerPage: Int) {
    val previousPageFirstItemIndex = maxOf(state.firstVisibleItemIndex - itemsPerPage, 0)
    state.scrollToItem(previousPageFirstItemIndex)
}

private suspend fun scrollToNextPage(state: LazyGridState, itemsPerPage: Int) {
    val nextPageFirstItemIndex = minOf(
        state.firstVisibleItemIndex + itemsPerPage,
        state.layoutInfo.totalItemsCount - 1
    )
    state.scrollToItem(nextPageFirstItemIndex)
}
