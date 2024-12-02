package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable

@Serializable
sealed class CategoryConfig {
    @Serializable
    data class CategoryScreen(val id: Long) : CategoryConfig()

    @Serializable
    data object SearchScreen : CategoryConfig()

    @Serializable
    data object ListingScreen : CategoryConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String) : CategoryConfig()
}
