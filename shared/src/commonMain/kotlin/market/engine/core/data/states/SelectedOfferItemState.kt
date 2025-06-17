package market.engine.core.data.states

import market.engine.core.data.events.CabinetOfferItemEvents
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.OfferItem

data class SelectedOfferItemState(
    val isSelected : Boolean = false,
    val onSelectionChange: (value : Boolean) -> Unit,
)

data class CabinetOfferItemState(
    val isVisible : Boolean,
    val selectedItem : SelectedOfferItemState? = null,
    val defOptions : List<MenuItem>,
    val item : OfferItem,
    val events : CabinetOfferItemEvents,
)
