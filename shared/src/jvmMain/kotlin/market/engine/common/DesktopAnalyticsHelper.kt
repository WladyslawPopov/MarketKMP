// shared/src/iosMain/kotlin/application/analytics/IosAnalyticsHelper.kt
package market.engine.common

import market.engine.core.analytics.AnalyticsHelper

object DesktopAnalyticsHelper : AnalyticsHelper {
    override fun reportEvent(eventName: String, eventParameters: Map<String, Any?>) {

    }
    override fun updateUserProfile(attributes: Map<String, Any?>) {

    }

    override fun setUserProfileID(userID: String) {

    }
}
