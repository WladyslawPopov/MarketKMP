package market.engine.core.data.baseFilters

import kotlinx.serialization.Serializable

@Serializable
data class LD(
    var filters : List<Filter> = emptyList(),
    var sort : Sort? = null,
    //pagination data
    var methodServer : String = "",
    var objServer : String = "",
)

