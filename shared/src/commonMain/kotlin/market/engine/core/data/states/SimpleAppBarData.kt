package market.engine.core.data.states

import androidx.compose.ui.graphics.Color
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem

data class SimpleAppBarData(
    val color: Color = colors.white,
    val onBackClick: (() -> Unit)? = null,
    val listItems: List<NavigationItem> = emptyList(),
    val closeMenu: (() -> Unit) = {},
    val menuItems: List<MenuItem> = emptyList(),
)
