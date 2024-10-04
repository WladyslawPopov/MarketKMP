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
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.globalObjects.searchData
import market.engine.presentation.main.CategoryConfig
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
    private val navigation: StackNavigation<CategoryConfig>
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
            if (searchData.searchCategoryID == 1L) {
                searchData.searchCategoryName = getString(strings.categoryMain)
            }

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
        navigation.pop()
    }

    override fun goToListing() {
       navigation.push(CategoryConfig.ListingScreen)
    }

    override fun goToSearch() {
       navigation.push(CategoryConfig.SearchScreen)
    }
}
