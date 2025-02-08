package market.engine.common

import market.engine.core.analytics.AnalyticsHelper

expect object AnalyticsFactory {
    fun getAnalyticsHelper(): AnalyticsHelper
}
