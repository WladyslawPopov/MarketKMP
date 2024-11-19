package market.engine.presentation.search

import market.engine.core.globalData.UserData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.analytics.AnalyticsHelper
import org.koin.mp.KoinPlatform.getKoin


interface SearchComponent {

    val model : Value<Model>

    data class Model(
        val searchViewModel: SearchViewModel,
    )

    fun onCloseClicked()

    fun goToListing()

    fun updateHistory(string : String)

    fun deleteHistory()

    fun deleteItemHistory(id : Long)

    fun goToCategory()
}

class DefaultSearchComponent(
    componentContext: ComponentContext,
    private val onBackPressed: () -> Unit,
    private val goToListingSelected: () -> Unit,
    private val goToCategorySelected: () -> Unit
) : SearchComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        SearchComponent.Model(
            searchViewModel = getKoin().get()
        )
    )

    override val model: Value<SearchComponent.Model> = _model
    private val analyticsHelper : AnalyticsHelper = getKoin().get()
    private val searchViewModel: SearchViewModel = getKoin().get()
    private val searchData = model.value.searchViewModel.searchData

    init {
        searchViewModel.getHistory()
    }

    override fun onCloseClicked() {
        searchAnalytic()
        onBackPressed()
    }

    override fun goToListing() {
        searchAnalytic()
        val searchString = searchData.value.searchString
        if (searchString != "" && searchString != null) {
            val sh = searchViewModel.dataBase.searchHistoryQueries
            if (sh.selectSearch(searchString, UserData.login).executeAsList().isEmpty()){
                sh.insertEntry(searchString, UserData.login)
            }
        }
        goToListingSelected()
    }

    override fun updateHistory(string : String) {
        searchViewModel.getHistory()
    }

    override fun deleteHistory() {
        val sh = searchViewModel.dataBase.searchHistoryQueries
        sh.deleteAll()
        searchViewModel.getHistory()
    }

    override fun deleteItemHistory(id: Long) {
        val sh = searchViewModel.dataBase.searchHistoryQueries
        sh.deleteById(id)
        searchViewModel.getHistory()
    }

    override fun goToCategory() {
        searchAnalytic()
        goToCategorySelected()
    }

    private fun searchAnalytic(){
        if (searchData.value.isRefreshing) {
            val event = mapOf(
                "search_query" to searchData.value.searchString,
                "visitor_id" to UserData.login,
                "search_cat_id" to searchData.value.searchCategoryID,
                "user_search" to searchData.value.userSearch,
                "user_search_login" to searchData.value.userLogin,
                "user_search_id" to searchData.value.userID
            )
            analyticsHelper.reportEvent("search_for_item", event)
        }
    }
}
