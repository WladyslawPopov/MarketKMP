package market.engine.core.navigation.main.configs

import kotlinx.serialization.Serializable
import market.engine.core.types.ReportPageType

@Serializable
data class FeedbackConfig(
    @Serializable
    val type: ReportPageType
)
