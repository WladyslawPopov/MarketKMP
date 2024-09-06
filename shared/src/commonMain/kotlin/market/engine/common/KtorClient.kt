package market.engine.common

import io.ktor.client.HttpClient

expect fun getKtorClient() : HttpClient
