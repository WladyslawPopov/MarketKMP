package market.engine.presentation.category

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.filtersObjects.CategoryBaseFilters
import market.engine.core.baseFilters.SD
import org.koin.mp.KoinPlatform.getKoin


interface CategoryComponent {

    val model : Value<Model>

    data class Model(
        val categoryViewModel: CategoryViewModel
    )

    fun onRefresh(searchData : SD)

    fun goToListing()

    fun goToSearch()

    val onBackClicked: () -> Unit

    val goToNewCategory: () -> Unit
}

class DefaultCategoryComponent(
    componentContext: ComponentContext,
    private val goToListingSelected: () -> Unit,
    private val goToSearchSelected: () -> Unit,
    override val goToNewCategory: () -> Unit,
    override val onBackClicked: () -> Unit,
) : CategoryComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        CategoryComponent.Model(
            categoryViewModel = CategoryViewModel(
                apiService = getKoin().get()
            )
        )
    )
    override val model: Value<CategoryComponent.Model> = _model

    val searchData = CategoryBaseFilters.filtersData.searchData

    init {
        onRefresh(searchData.value)
    }

    override fun onRefresh(searchData : SD) {
        model.value.categoryViewModel.getCategory(searchData)
    }

    override fun goToListing() {
        goToListingSelected()
    }

    override fun goToSearch() {
        goToSearchSelected()
    }
}
