package market.engine.core.repositories

import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import market.engine.core.data.globalData.UserData
import market.engine.shared.AuctionMarketDb
import market.engine.shared.NotificationsHistory


class NotificationsRepository(
    private val db: AuctionMarketDb,
    private val mutex: Mutex
) {

    suspend fun getNotificationList() : List<NotificationsHistory> {
        return mutex.withLock {
            try {
                db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
            }catch (_ : Exception){
                emptyList()
            }
        }
    }

    suspend fun deleteReadNotifications() {
        return mutex.withLock {
            try {
                db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
                    .filter { it.isRead > 0 }.fastForEach {
                    db.notificationsHistoryQueries.deleteNotificationById(it.id)
                }
            } catch (e: Exception) {
                // Log.error("Failed to delete read notifications", e) // Using a hypothetical logger
                println("Error in deleteReadNotifications: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteNotificationById(id: String) {
        return mutex.withLock {
            try {
                db.notificationsHistoryQueries.deleteNotificationById(id)
            } catch (e: Exception) {
                // Log.error("Failed to delete read notifications", e) // Using a hypothetical logger
                println("Error in deleteReadNotifications: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    suspend fun getUnreadCount(): Int {
        return mutex.withLock {
            try {
                val list = db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
                if (list.isEmpty()) 0 else list.filter { it.isRead == 0L }.size
            }catch (_ : Exception){
                0
            }
        }
    }
}
