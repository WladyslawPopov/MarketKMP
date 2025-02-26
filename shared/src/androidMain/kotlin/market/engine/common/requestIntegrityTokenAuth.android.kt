package market.engine.common

var reqIntegrityTokenAuth: () -> Unit = {}

actual fun requestIntegrityTokenAuth() {
    reqIntegrityTokenAuth()
}
