package market.engine.core.navigation.children

import market.engine.presentation.home.HomeComponent
import market.engine.presentation.listing.ListingComponent
import market.engine.presentation.offer.OfferComponent

sealed class ChildHome {
    class HomeChild(val component: HomeComponent) : ChildHome()
    class OfferChild(val component: OfferComponent) : ChildHome()
    class ListingChild(val component: ListingComponent) : ChildHome()
}
