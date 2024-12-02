package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable

@Serializable
sealed class ProfileConfig {
    @Serializable
    data object ProfileScreen : ProfileConfig()
    @Serializable
    data object MyOffersScreen : ProfileConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String) : ProfileConfig()
}
