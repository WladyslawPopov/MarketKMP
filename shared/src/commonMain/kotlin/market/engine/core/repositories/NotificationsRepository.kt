package market.engine.core.repositories

import androidx.compose.ui.util.fastForEach
import market.engine.core.data.globalData.UserData
import market.engine.shared.AuctionMarketDb
import market.engine.shared.NotificationsHistory


class NotificationsRepository(
    private val db: AuctionMarketDb
) {

    fun getNotificationList() : List<NotificationsHistory> = try {
            db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
        }catch (_ : Exception){
            emptyList()
        }

    fun deleteReadNotifications() = try {
            db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
                .filter { it.isRead > 0 }.fastForEach {
                db.notificationsHistoryQueries.deleteNotificationById(it.id)
            }
        } catch (e: Exception) {
            // Log.error("Failed to delete read notifications", e) // Using a hypothetical logger
            println("Error in deleteReadNotifications: ${e.message}")
            e.printStackTrace()
        }


    fun deleteNotificationById(id: String) = try {
            db.notificationsHistoryQueries.deleteNotificationById(id)
        } catch (e: Exception) {
            // Log.error("Failed to delete read notifications", e) // Using a hypothetical logger
            println("Error in deleteReadNotifications: ${e.message}")
            e.printStackTrace()
        }

    fun getUnreadCount(): Int = try {
            val list = db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
            if (list.isEmpty()) 0 else list.filter { it.isRead == 0L }.size
        }catch (_ : Exception){
            0
        }
}
