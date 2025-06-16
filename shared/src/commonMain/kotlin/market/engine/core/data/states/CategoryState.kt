package market.engine.core.data.states

import market.engine.widgets.filterContents.categories.CategoryViewModel

data class CategoryState(
    val openCategory: Boolean = false,
    val categoryViewModel: CategoryViewModel = CategoryViewModel(),
)
