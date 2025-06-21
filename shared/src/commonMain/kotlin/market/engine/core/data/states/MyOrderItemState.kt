package market.engine.core.data.states

import kotlinx.serialization.Serializable
import market.engine.core.data.events.OrderItemEvents
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.network.networkObjects.Order

@Serializable
data class MyOrderItemState(
    val typeGroup: DealTypeGroup,
    val order: Order,
    val events: OrderItemEvents
)
