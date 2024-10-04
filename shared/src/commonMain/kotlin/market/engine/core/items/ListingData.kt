package market.engine.core.items

import market.engine.core.globalData.LD
import market.engine.core.globalData.SD
import kotlinx.serialization.Serializable

@Serializable
data class ListingData(
    var searchData : SD? = null,
    var data: LD? = null
)
