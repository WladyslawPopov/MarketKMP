package market.engine.ui.category

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


interface CategoryComponent {

    val model : Value<Model>

    data class Model(
        val categories: StateFlow<List<Category>>,
        val isLoading: StateFlow<Boolean>,
        val isError: StateFlow<ServerErrorException>
    )

    fun onRefresh(categoryId: Long)

    fun onCloseClicked()

    fun goToListing()

    fun goToSearch()
}

class DefaultCategoryComponent(
    componentContext: ComponentContext,
    private val onBackPressed: () -> Unit,
    private val goToListingSelected: () -> Unit,
    private val goToSearchSelected: () -> Unit
) : CategoryComponent, ComponentContext by componentContext {

    private val categoryViewModel: CategoryViewModel = getKoin().get()
    private val categoryOperations : CategoryOperations = getKoin().get()

    private val _model = MutableValue(
        CategoryComponent.Model(
            categories = categoryViewModel.responseCategory,
            isLoading = categoryViewModel.isShowProgress,
            isError = categoryViewModel.errorMessage
        )
    )

    init {
        categoryViewModel.viewModelScope.launch {
            searchData.searchCategoryName = getString(strings.categoryMain)
            searchData.searchCategoryID = 1L

            categoryViewModel.getCategory(searchData.searchCategoryID ?: 1L)
        }

    }

    override val model: Value<CategoryComponent.Model> = _model

    override fun onRefresh(categoryId: Long) {
        categoryViewModel.viewModelScope.launch {
            val catInfo = categoryOperations.getCategoryInfo(categoryId)
            if(catInfo.success != null) {
                searchData.searchCategoryName = catInfo.success?.name
                searchData.searchCategoryID = catInfo.success?.id
                searchData.searchParentID = catInfo.success?.parentId
                searchData.searchIsLeaf = catInfo.success?.isLeaf == true
                categoryViewModel.getCategory(categoryId)
            }
        }
    }

    override fun onCloseClicked() {
        onBackPressed()
    }

    override fun goToListing() {
        goToListingSelected()
    }

    override fun goToSearch() {
        goToSearchSelected()
    }
}
