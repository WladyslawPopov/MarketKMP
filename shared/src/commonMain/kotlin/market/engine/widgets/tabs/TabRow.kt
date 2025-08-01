package market.engine.widgets.tabs

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.items.TabWithIcon

@Composable
fun TabRow(
    tabs: List<TabWithIcon>,
    containerColor: Color = colors.primaryColor,
    dividerColor: Color = colors.primaryColor,
    selectedTab: Int,
    modifier: Modifier = Modifier,
    contentTab : @Composable (index : Int, tab : TabWithIcon) -> Unit
) {
    ScrollableTabRow(
        modifier = modifier,
        selectedTabIndex = selectedTab,
        containerColor = containerColor,
        divider = {
            HorizontalDivider(modifier, color = containerColor)
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            contentTab(index, tab)
        }
    }
}
