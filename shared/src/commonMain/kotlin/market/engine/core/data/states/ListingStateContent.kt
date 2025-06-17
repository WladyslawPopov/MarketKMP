package market.engine.core.data.states

import market.engine.core.data.baseFilters.ListingData
import market.engine.core.network.networkObjects.Options


data class ListingStateContent(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val listingData: ListingData = ListingData(),
    val regions: List<Options> = emptyList(),

    val swipeTabsBarState: SwipeTabsBarState = SwipeTabsBarState(),
    val filterBarData: FilterBarUiState = FilterBarUiState(),
    val listingCategoryState: CategoryState = CategoryState(),
    val listingBaseState: ListingBaseState = ListingBaseState(),
)
