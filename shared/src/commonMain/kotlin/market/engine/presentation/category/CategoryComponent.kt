package market.engine.presentation.category

import market.engine.core.networkObjects.Category
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import market.engine.core.globalData.SD
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.types.CategoryScreenType
import market.engine.presentation.main.CategoryConfig
import org.koin.mp.KoinPlatform.getKoin


interface CategoryComponent {

    val model : Value<Model>

    data class Model(
        val categories: StateFlow<List<Category>>,
        val isLoading: StateFlow<Boolean>,
        val isError: StateFlow<ServerErrorException>
    )

    val searchData : StateFlow<SD>

    fun onRefresh()

    fun onCloseClicked()

    fun goToListing()

    fun goToSearch()

    fun updateCategoryList(id : Long)
}

class DefaultCategoryComponent(
    componentContext: ComponentContext,
    private val goToListingSelected: () -> Unit,
    private val goToSearchSelected: () -> Unit,
    private val onBackPressed: () -> Unit,
) : CategoryComponent, ComponentContext by componentContext {

    private val categoryViewModel: CategoryViewModel = getKoin().get()
    private val categoryOperations : CategoryOperations = getKoin().get()

    override val searchData : StateFlow<SD> = getKoin().get<StateFlow<SD>>()

    private val _model = MutableValue(
        CategoryComponent.Model(
            categories = categoryViewModel.responseCategory,
            isLoading = categoryViewModel.isShowProgress,
            isError = categoryViewModel.errorMessage
        )
    )

    init {
        onRefresh()
    }

    override val model: Value<CategoryComponent.Model> = _model

    override fun onRefresh() {
        categoryViewModel.viewModelScope.launch {
            if (searchData.value.searchCategoryID == searchData.value.searchParentID) {
                val catInfo = categoryOperations.getCategoryInfo(searchData.value.searchCategoryID)
                if (catInfo.success != null) {
                    searchData.value.searchCategoryName = catInfo.success?.name
                    searchData.value.searchCategoryID = catInfo.success?.id
                    searchData.value.searchParentID = catInfo.success?.parentId
                    searchData.value.searchIsLeaf = catInfo.success?.isLeaf == true
                    categoryViewModel.getCategory(searchData.value.searchCategoryID ?: 1L)
                }
            }else{
                categoryViewModel.getCategory(searchData.value.searchCategoryID ?: 1L)
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

    override fun updateCategoryList(id: Long) {
        categoryViewModel.updateCategory(id)
    }
}
