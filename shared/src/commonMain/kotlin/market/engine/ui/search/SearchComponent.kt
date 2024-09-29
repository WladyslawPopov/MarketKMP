package market.engine.ui.search

import application.market.agora.business.constants.UserData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import market.engine.business.core.ServerErrorException
import market.engine.business.core.network.functions.CategoryOperations
import market.engine.business.globalObjects.searchData
import org.koin.mp.KoinPlatform.getKoin


interface SearchComponent {

    val model : Value<Model>

    data class Model(
        val history: StateFlow<List<String>>,
        val isLoading: StateFlow<Boolean>,
        val isError: StateFlow<ServerErrorException>,
        val searchString : StateFlow<String>
    )

    fun onCloseClicked()

    fun goToListing()

    fun updateHistory(searchString: String)

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
            sh.insertEntry(searchData.searchString!!, UserData.login)
        }

        goToListingSelected()
    }

    override fun updateHistory(searchString: String) {
        searchViewModel.getHistory(searchString)
    }

    override fun goToCategory() {

        goToCategorySelected()
    }
}
