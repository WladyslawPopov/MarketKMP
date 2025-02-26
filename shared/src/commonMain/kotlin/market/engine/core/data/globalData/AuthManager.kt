package market.engine.core.data.globalData

object AuthManager {
    var onAuthSuccess: ((Map<String, String>) -> Unit)? = null
}
