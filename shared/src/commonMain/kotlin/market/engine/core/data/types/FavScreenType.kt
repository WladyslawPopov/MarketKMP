package market.engine.core.data.types

import kotlinx.serialization.Serializable

@Serializable
enum class FavScreenType {
    FAV_LIST, FAVORITES, NOTES, SUBSCRIBED
}
