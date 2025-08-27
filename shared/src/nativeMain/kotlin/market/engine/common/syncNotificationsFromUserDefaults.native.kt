package market.engine.common

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import market.engine.shared.AuctionMarketDb
import platform.Foundation.NSArray
import platform.Foundation.NSDictionary
import platform.Foundation.NSUserDefaults

actual fun syncNotificationsFromUserDefaults(db: AuctionMarketDb, mutex: Mutex) {
    runBlocking {
        mutex.withLock {
            val nativeSettings =
                NSUserDefaults(suiteName = "group.application.market.auction-mobile")
            val pendingArray =
                nativeSettings.arrayForKey("pending_notifications") as? NSArray ?: return@runBlocking

            if (pendingArray.count == 0uL) { // NSUInteger (ULong) сравниваем с ULong
                println("No pending notifications to sync from Kotlin.")
                return@runBlocking
            }

            println(">>> Syncing ${pendingArray.count} pending notifications from Kotlin...")

            val notificationsToSync = mutableListOf<Map<String, Any>>()

            repeat(pendingArray.count.toInt()) { index ->
                val dict =
                    pendingArray.objectAtIndex(index.toULong()) as? NSDictionary ?: return@repeat

                val id = dict.objectForKey("id") as? String
                val owner = dict.objectForKey("owner") as? Long
                val title = dict.objectForKey("title") as? String
                val body = dict.objectForKey("body") as? String
                val type = dict.objectForKey("type") as? String
                val timestamp = dict.objectForKey("timestamp") as? Long
                val data = dict.objectForKey("data") as? String
                val isRead = dict.objectForKey("isRead") as? Long

                if (id != null && owner != null && title != null && body != null && type != null && timestamp != null && data != null && isRead != null) {
                    notificationsToSync.add(
                        mapOf(
                            "id" to id,
                            "owner" to owner,
                            "title" to title,
                            "body" to body,
                            "type" to type,
                            "timestamp" to timestamp,
                            "data" to data,
                            "isRead" to isRead
                        )
                    )
                }
            }

            if (notificationsToSync.isNotEmpty()) {
                db.transaction {
                    notificationsToSync.forEach { notification ->
                        db.notificationsHistoryQueries.insertOrReplaceNotification(
                            id = notification["id"] as String,
                            owner = notification["owner"] as Long,
                            title = notification["title"] as String,
                            body = notification["body"] as String,
                            type = notification["type"] as String,
                            timestemp = notification["timestamp"] as Long,
                            data_ = notification["data"] as String,
                            isRead = notification["isRead"] as Long
                        )
                    }
                }
            }

            nativeSettings.removeObjectForKey("pending_notifications")
            nativeSettings.synchronize()
        }
    }
    println(">>> Sync complete. Cleared pending_notifications from UserDefaults.")
}
