package market.engine.presentation.search.listing.search

import market.engine.core.globalData.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.core.filtersObjects.CategoryBaseFilters
import market.engine.core.network.ServerErrorException

import market.engine.presentation.base.BaseViewModel
import market.engine.shared.MarketDB
import market.engine.shared.SearchHistory

class SearchViewModel(val db : MarketDB) : BaseViewModel() {

    private val _responseHistory = MutableStateFlow<List<SearchHistory>>(emptyList())
    val responseHistory: StateFlow<List<SearchHistory>> = _responseHistory.asStateFlow()

    val searchData = CategoryBaseFilters.filtersData.searchData
    val listingData = CategoryBaseFilters.filtersData.data

    fun getHistory(searchString : String = ""){
        try {
            val sh = db.searchHistoryQueries
            val searchHistory : List<SearchHistory> =
                sh.selectSearch("${searchString.trim()}%", UserData.login).executeAsList()

            _responseHistory.value = searchHistory
        }catch (e : Exception){
            onError(ServerErrorException(e.message.toString(), ""))
        }
    }

    fun deleteHistory() {
        val sh = db.searchHistoryQueries
        sh.deleteAll()
        getHistory()
    }

    fun deleteItemHistory(id: Long) {
        val sh = db.searchHistoryQueries
        sh.deleteById(id, UserData.login)
        getHistory()
    }
}
