package market.engine.core.baseFilters

import com.arkivanov.decompose.value.MutableValue
import market.engine.core.items.ListingData
import market.engine.core.types.CategoryScreenType
import market.engine.core.types.FavScreenType
import market.engine.core.types.LotsType

interface ProfileControls {
    val listingData : ListingData
    val profStack : ArrayList<LotsType>
}

object ProfileBaseFilters : ProfileControls {
    override val listingData: ListingData = ListingData(
        searchData = MutableValue(SD()),
        data = MutableValue(LD())
    )
    override val profStack = arrayListOf(LotsType.MYLOT_ACTIVE)
}
