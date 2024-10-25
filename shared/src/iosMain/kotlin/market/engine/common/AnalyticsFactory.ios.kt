package market.engine.common

import IosAnalyticsHelperWrapper
import market.engine.core.analytics.AnalyticsHelper

actual object AnalyticsFactory {
    actual fun createAnalyticsHelper(): AnalyticsHelper {
       return IosAnalyticsHelperWrapper
    }
}





