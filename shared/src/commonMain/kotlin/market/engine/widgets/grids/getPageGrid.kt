package market.engine.widgets.grids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import market.engine.business.types.WindowSizeClass
import market.engine.business.util.getWindowSizeClass

@Composable
fun <T : Any> PagingGrid(
    data: LazyPagingItems<T>,
    content: @Composable (T) -> Unit
) {
    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    LazyVerticalGrid(
        columns = GridCells.Fixed(if (showNavigationRail) 4 else 2),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.sizeIn(minHeight = 1000.dp, maxHeight = 2000.dp ).fillMaxWidth()
    ) {
        items(data.itemCount) { index ->
            val item = data[index]
            item?.let { content(it) }
        }
    }
}
