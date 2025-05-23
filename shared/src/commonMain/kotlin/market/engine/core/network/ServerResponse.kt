package market.engine.core.network

import kotlinx.serialization.Serializable


@Serializable
data class ServerResponse<T>(
    var success: T? = null,
    var error: ServerErrorException? = null
)
