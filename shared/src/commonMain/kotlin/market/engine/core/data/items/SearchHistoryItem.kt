package market.engine.core.data.items

import kotlinx.serialization.Serializable

@Serializable
data class SearchHistoryItem(
    val id: Long,
    val query: String,
    val isUsersSearch: Boolean,
    val isFinished: Boolean,
)
