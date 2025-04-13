package market.engine.core.data.items

data class SearchHistoryItem(
    val id: Long,
    val query: String,
    val isUsersSearch: Boolean,
    val isFinished: Boolean,
)
