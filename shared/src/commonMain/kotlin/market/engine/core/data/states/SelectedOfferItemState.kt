package market.engine.core.data.states

import market.engine.core.data.events.CabinetOfferItemEvents
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.OfferItem

data class SelectedOfferItemState(
    val selected : List<Long> = emptyList(),
    val onSelectionChange: (id : Long) -> Unit = {},
)

data class CabinetOfferItemState(
    val selectedItem : SelectedOfferItemState? = null,
    val defOptions : List<MenuItem>,
    val item : OfferItem,
    val events : CabinetOfferItemEvents,
)
