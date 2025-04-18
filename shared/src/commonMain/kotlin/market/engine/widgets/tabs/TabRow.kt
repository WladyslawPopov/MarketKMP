package market.engine.widgets.tabs

import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.items.Tab

@Composable
fun TabRow(
    tabs: List<Tab>,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    containerColor: Color = colors.primaryColor,
    selectedTab: Int,
    modifier: Modifier = Modifier,
    contentTab : @Composable (index : Int, tab : Tab) -> Unit
) {
    ScrollableTabRow(
        modifier = modifier,
        selectedTabIndex = selectedTab,
        edgePadding = edgePadding,
        containerColor = containerColor,
    ) {
        tabs.forEachIndexed { index, tab ->
            contentTab(index, tab)
        }
    }
}
