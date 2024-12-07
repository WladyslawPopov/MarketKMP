package market.engine.core.navigation.children



import market.engine.core.navigation.main.MainComponent
import market.engine.presentation.listing.ListingComponent
import market.engine.presentation.offer.OfferComponent
import market.engine.presentation.profile.ProfileComponent

sealed class ChildProfile {
    class ProfileChild(val component: ProfileComponent) : ChildProfile()
    class MyOffersChild(val component: MainComponent) : ChildProfile()
    class OfferChild(val component: OfferComponent) : ChildProfile()
    class ListingChild(val component: ListingComponent) : ChildProfile()
}
