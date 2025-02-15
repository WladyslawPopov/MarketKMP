package market.engine.core.data.baseFilters

import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    var key: String,
    var value: String,
    var interpretation: String?,
    var operation: String?,
)
