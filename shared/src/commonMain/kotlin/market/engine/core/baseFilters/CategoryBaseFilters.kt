package market.engine.core.baseFilters

import com.arkivanov.decompose.value.MutableValue
import market.engine.core.items.ListingData
import market.engine.core.types.CategoryScreenType

interface CategoryControls {
    val listingData : ListingData
    val categoryStack : ArrayList<CategoryScreenType>
}

object CategoryBaseFilters : CategoryControls {
    override val listingData: ListingData = ListingData(
        searchData = MutableValue(SD()),
        data = MutableValue(LD())
    )
    override val categoryStack = arrayListOf(CategoryScreenType.CATEGORY)
}
