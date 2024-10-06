package market.engine.widgets.grids

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass

@Composable
fun <T : Any> PagingGrid(
    data: LazyPagingItems<T>,
    state: LazyGridState = rememberLazyGridState(),
    content: @Composable (T) -> Unit
) {
    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(if (showNavigationRail) 4 else 2),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        items(data.itemCount) { index ->
            val item = data[index]
            item?.let { content(it) }
        }
    }
}
