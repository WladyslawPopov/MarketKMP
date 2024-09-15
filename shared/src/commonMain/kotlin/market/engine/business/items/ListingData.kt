package market.engine.business.items

import market.engine.business.globalData.LD
import market.engine.business.globalData.SD
import kotlinx.serialization.Serializable

@Serializable
data class ListingData(
    var searchData : SD? = null,
    var data: LD? = null
)
