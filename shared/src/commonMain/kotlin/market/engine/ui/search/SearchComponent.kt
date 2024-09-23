package market.engine.ui.search

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import market.engine.business.core.ServerErrorException
import market.engine.business.core.network.functions.CategoryOperations
import org.koin.mp.KoinPlatform.getKoin


interface SearchComponent {

    val model : Value<Model>

    data class Model(
        val history: StateFlow<List<String>>?,
        val isLoading: StateFlow<Boolean>,
        val isError: StateFlow<ServerErrorException>
    )

    fun onCloseClicked()

    fun goToListing()
}

class DefaultSearchComponent(
    componentContext: ComponentContext,
    private val onBackPressed: () -> Unit,
    private val goToListingSelected: () -> Unit
) : SearchComponent, ComponentContext by componentContext {

    private val searchViewModel: SearchViewModel = getKoin().get()
    private val categoryOperations : CategoryOperations = getKoin().get()

    private val _model = MutableValue(
        SearchComponent.Model(
            isLoading = searchViewModel.isShowProgress,
            isError = searchViewModel.errorMessage,
            history = searchViewModel.getHistory()
        )
    )

    override val model: Value<SearchComponent.Model> = _model


    override fun onCloseClicked() {
        onBackPressed()
    }

    override fun goToListing() {
        goToListingSelected()
    }
}
