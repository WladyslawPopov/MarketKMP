package market.engine.core.data.states

import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.Tab

data class SwipeTabsBarState(
    val tabs : List<Tab> = emptyList(),
    val currentTab : String = "",
    val isTabsVisible : Boolean = true,
    val onClick: (String) -> Unit = {},
    val onLongClick: () -> Unit = {},
)

data class FilterBarUiState(
    val listFiltersButtons: List<FilterListingBtnItem> = emptyList(),
    val listNavigation: List<NavigationItem> = emptyList(),
    val swipeTabsBarState: SwipeTabsBarState? = null,
    val onClick : (NavigationItem) -> Unit = {}
)
