package market.engine.fragments.root.main.listing.search

import market.engine.core.data.globalData.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.core.data.baseFilters.SD
import market.engine.core.network.ServerErrorException

import market.engine.fragments.base.BaseViewModel
import market.engine.shared.MarketDB
import market.engine.shared.SearchHistory

class SearchViewModel(private val db : MarketDB) : BaseViewModel() {

    private val _responseHistory = MutableStateFlow<List<SearchHistory>>(emptyList())
    val responseHistory: StateFlow<List<SearchHistory>> = _responseHistory.asStateFlow()

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


    fun addHistory(searchString: String) {
        if (searchString != "") {
            val sh = db.searchHistoryQueries
            if (sh.selectSearch("${searchString.trim()}%", UserData.login).executeAsList().isEmpty()){
                sh.insertEntry(searchString, UserData.login)
            }
        }
    }

    fun searchAnalytic(searchData : SD){
        if (searchData.isRefreshing) {
            val event = mapOf(
                "search_query" to searchData.searchString,
                "visitor_id" to UserData.login,
                "search_cat_id" to searchData.searchCategoryID,
                "user_search" to searchData.userSearch,
                "user_search_login" to searchData.userLogin,
                "user_search_id" to searchData.userID
            )
            analyticsHelper.reportEvent("search_for_item", event)
        }
    }
}
