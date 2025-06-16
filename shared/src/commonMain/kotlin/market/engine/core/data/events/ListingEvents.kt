package market.engine.core.data.events

import market.engine.core.data.baseFilters.LD
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.ActiveWindowListingType

interface ListingEvents {
    fun onRefresh()
    fun changeActiveWindowType(type : ActiveWindowListingType)
    fun clearListingData()
    fun backClick()
    fun clickCategory(complete: Boolean)
    fun closeFilters(ld: LD, clear: Boolean)
    fun updateItem(item : OfferItem?)
    fun clearError()
}
