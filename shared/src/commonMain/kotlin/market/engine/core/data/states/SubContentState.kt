package market.engine.core.data.states

import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.items.FilterListingBtnItem

data class SubContentState(
    val appState : SimpleAppBarData = SimpleAppBarData(),
    val listingData : ListingData = ListingData(),
    val activeFilterListingBtnItem: FilterListingBtnItem = FilterListingBtnItem()
)
