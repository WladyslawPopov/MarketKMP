package market.engine.common

import market.engine.core.analytics.AnalyticsHelper

var analyticsHelper: AnalyticsHelper? = null

actual object AnalyticsFactory {
    actual fun getAnalyticsHelper(): AnalyticsHelper {
        return if (analyticsHelper == null) {
            object : AnalyticsHelper {
                override fun reportEvent(eventName: String, eventParameters: Map<String, Any?>) {
                    println("Event: $eventName, Parameters: $eventParameters")
                }

                override fun updateUserProfile(attributes: Map<String, Any?>) {
                    println("User Profile Updated: $attributes")
                }

                override fun setUserProfileID(userID: String) {
                    println("User ID Set: $userID")
                }
            }
        }else{
            analyticsHelper!!
        }
    }
}
