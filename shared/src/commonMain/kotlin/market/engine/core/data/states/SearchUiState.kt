package market.engine.core.data.states

import market.engine.core.data.baseFilters.SD
import market.engine.core.data.events.SearchEvents
import market.engine.core.data.items.SearchHistoryItem
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.items.TabWithIcon

data class SearchUiState(
    val searchData: SD = SD(),

    val searchString : String = "",

    val selectedTabIndex: Int = 0,
    val tabs: List<TabWithIcon> = listOf(),

    val searchHistory: List<SearchHistoryItem> = emptyList(),

    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val categoryState: CategoryState,
    val searchEvents: SearchEvents
)
