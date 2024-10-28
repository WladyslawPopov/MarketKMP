package market.engine.core.navigation.children

import market.engine.presentation.category.CategoryComponent
import market.engine.presentation.listing.ListingComponent
import market.engine.presentation.search.SearchComponent

sealed class ChildCategory {
        class CategoryChild(val component: CategoryComponent) : ChildCategory()
        class ListingChild(val component: ListingComponent) : ChildCategory()
        class SearchChild(val component: SearchComponent) : ChildCategory()
    }
