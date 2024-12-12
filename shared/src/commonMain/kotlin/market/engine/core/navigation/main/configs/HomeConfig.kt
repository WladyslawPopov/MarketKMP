package market.engine.core.navigation.main.configs

import kotlinx.serialization.Serializable
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD
import market.engine.core.network.networkObjects.Snapshot

@Serializable
sealed class HomeConfig {
    @Serializable
    data object HomeScreen : HomeConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String, val isSnapshot: Boolean = false) : HomeConfig()

    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : HomeConfig()

    @Serializable
    data class UserScreen(val userId: Long, val ts: String, val aboutMe : Boolean) : HomeConfig()
}
