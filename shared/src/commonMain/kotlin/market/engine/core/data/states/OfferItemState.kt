package market.engine.core.data.states

import kotlinx.serialization.Serializable
import market.engine.core.data.items.OfferItem

@Serializable
data class OfferItemState(
    val item : OfferItem,
    val onItemClick: () -> Unit = {},
    val addToFavorites : (OfferItem) -> Unit = {},
    val updateItemState : () -> Unit = {}
)
