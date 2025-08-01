package market.engine.core.data.states

import kotlinx.serialization.Serializable
import market.engine.core.data.events.BasketEvents
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.network.networkObjects.User

@Serializable
data class SelectedBasketList(
    val userId: Long,
    val selectedOffers: List<SelectedBasketItem>
)

@Serializable
data class BasketItem(
    val user : User = User(),
    val offerList : List<OfferItem> = emptyList()
)

@Serializable
data class ShowBasketItem(
    val userId : Long,
    val index : Int
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
