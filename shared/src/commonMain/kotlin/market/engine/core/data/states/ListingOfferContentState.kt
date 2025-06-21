package market.engine.core.data.states

import market.engine.core.data.baseFilters.ListingData

data class ListingOfferContentState(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val listingData: ListingData = ListingData(),
    val filterBarData: FilterBarUiState = FilterBarUiState(),
    val filtersCategoryState: CategoryState = CategoryState(),
    val listingBaseState: ListingBaseState = ListingBaseState(),
)
