package market.engine.core.data.states

import market.engine.core.data.items.Tab

data class SwipeTabsBarState(
    val tabs : List<Tab> = emptyList(),
    val currentTab : String = "",
    val isTabsVisible : Boolean = true
)
