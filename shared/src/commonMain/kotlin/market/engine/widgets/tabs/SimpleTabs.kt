package market.engine.widgets.tabs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import market.engine.core.data.globalData.ThemeResources.colors

@Composable
fun SimpleTabs(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
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
