package market.engine.presentation.search

import market.engine.core.constants.UserData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.globalObjects.searchData
import market.engine.shared.SearchHistory
import org.koin.mp.KoinPlatform.getKoin


interface SearchComponent {

    val model : Value<Model>

    data class Model(
        val history: StateFlow<List<SearchHistory>>,
        val isLoading: StateFlow<Boolean>,
        val isError: StateFlow<ServerErrorException>,
        val searchString : StateFlow<String>
    )

    fun onCloseClicked()

    fun goToListing()

    fun updateHistory(searchString: String)

    fun goToCategory()

    fun deleteHistory()

    fun deleteItemHistory(id : Long)
}

class DefaultSearchComponent(
    componentContext: ComponentContext,
    private val onBackPressed: () -> Unit,
) : SearchComponent, ComponentContext by componentContext {

    private val searchViewModel: SearchViewModel = getKoin().get()
    private val categoryOperations : CategoryOperations = getKoin().get()

    init {

        searchViewModel.getHistory()
    }

    private val _model = MutableValue(
        SearchComponent.Model(
            isLoading = searchViewModel.isShowProgress,
            isError = searchViewModel.errorMessage,
            history = searchViewModel.responseHistory,
            searchString = searchViewModel.searchString
        )
    )

    override val model: Value<SearchComponent.Model> = _model


    override fun onCloseClicked() {
        onBackPressed()
    }

    override fun goToListing() {
        if (searchData.searchString != null) {
            val sh = searchViewModel.dataBase.searchHistoryQueries
            if (sh.selectSearch(searchData.searchString!!, UserData.login).executeAsList().isEmpty()){
                sh.insertEntry(searchData.searchString!!.trim(), UserData.login)
            }
        }

       // goToListingSelected()
    }

    override fun updateHistory(searchString: String) {
        searchViewModel.getHistory(searchString)
    }

    override fun goToCategory() {

       // goToCategorySelected()
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
