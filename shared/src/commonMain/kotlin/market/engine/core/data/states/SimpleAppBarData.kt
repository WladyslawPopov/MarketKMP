package market.engine.core.data.states

import androidx.compose.runtime.Immutable
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem

data class MenuData(
    val isMenuVisible: Boolean = false,
    val menuItems: List<MenuItem> = emptyList(),
    val closeMenu: (() -> Unit) = {},
)

@Immutable
data class SimpleAppBarData(
    val menuData: MenuData = MenuData(),
    val onBackClick: (() -> Unit)? = null,
    val listItems: List<NavigationItem> = emptyList(),
)
