package market.engine.common

import market.engine.core.analytics.AnalyticsHelper


actual object AnalyticsFactory {
    actual fun createAnalyticsHelper(): AnalyticsHelper {
        return DesktopAnalyticsHelper
    }
}
