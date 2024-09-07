package market.engine.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import market.engine.business.core.KtorHttpClient.installPlugins
import platform.Foundation.setAllHTTPHeaderFields

actual fun getKtorClient(): HttpClient {
    return HttpClient(Darwin) {
        installPlugins()
    }
}
