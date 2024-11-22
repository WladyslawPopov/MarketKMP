package market.engine.core.navigation.children


import market.engine.presentation.offer.OfferComponent

sealed class ChildOffer {
    class OfferChild(val component: OfferComponent) : ChildOffer()
}
