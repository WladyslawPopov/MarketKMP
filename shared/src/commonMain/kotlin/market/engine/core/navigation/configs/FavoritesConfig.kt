package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD

@Serializable
sealed class FavoritesConfig {
    @Serializable
    data object FavoritesScreen : FavoritesConfig()

    @Serializable
    data object SubscriptionsScreen : FavoritesConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String) : FavoritesConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : FavoritesConfig()
}
