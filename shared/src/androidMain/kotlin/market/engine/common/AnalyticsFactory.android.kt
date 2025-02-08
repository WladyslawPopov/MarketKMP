package market.engine.common

import market.engine.core.analytics.AnalyticsHelper

var analyticsHelper: AnalyticsHelper? = null

actual object AnalyticsFactory {
    actual fun getAnalyticsHelper(): AnalyticsHelper {
       return analyticsHelper!!
    }
}
