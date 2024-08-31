package application.market.auction_mobile.business.core

import kotlinx.serialization.Serializable


@Serializable
data class ServerResponse<T>(
    var success: T? = null,
    var error: ServerErrorException? = null
)
