package market.engine.ui.search

import application.market.agora.business.constants.UserData
import com.example.shared.MarketDB
import com.example.shared.SearchHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.business.core.ServerErrorException
import market.engine.business.globalObjects.searchData
import market.engine.root.BaseViewModel

class SearchViewModel(val dataBase : MarketDB) : BaseViewModel() {

    private val _responseHistory = MutableStateFlow<List<String>>(emptyList())
    val responseHistory: StateFlow<List<String>> = _responseHistory.asStateFlow()

    private val _searchString = MutableStateFlow(searchData.searchString ?: "")
    val searchString: StateFlow<String> = _searchString.asStateFlow()

    init {
        getHistory()
    }

    fun getHistory(searchString : String? = null){
        try {
            val sh = dataBase.searchHistoryQueries
            val searches = mutableListOf<String>()
            val searchHistory : List<SearchHistory> = if (searchString == null) {
                sh.selectAll().executeAsList()
            }else{
                _searchString.value = searchString
                sh.selectSearch("$searchString%", UserData.login).executeAsList()
            }

            if (searchHistory.isNotEmpty()){
                searchHistory.forEach {
                    val data = it.query
                    searches.add(data)
                }
            }
            _responseHistory.value = searches
        }catch (e : Exception){
            onError(ServerErrorException(e.message.toString(), ""))
        }
    }
}
