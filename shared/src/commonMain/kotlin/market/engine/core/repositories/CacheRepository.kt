package market.engine.core.repositories

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import market.engine.core.utils.getMinutesRemainingUntil
import market.engine.shared.AuctionMarketDb

class CacheRepository(private val db: AuctionMarketDb) {

    fun <T> get(key: String, serializer: KSerializer<T>): T? {
        val cached = db.cacheQueries.selectByRequestId(key).executeAsOneOrNull()

        return if (cached != null && !isCacheExpired(cached.timestamp)) {
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

    fun <T> put(key: String, data: T, expiredTs: Long, serializer: KSerializer<T>) {
        val jsonResponse = Json.Default.encodeToString(serializer, data)
        db.cacheQueries.insertOrReplace(
            requestId = key,
            response = jsonResponse,
            timestamp = expiredTs
        )
    }

    fun deleteById(key: String) {
        db.cacheQueries.deleteByRequestId(key)
    }

    private fun isCacheExpired(timestamp: Long): Boolean {
        return getMinutesRemainingUntil(timestamp) == null
    }
}
