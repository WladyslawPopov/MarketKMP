package market.engine.core.analytics

interface AnalyticsHelper {
    fun reportEvent(eventName: String, eventParameters: Map<String, Any?>)
    fun updateUserProfile(attributes: Map<String, Any?>)
    fun setUserProfileID(userID: String)
}
