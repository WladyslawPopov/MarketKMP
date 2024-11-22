package market.engine.core.navigation.configs

import kotlinx.serialization.Serializable

@Serializable
sealed class HomeConfig {
    @Serializable
    data object HomeScreen : HomeConfig()

    @Serializable
    data class OfferScreen(val id: Long) : HomeConfig()
}
