package market.engine.core.repositories

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import market.engine.core.data.globalData.UserData
import market.engine.shared.AuctionMarketDb
import market.engine.shared.SearchHistory

class SearchRepository(private val db: AuctionMarketDb, private val mutex: Mutex) {
    suspend fun deleteHistoryItemById(id: Long){
        mutex.withLock {
            db.searchHistoryQueries.deleteById(id, UserData.login)
        }
    }

    suspend fun getHistory(query: String? = null) : List<SearchHistory> = mutex.withLock {
        db.searchHistoryQueries.selectSearch("${query}%", UserData.login).executeAsList()
    }

    suspend fun addHistory(query: String) = mutex.withLock {
        db.searchHistoryQueries.insertEntry(query, UserData.login)
    }

    suspend fun clearHistory() = mutex.withLock {
        db.searchHistoryQueries.deleteAll()
    }
}
