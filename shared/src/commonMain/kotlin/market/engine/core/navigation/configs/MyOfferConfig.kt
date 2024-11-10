package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable
import market.engine.core.types.LotsType

@Serializable
data class MyOfferConfig(
    @Serializable
    val type: LotsType
)
