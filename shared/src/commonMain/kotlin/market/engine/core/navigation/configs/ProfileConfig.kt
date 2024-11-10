package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable
import market.engine.core.types.LotsType


@Serializable
sealed class ProfileConfig {
    @Serializable
    data object ProfileScreen : ProfileConfig()
    @Serializable
    data object MyOffersScreen : ProfileConfig()
}
