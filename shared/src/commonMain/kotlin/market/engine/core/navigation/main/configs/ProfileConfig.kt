package market.engine.core.navigation.main.configs

import kotlinx.serialization.Serializable
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD

@Serializable
sealed class ProfileConfig {
    @Serializable
    data object ProfileScreen : ProfileConfig()
    @Serializable
    data object MyOffersScreen : ProfileConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String) : ProfileConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : ProfileConfig()

    @Serializable
    data class UserScreen(val userId: Long, val ts: String) : ProfileConfig()
}
