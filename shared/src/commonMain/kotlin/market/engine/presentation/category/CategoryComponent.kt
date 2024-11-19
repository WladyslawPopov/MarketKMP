package market.engine.presentation.category

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.launch
import market.engine.core.baseFilters.SD
import market.engine.core.network.functions.CategoryOperations
import org.koin.mp.KoinPlatform.getKoin


interface CategoryComponent {

    val model : Value<Model>

    data class Model(
        val categoryViewModel: CategoryViewModel
    )

    fun onRefresh(searchData : SD)

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

    override fun onRefresh(searchData : SD) {

        model.value.categoryViewModel.viewModelScope.launch {
            if (searchData.searchCategoryID == searchData.searchParentID) {
                val catInfo = categoryOperations.getCategoryInfo(searchData.searchCategoryID)
                if (catInfo.success != null) {
                    searchData.searchCategoryName = catInfo.success?.name
                    searchData.searchCategoryID = catInfo.success?.id
                    searchData.searchParentID = catInfo.success?.parentId
                    searchData.searchIsLeaf = catInfo.success?.isLeaf == true
                    model.value.categoryViewModel.getCategory(searchData.searchCategoryID ?:1L)
                }
            }else{
                model.value.categoryViewModel.getCategory(searchData.searchCategoryID ?: 1L)
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
