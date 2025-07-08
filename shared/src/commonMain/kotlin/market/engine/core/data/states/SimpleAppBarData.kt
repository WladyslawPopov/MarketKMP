package market.engine.core.data.states

import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem

data class SimpleAppBarData(
    val isMenuVisible: Boolean = false,
    val onBackClick: (() -> Unit)? = null,
    val listItems: List<NavigationItem> = emptyList(),
    val closeMenu: (() -> Unit) = {},
    val menuItems: List<MenuItem> = emptyList(),
)
