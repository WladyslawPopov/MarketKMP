package market.engine.common

import market.engine.core.analytics.AnalyticsHelper

actual object AnalyticsFactory {
    actual fun getAnalyticsHelper(): AnalyticsHelper {
       return analyticsHelper!!
    }
}

var analyticsHelper: AnalyticsHelper? = null





