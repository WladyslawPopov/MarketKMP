package market.engine.core.repositories

import market.engine.core.data.globalData.UserData
import market.engine.shared.AuctionMarketDb

class OfferVisitedHistoryRepository(private val db: AuctionMarketDb) {
    fun getHistory(): List<Long> = try {
            db.offerVisitedHistoryQueries.selectAll(UserData.login).executeAsList()
        } catch (_: Exception) {
            emptyList()
        }
}
