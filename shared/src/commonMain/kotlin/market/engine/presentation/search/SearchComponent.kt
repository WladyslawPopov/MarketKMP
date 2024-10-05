package market.engine.presentation.search

import market.engine.core.constants.UserData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import market.engine.core.globalData.SD
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

    val searchData : StateFlow<SD>

    fun onCloseClicked(categoryType: CategoryScreenType)

    fun goToListing()

    fun updateHistory(searchString: String)

    fun deleteHistory()

    fun deleteItemHistory(id : Long)
}

class DefaultSearchComponent(
    componentContext: ComponentContext,
    private val onBackPressed: () -> Unit,
) : SearchComponent, ComponentContext by componentContext {

    private val searchViewModel: SearchViewModel = getKoin().get()
    private val categoryOperations : CategoryOperations = getKoin().get()

    override val searchData : StateFlow<SD> = getKoin().get<StateFlow<SD>>()

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
        searchData.value.categoryType = categoryType
        onBackPressed()
    }

    override fun goToListing() {
        searchData.value.categoryType = CategoryScreenType.LISTING
        if (searchData.value.searchString != null && searchData.value.searchString != "") {
            val sh = searchViewModel.dataBase.searchHistoryQueries
            if (sh.selectSearch(searchData.value.searchString!!, UserData.login).executeAsList().isEmpty()){
                sh.insertEntry(searchData.value.searchString!!.trim(), UserData.login)
            }
        }

        onBackPressed()
    }

    override fun updateHistory(searchString: String) {
        searchData.value.searchString = searchString
        searchViewModel.getHistory(searchString)
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
}
