package market.engine.core.data.states

import market.engine.core.data.items.OfferItem
import market.engine.core.repositories.OfferRepository


data class CabinetOfferItemState(
    val item : OfferItem,
    val offerRepository: OfferRepository
)
