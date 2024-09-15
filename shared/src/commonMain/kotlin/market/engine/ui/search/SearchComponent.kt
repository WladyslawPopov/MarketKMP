package market.engine.ui.search

import application.market.auction_mobile.business.networkObjects.Category
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.core.ServerErrorException
import market.engine.business.core.network.functions.CategoryOperations
import market.engine.business.globalObjects.searchData
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin


interface SearchComponent {

    val model : Value<Model>

    data class Model(
        val categories: StateFlow<List<Category>>,
        val isLoading: StateFlow<Boolean>,
        val isError: StateFlow<ServerErrorException>
    )

    fun onRefresh(categoryId: Long)

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
            categories = searchViewModel.responseCategory,
            isLoading = searchViewModel.isShowProgress,
            isError = searchViewModel.errorMessage
        )
    )

    init {
        searchViewModel.viewModelScope.launch {
            searchData.searchCategoryName = getString(strings.categoryMain)
            searchData.searchCategoryID = 1L

            searchViewModel.getCategory(searchData.searchCategoryID ?: 1L)
        }

    }

    override val model: Value<SearchComponent.Model> = _model

    override fun onRefresh(categoryId: Long) {
        searchViewModel.viewModelScope.launch {
            val catInfo = categoryOperations.getCategoryInfo(categoryId)
            if(catInfo.success != null) {
                searchData.searchCategoryName = catInfo.success?.name
                searchData.searchCategoryID = catInfo.success?.id
                searchData.searchParentID = catInfo.success?.parentId
                searchData.searchIsLeaf = catInfo.success?.isLeaf == true
                searchViewModel.getCategory(categoryId)
            }
        }
    }

    override fun onCloseClicked() {
        onBackPressed()
    }

    override fun goToListing() {
        goToListingSelected()
    }
}
