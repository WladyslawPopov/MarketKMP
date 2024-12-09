package market.engine.core.navigation.main.configs

import kotlinx.serialization.Serializable
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD

@Serializable
sealed class SearchConfig {
    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : SearchConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String) : SearchConfig()
}
