package market.engine.core.data.states


import market.engine.core.data.items.OfferItem
import market.engine.core.repositories.OfferRepository

data class SelectedOfferItemState(
    val selected : List<Long> = emptyList(),
    val onSelectionChange: (id : Long) -> Unit = {},
)

data class CabinetOfferItemState(
    val selectedItem : SelectedOfferItemState? = null,
    val item : OfferItem,
    val offerRepository: OfferRepository
)
