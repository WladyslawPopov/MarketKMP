package market.engine.core.navigation.children

import market.engine.presentation.listing.ListingComponent
import market.engine.presentation.offer.OfferComponent

sealed class ChildSearch {
    class ListingChild(val component: ListingComponent) : ChildSearch()
    class OfferChild(val component: OfferComponent) : ChildSearch()
}
