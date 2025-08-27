package market.engine.core.utils

import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import market.engine.core.data.globalData.UserData
import market.engine.shared.AuctionMarketDb

suspend fun deleteReadNotifications(db: AuctionMarketDb, mutex: Mutex) {
    try {
        mutex.withLock {
            db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList().filter { it.isRead > 0 }.fastForEach {
                db.notificationsHistoryQueries.deleteNotificationById(it.id)
            }
        }
    } catch (e: Exception) {
        // Log.error("Failed to delete read notifications", e) // Using a hypothetical logger
        println("Error in deleteReadNotifications: ${e.message}")
        e.printStackTrace()
    }
}
