package market.engine.core.repositories

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import market.engine.core.data.globalData.UserData
import market.engine.shared.AuctionMarketDb

class OfferVisitedHistoryRepository(private val db: AuctionMarketDb, private val mutex: Mutex) {
    suspend fun getHistory(): List<Long> {
        return mutex.withLock {
            try {
                db.offerVisitedHistoryQueries.selectAll(UserData.login).executeAsList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
