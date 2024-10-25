// shared/src/iosMain/kotlin/application/analytics/IosAnalyticsHelper.kt
package market.engine.common

import market.engine.core.analytics.AnalyticsHelper

object DesktopAnalyticsHelper : AnalyticsHelper {

    override fun initialize() {

        // Firebase config is typically handled in the iOS app delegate, so no action needed here
    }

    override fun reportEvent(eventName: String, eventParameters: Map<String, Any?>) {

    }

    override fun reportEvent(eventName: String, eventParameters: String) {

    }

    override fun updateUserProfile(attributes: Map<String, Any?>) {

    }

    override fun setUserProfileID(userID: Long) {

    }
}
