package market.engine.core.repositories

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import market.engine.core.utils.getMinutesRemainingUntil
import market.engine.shared.AuctionMarketDb

class CacheRepository(private val db: AuctionMarketDb, private val mutex: Mutex) {

    suspend fun <T> get(key: String, serializer: KSerializer<T>): T? {
        return mutex.withLock {
            val cached = db.cacheQueries.selectByRequestId(key).executeAsOneOrNull()

            if (cached != null && !isCacheExpired(cached.timestamp)) {
                try {
                    Json.Default.decodeFromString(serializer, cached.response)
                } catch (_: Exception) {
                    null
                }
            } else {
                if (cached != null) {
                    db.cacheQueries.deleteByRequestId(key)
                }
                null
            }
        }
    }

    suspend fun <T> put(key: String, data: T, expiredTs: Long, serializer: KSerializer<T>) {
        mutex.withLock {
            val jsonResponse = Json.Default.encodeToString(serializer, data)
            db.cacheQueries.insertOrReplace(
                requestId = key,
                response = jsonResponse,
                timestamp = expiredTs
            )
        }
    }

    suspend fun deleteById(key: String) {
        mutex.withLock {
            db.cacheQueries.deleteByRequestId(key)
        }
    }

    private fun isCacheExpired(timestamp: Long): Boolean {
        return getMinutesRemainingUntil(timestamp) == null
    }
}
