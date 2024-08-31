package application.market.auction_mobile.business.baseFilters

import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    var key: String,
    var value: String,
    var interpritation: String?,
    var operation: String?,
)
