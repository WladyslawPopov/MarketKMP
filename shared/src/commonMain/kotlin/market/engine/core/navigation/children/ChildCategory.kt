package market.engine.core.navigation.children

import market.engine.core.items.ListingData
import market.engine.presentation.search.listing.ListingComponent
import market.engine.presentation.offer.OfferComponent

sealed class ChildCategory {
    class ListingChild(val listingData: ListingData, val component: ListingComponent) : ChildCategory()
    class OfferChild(val component: OfferComponent) : ChildCategory()
}
