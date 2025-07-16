package market.engine.core.data.states

import market.engine.core.data.events.CabinetConversationsItemEvents
import market.engine.core.data.items.MenuItem
import market.engine.core.network.networkObjects.Conversations

data class CabinetConversationsItemState(
    val conversation: Conversations,
    val defOptions : List<MenuItem> = emptyList(),
    val events: CabinetConversationsItemEvents
)
