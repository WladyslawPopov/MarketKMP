package market.engine.core.data.states

import androidx.compose.ui.text.input.TextFieldValue
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.events.SearchEvents
import market.engine.core.data.items.SearchHistoryItem
import market.engine.core.data.items.Tab

data class SearchUiState(
    val openSearch: Boolean = false,
    val searchData: SD = SD(),

    val searchString : TextFieldValue = TextFieldValue(""),

    val selectedTabIndex: Int = 0,
    val tabs: List<Tab> = listOf<Tab>(),

    val searchHistory: List<SearchHistoryItem> = emptyList(),

    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val categoryState: CategoryState = CategoryState(),
    val searchEvents: SearchEvents
)
