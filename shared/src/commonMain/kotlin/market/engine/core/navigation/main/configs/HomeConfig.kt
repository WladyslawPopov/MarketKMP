package market.engine.core.navigation.main.configs

import kotlinx.serialization.Serializable
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD

@Serializable
sealed class HomeConfig {
    @Serializable
    data object HomeScreen : HomeConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String) : HomeConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : HomeConfig()

    @Serializable
    data class UserScreen(val userId: Long, val ts: String) : HomeConfig()
}
