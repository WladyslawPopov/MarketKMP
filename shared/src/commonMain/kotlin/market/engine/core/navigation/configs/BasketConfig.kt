package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable

@Serializable
sealed class BasketConfig {
    @Serializable
    data object BasketScreen : BasketConfig()
}
