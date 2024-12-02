package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable

@Serializable
sealed class FavoritesConfig {
    @Serializable
    data object FavoritesScreen : FavoritesConfig()

    @Serializable
    data object SubscriptionsScreen : FavoritesConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String) : FavoritesConfig()
}
