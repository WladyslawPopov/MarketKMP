package market.engine.core.navigation.main.configs

import kotlinx.serialization.Serializable

@Serializable
sealed class BasketConfig {
    @Serializable
    data object BasketScreen : BasketConfig()
}
