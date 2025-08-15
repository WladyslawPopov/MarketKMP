package market.engine.core.data.states

import market.engine.core.data.items.MesHeaderItem
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Order

data class DialogContentState(
    val appBarState: SimpleAppBarData = SimpleAppBarData(),
    val responseGetOfferInfo: Offer? = null,
    val responseGetOrderInfo: Order? = null,
    val conversations: Conversations? = null,
    val mesHeader : MesHeaderItem? = null
)
