package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD

@Serializable
sealed class CategoryConfig {
    @Serializable
    data class ListingScreen(val listingData: LD, val searchData : SD) : CategoryConfig()

    @Serializable
    data class OfferScreen(val id: Long, val ts: String) : CategoryConfig()
}
