package market.engine.core.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import market.engine.shared.AuctionMarketDb

class CacheRepository(private val database: AuctionMarketDb) {

    suspend fun <T> get(key: String, serializer: KSerializer<T>): T? {
        return withContext(Dispatchers.IO) {
            val cached = database.cacheQueries.selectByRequestId(key).executeAsOneOrNull()

            if (cached != null && !isCacheExpired(cached.timestamp)) {
                try {
                    Json.decodeFromString(serializer, cached.response)
                } catch (_: Exception) {
                    null
                }
            } else {
                database.cacheQueries.deleteByRequestId(key)
                null
            }
        }
    }

    suspend fun <T> put(key: String, data: T, expiredTs: Long, serializer: KSerializer<T>) {
        withContext(Dispatchers.IO) {
            val jsonResponse = Json.encodeToString(serializer, data)
            database.cacheQueries.insertOrReplace(
                requestId = key,
                response = jsonResponse,
                timestamp = expiredTs
            )
        }
    }

    fun deleteById(key: String) {
        database.cacheQueries.deleteByRequestId(key)
    }

    private fun isCacheExpired(timestamp: Long): Boolean {
        return getMinutesRemainingUntil(timestamp) == null
    }
}
