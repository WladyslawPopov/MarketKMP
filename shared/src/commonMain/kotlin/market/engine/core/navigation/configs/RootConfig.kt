package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable

@Serializable
sealed class RootConfig {
    @Serializable
    data object Main : RootConfig()

    @Serializable
    data object Login : RootConfig()
}
