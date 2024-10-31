package market.engine.core.baseFilters

import com.arkivanov.decompose.value.MutableValue
import market.engine.core.items.ListingData
import market.engine.core.types.CategoryScreenType
import market.engine.core.types.FavScreenType

interface FavControls {
    val listingData : ListingData
    val favStack : ArrayList<FavScreenType>
}

object FavBaseFilters : FavControls {
    override val listingData: ListingData = ListingData(
        searchData = MutableValue(SD()),
        data = MutableValue(LD())
    )
    override val favStack = arrayListOf(FavScreenType.FAVORITES)
}
