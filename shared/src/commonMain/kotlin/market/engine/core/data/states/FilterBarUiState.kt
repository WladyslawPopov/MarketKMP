package market.engine.core.data.states

import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.items.NavigationItem

data class FilterBarUiState(
    val listFiltersButtons: List<FilterListingBtnItem> = emptyList(),
    val listNavigation: List<NavigationItem> = emptyList(),
    val isShowFilters: Boolean = true,
    val isShowGrid: Boolean = false,
)
