package market.engine.presentation.search

import market.engine.core.globalData.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.core.baseFilters.CategoryBaseFilters
import market.engine.core.network.ServerErrorException

import market.engine.presentation.base.BaseViewModel
import market.engine.shared.MarketDB
import market.engine.shared.SearchHistory

class SearchViewModel(val dataBase : MarketDB) : BaseViewModel() {

    private val _responseHistory = MutableStateFlow<List<SearchHistory>>(emptyList())
    val responseHistory: StateFlow<List<SearchHistory>> = _responseHistory.asStateFlow()

    val searchData = CategoryBaseFilters.filtersData.searchData
    val listingData = CategoryBaseFilters.filtersData.data

    init {
        getHistory()
    }

    fun getHistory(searchString : String = ""){
        try {
            val sh = dataBase.searchHistoryQueries
            val searchHistory : List<SearchHistory> =
                sh.selectSearch("${searchString.trim()}%", UserData.login).executeAsList()

            _responseHistory.value = searchHistory
        }catch (e : Exception){
            onError(ServerErrorException(e.message.toString(), ""))
        }
    }
}
