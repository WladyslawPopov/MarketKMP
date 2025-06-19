package market.engine.core.data.states

import market.engine.core.data.events.BasketEvents
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.network.networkObjects.User

data class SelectedBasketList(
    val userId: Long,
    val selectedOffers: List<SelectedBasketItem>
)

data class BasketGroupUiState(
    val user: User,
    val offersInGroup: List<OfferItem?>,
    val selectedOffers: List<SelectedBasketItem>,
    val showItemsCount: Int,
    val selectedOffersCount: Int = selectedOffers.size,
    val isAllSelected: Boolean,
)

data class BasketUiState(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val basketEvents: BasketEvents,
    val deleteIds: List<Long> = emptyList(),
    val subtitle: String = ""
)
