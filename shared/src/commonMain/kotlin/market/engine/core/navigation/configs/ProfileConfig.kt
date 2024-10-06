package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable

@Serializable
sealed class ProfileConfig {
    @Serializable
    data object ProfileScreen : ProfileConfig()
}
