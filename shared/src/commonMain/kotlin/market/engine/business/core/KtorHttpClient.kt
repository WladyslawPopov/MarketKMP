package market.engine.business.core

import application.market.auction_mobile.business.core.ServerErrorException
import market.engine.business.constants.SAPI
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import market.engine.common.getKtorClient

object KtorHttpClient {

    fun HttpClientConfig<*>.installPlugins() {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    println("AppDebug KtorHttpClient message: $message")
                }
            }
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
            header("X-Api-Key", SAPI.getApiKey())
            SAPI.headers.forEach { (key, value) ->
                header(key, value)
            }
        }
    }
}
