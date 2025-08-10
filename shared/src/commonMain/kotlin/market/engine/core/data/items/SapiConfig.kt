package market.engine.core.data.items

import kotlinx.serialization.Serializable

@Serializable
data class SapiConfig(
    val apiBase: String = "",
    val serverBase: String = "",
    val reviewUrl: String = "",
    val secret: String = "",
)
