package market.engine.core.data.baseFilters

import kotlinx.serialization.Serializable

@Serializable
data class Sort(
    var key: String,
    var value: String,
    var interpretation: String?,
    var operation: String?,
    var sortText: String?
)
