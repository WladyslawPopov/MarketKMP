package market.engine.core.data.states

import androidx.compose.runtime.Immutable
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem

@Immutable
data class SimpleAppBarData(
    val isMenuVisible: Boolean = false,
    val onBackClick: (() -> Unit)? = null,
    val listItems: List<NavigationItem> = emptyList(),
    val closeMenu: (() -> Unit) = {},
    val menuItems: List<MenuItem> = emptyList(),
)
