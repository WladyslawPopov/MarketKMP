package market.engine.core.data.states

import market.engine.core.network.networkObjects.Order
import market.engine.core.repositories.OrderRapository

data class MyOrderItemState(
    val order: Order,
    val orderRapository: OrderRapository
)
