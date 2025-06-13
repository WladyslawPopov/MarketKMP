package market.engine.core.data.baseFilters

import kotlinx.serialization.Serializable

@Serializable
data class ListingData(
    val searchData : SD = SD(),
    val data: LD = LD()
)
