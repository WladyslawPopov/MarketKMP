package market.engine.presentation.category

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import market.engine.core.network.functions.CategoryOperations
import org.koin.mp.KoinPlatform.getKoin


interface CategoryComponent {

    val model : Value<Model>

    data class Model(
        val categoryViewModel: CategoryViewModel
    )

    fun onRefresh()

    fun onCloseClicked()

    fun goToListing()

    fun goToSearch()
}

class DefaultCategoryComponent(
    componentContext: ComponentContext,
    private val goToListingSelected: () -> Unit,
    private val goToSearchSelected: () -> Unit,
    private val onBackPressed: () -> Unit,
) : CategoryComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        CategoryComponent.Model(
            categoryViewModel = getKoin().get()
        )
    )
    override val model: Value<CategoryComponent.Model> = _model


    private val categoryOperations : CategoryOperations = getKoin().get()

    val searchData = model.value.categoryViewModel.searchData

    override fun onRefresh() {
        model.value.categoryViewModel.viewModelScope.launch {
            if (searchData.value.searchCategoryID == searchData.value.searchParentID) {
                val catInfo = categoryOperations.getCategoryInfo(searchData.value.searchCategoryID)
                if (catInfo.success != null) {
                    searchData.value.searchCategoryName = catInfo.success?.name
                    searchData.value.searchCategoryID = catInfo.success?.id
                    searchData.value.searchParentID = catInfo.success?.parentId
                    searchData.value.searchIsLeaf = catInfo.success?.isLeaf == true
                    model.value.categoryViewModel.getCategory()
                }
            }else{
                model.value.categoryViewModel.getCategory()
            }
        }
    }

    override fun onCloseClicked() {
        onBackPressed()
    }

    override fun goToListing() {
        searchData.value.isRefreshing = true
        goToListingSelected()
    }

    override fun goToSearch() {
        goToSearchSelected()
    }
}
