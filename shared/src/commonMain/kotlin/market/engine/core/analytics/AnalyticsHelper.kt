package market.engine.core.analytics

interface AnalyticsHelper {
    fun initialize()
    fun reportEvent(eventName: String, eventParameters: Map<String, Any?>)
    fun reportEvent(eventName: String, eventParameters: String)
    fun updateUserProfile(attributes: Map<String, Any?>)
    fun setUserProfileID(userID: Long)
}
