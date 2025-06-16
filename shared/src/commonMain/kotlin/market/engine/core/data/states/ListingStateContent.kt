package market.engine.core.data.states

import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.events.ListingEvents
import market.engine.core.network.networkObjects.Options
import market.engine.widgets.bars.appBars.SimpleAppBarData

data class ListingStateContent(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val listingData: ListingData = ListingData(),
    val regions: List<Options> = emptyList(),

    val filterBarData: FilterBarUiState = FilterBarUiState(),
    val listingCategoryState: CategoryState = CategoryState(),
    val listingBaseState: ListingBaseState = ListingBaseState(),

    val listingEvents: ListingEvents
)
