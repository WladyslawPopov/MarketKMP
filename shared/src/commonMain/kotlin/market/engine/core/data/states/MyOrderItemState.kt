package market.engine.core.data.states

import market.engine.core.network.networkObjects.Order
import market.engine.core.repositories.OrderRepository

data class MyOrderItemState(
    val order: Order,
    val orderRepository: OrderRepository
)
