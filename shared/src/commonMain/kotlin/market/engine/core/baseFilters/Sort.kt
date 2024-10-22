package market.engine.core.baseFilters

import kotlinx.serialization.Serializable

@Serializable
data class Sort(
    var key: String,
    var value: String,
    var interpritation: String?,
    var operation: String?,
    var sortText: String?
)
