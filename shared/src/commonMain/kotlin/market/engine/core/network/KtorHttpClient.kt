package market.engine.core.network

import market.engine.core.data.globalData.SAPI
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorHttpClient {

    fun HttpClientConfig<*>.installPlugins() {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status.value >= 300) {
                    throw ServerErrorException().isNotSuccessfulServerResponse(response.bodyAsText(), response.status.value)
                }
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 60_000
        }
        install(HttpRequestRetry) {
            retryOnException(maxRetries = 5)
            exponentialDelay()
        }
        defaultRequest {
            url(SAPI.API_BASE)
            header("X-Api-Key", SAPI.secret)
            SAPI.headers.forEach { (key, value) ->
                header(key, value)
            }
        }
    }
}
