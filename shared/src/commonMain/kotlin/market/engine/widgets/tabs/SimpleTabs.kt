package market.engine.widgets.tabs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun SimpleTabs(
    tabs: List<String>,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    containerColor: Color = colors.primaryColor,
    selectedTab: Int,
    modifier: Modifier = Modifier,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        modifier = modifier,
        selectedTabIndex = selectedTab,
        edgePadding = edgePadding,
        containerColor = containerColor,
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                unselectedContentColor = colors.grayText,
                selectedContentColor = colors.black,
                text = { Text(text = title, style = MaterialTheme.typography.titleSmall) }
            )
        }
    }
}
