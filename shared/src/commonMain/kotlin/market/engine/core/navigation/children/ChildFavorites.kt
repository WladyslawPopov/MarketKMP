package market.engine.core.navigation.children

import market.engine.presentation.favorites.FavoritesComponent
import market.engine.presentation.offer.OfferComponent
import market.engine.presentation.subscriptions.SubscribesComponent

sealed class ChildFavorites {
    class FavoritesChild(val component: FavoritesComponent) : ChildFavorites()
    class SubChild(val component: SubscribesComponent) : ChildFavorites()
    class OfferChild(val component: OfferComponent) : ChildFavorites()
}
