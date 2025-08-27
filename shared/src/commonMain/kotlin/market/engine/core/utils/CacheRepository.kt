package market.engine.core.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock // Важный импорт
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import market.engine.shared.AuctionMarketDb


class CacheRepository(private val database: AuctionMarketDb, private val mutex: Mutex) {

    suspend fun <T> get(key: String, serializer: KSerializer<T>): T? {
        return mutex.withLock {
            val cached = database.cacheQueries.selectByRequestId(key).executeAsOneOrNull()

            if (cached != null && !isCacheExpired(cached.timestamp)) {
                try {
                    Json.decodeFromString(serializer, cached.response)
                } catch (_: Exception) {
                    null
                }
            } else {
                if (cached != null) {
                    database.cacheQueries.deleteByRequestId(key)
                }
                null
            }
        }
    }

    suspend fun <T> put(key: String, data: T, expiredTs: Long, serializer: KSerializer<T>) {
        mutex.withLock {
            val jsonResponse = Json.encodeToString(serializer, data)
            database.cacheQueries.insertOrReplace(
                requestId = key,
                response = jsonResponse,
                timestamp = expiredTs
            )
        }
    }

    suspend fun deleteById(key: String) {
        mutex.withLock {
            database.cacheQueries.deleteByRequestId(key)
        }
    }

    private fun isCacheExpired(timestamp: Long): Boolean {
        return getMinutesRemainingUntil(timestamp) == null
    }
}
