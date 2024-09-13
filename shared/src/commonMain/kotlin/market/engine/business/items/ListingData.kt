package market.engine.business.items

import application.market.auction_mobile.business.globalData.LD
import application.market.data.globalData.SD
import kotlinx.serialization.Serializable

@Serializable
data class ListingData(
    var searchData : SD? = null,
    var data: LD? = null
)
