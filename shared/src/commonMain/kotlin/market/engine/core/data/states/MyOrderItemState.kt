package market.engine.core.data.states

import market.engine.core.network.networkObjects.Order
import market.engine.core.repositories.OrderBaseViewModel

data class MyOrderItemState(
    val order: Order,
    val orderBaseViewModel: OrderBaseViewModel
)
