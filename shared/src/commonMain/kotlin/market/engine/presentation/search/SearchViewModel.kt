package market.engine.presentation.search

import market.engine.core.constants.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.core.network.ServerErrorException
import market.engine.core.globalObjects.searchData
import market.engine.presentation.base.BaseViewModel
import market.engine.shared.MarketDB
import market.engine.shared.SearchHistory

class SearchViewModel(val dataBase : MarketDB) : BaseViewModel() {

    private val _responseHistory = MutableStateFlow<List<SearchHistory>>(emptyList())
    val responseHistory: StateFlow<List<SearchHistory>> = _responseHistory.asStateFlow()

    private val _searchString = MutableStateFlow(searchData.searchString ?: "")
    val searchString: StateFlow<String> = _searchString.asStateFlow()

    init {
        getHistory()
    }

    fun getHistory(searchString : String = searchData.searchString ?: ""){
        try {
            val sh = dataBase.searchHistoryQueries
            val searchHistory : List<SearchHistory> =
                sh.selectSearch("${searchString.trim()}%", UserData.login).executeAsList()

            _responseHistory.value = searchHistory
            _searchString.value = searchString
            searchData.searchString = searchString
        }catch (e : Exception){
            onError(ServerErrorException(e.message.toString(), ""))
        }
    }
}
