package market.engine.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import market.engine.core.network.KtorHttpClient.installPlugins

actual fun getKtorClient(): HttpClient {
    return HttpClient(Java){
        installPlugins()
    }
}
