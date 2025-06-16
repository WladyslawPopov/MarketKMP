package market.engine.core.data.states

import kotlinx.serialization.Serializable
import market.engine.core.data.events.SubItemEvents
import market.engine.core.network.networkObjects.Subscription

@Serializable
data class SubItemState(
    val subscription: Subscription,
    val events: SubItemEvents,
)
