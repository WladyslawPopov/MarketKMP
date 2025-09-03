package market.engine.core.repositories

import market.engine.core.data.globalData.UserData
import market.engine.shared.AuctionMarketDb
import market.engine.shared.SearchHistory

class SearchRepository(private val db: AuctionMarketDb) {
    fun deleteHistoryItemById(id: Long){
        db.searchHistoryQueries.deleteById(id, UserData.login)
    }

    fun getHistory(query: String? = null) : List<SearchHistory> =
        db.searchHistoryQueries.selectSearch("${query}%", UserData.login).executeAsList()

    fun addHistory(query: String) = db.searchHistoryQueries.insertEntry(query, UserData.login)

    fun clearHistory() = db.searchHistoryQueries.deleteAll()
}
