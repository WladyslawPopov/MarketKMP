package market.engine.presentation.search

import market.engine.core.constants.UserData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import market.engine.core.globalData.CategoryBaseFilters
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.types.CategoryScreenType
import market.engine.shared.SearchHistory
import org.koin.mp.KoinPlatform.getKoin


interface SearchComponent {

    val model : Value<Model>

    data class Model(
        val history: StateFlow<List<SearchHistory>>,
        val isLoading: StateFlow<Boolean>,
        val isError: StateFlow<ServerErrorException>,
    )

    val globalData : CategoryBaseFilters

    fun onCloseClicked(categoryType: CategoryScreenType)

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

    private val searchViewModel: SearchViewModel = getKoin().get()
    private val categoryOperations : CategoryOperations = getKoin().get()

    override val globalData: CategoryBaseFilters = getKoin().get()

    private val searchData = globalData.listingData.searchData

    init {

        searchViewModel.getHistory()
    }

    private val _model = MutableValue(
        SearchComponent.Model(
            isLoading = searchViewModel.isShowProgress,
            isError = searchViewModel.errorMessage,
            history = searchViewModel.responseHistory,
        )
    )

    override val model: Value<SearchComponent.Model> = _model


    override fun onCloseClicked(categoryType: CategoryScreenType) {
        onBackPressed()
    }

    override fun goToListing() {
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
        searchData.value.searchString = string
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
        goToCategorySelected()
    }
}
